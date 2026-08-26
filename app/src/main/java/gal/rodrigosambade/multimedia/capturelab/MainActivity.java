package gal.rodrigosambade.multimedia.capturelab;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private TextView status;
    private ImageView imagePreview;
    private VideoView videoView;
    private Button audioButton;
    private MediaRecorder recorder;
    private MediaPlayer audioPlayer;
    private File lastAudio;
    private Uri pendingPhoto;
    private Uri pendingVideo;
    private boolean recording;
        private boolean pendingCameraVideo;

    private final ActivityResultLauncher<String> audioPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startAudioRecording();
                else status.setText("Permiso de micrófono denegado");
            });

    private final ActivityResultLauncher<String> cameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) capturePhoto();
                else status.setText("Permiso de cámara denegado");
            });

    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok && pendingPhoto != null) {
                    imagePreview.setImageURI(pendingPhoto);
                    status.setText("Foto capturada");
                }
            });

    private final ActivityResultLauncher<Uri> captureVideo =
            registerForActivityResult(new ActivityResultContracts.CaptureVideo(), ok -> {
                if (ok && pendingVideo != null) {
                    status.setText("Vídeo capturado");
                    playVideo(pendingVideo);
                }
            });

    private final ActivityResultLauncher<String[]> pickVideo =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(
                            uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    playVideo(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.tvStatus);
        imagePreview = findViewById(R.id.imagePreview);
        videoView = findViewById(R.id.videoView);
        audioButton = findViewById(R.id.btnAudio);
        Button playAudio = findViewById(R.id.btnPlayAudio);
        Button photo = findViewById(R.id.btnPhoto);
        Button video = findViewById(R.id.btnVideo);
        Button openVideo = findViewById(R.id.btnPickVideo);

        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);

        audioButton.setOnClickListener(v -> {
            if (recording) stopAudioRecording();
            else ensureAudioPermission();
        });
        playAudio.setOnClickListener(v -> playLastAudio());
        photo.setOnClickListener(v -> ensureCameraThen(false));
        video.setOnClickListener(v -> ensureCameraThen(true));
        openVideo.setOnClickListener(v -> pickVideo.launch(new String[]{"video/*"}));
    }

    private void ensureAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startAudioRecording();
        } else {
            audioPermission.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void ensureCameraThen(boolean video) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (video) captureVideo(); else capturePhoto();
        } else {
            // La acción pendiente se simplifica a foto para mantener el ejemplo legible.
            // Tras conceder, el usuario puede pulsar de nuevo "Capturar vídeo".
            cameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void startAudioRecording() {
        lastAudio = MediaFileFactory.create(this, Environment.DIRECTORY_MUSIC, "audio", ".m4a");
        recorder = Build.VERSION.SDK_INT >= 31 ? new MediaRecorder(this) : new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(lastAudio.getAbsolutePath());
        try {
            recorder.prepare();
            recorder.start();
            recording = true;
            audioButton.setText("Detener grabación de audio");
            status.setText("Grabando… " + lastAudio.getName());
        } catch (IOException | RuntimeException e) {
            status.setText("Error al iniciar grabación: " + e.getClass().getSimpleName());
            releaseRecorder();
        }
    }

    private void stopAudioRecording() {
        try {
            recorder.stop();
            status.setText("Audio guardado: " + lastAudio.getName());
        } catch (RuntimeException e) {
            status.setText("Grabación demasiado corta o inválida");
        } finally {
            releaseRecorder();
            recording = false;
            audioButton.setText("Iniciar grabación de audio");
        }
    }

    private void playLastAudio() {
        if (lastAudio == null || !lastAudio.exists()) {
            status.setText("Aún no hay audio grabado");
            return;
        }
        releaseAudioPlayer();
        audioPlayer = new MediaPlayer();
        try {
            audioPlayer.setDataSource(lastAudio.getAbsolutePath());
            audioPlayer.setOnCompletionListener(mp -> releaseAudioPlayer());
            audioPlayer.prepare();
            audioPlayer.start();
            status.setText("Reproduciendo " + lastAudio.getName());
        } catch (IOException e) {
            status.setText("No se pudo reproducir el audio");
            releaseAudioPlayer();
        }
    }

    private void capturePhoto() {
        File file = MediaFileFactory.create(this, Environment.DIRECTORY_PICTURES, "photo", ".jpg");
        pendingPhoto = FileProvider.getUriForFile(this, getPackageName() + ".files", file);
        takePicture.launch(pendingPhoto);
    }

    private void captureVideo() {
        File file = MediaFileFactory.create(this, Environment.DIRECTORY_MOVIES, "video", ".mp4");
        pendingVideo = FileProvider.getUriForFile(this, getPackageName() + ".files", file);
        captureVideo.launch(pendingVideo);
    }

    private void playVideo(Uri uri) {
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> {
            status.setText("Vídeo preparado: " + mp.getVideoWidth() + "×" + mp.getVideoHeight());
            videoView.start();
        });
    }

    private void releaseRecorder() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    private void releaseAudioPlayer() {
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        if (recording) stopAudioRecording();
        releaseAudioPlayer();
        super.onStop();
    }
}
