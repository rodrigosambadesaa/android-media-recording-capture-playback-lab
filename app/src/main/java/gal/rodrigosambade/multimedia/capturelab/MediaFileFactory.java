package gal.rodrigosambade.multimedia.capturelab;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class MediaFileFactory {
    private MediaFileFactory() {}

    static File create(Context context, String directory, String prefix, String extension) {
        File base = context.getExternalFilesDir(directory);
        if (base == null) base = context.getFilesDir();
        if (!base.exists() && !base.mkdirs()) {
            throw new IllegalStateException("Cannot create media directory");
        }
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
        return new File(base, prefix + "_" + stamp + extension);
    }
}
