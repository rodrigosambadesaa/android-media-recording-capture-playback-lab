# Android media recording, capture & playback lab

Modernización del bloque histórico de **grabación** de Programación Multimedia y Dispositivos Móviles.

## Funcionalidades

- Solicitud contextual de `RECORD_AUDIO`.
- Grabación de audio con `MediaRecorder`.
- Reproducción posterior con `MediaPlayer`.
- Solicitud contextual de `CAMERA`.
- Captura de foto con `ActivityResultContracts.TakePicture`.
- Captura de vídeo con `ActivityResultContracts.CaptureVideo`.
- `FileProvider` para compartir URIs seguras con la app de cámara.
- Reproducción del vídeo capturado en `VideoView` + `MediaController`.
- Selector moderno de vídeo con `OpenDocument`.
- Almacenamiento en directorios externos **propios de la app**, sin pedir permisos generales de almacenamiento.

## Diferencia frente al material antiguo

El material original proponía `WRITE_EXTERNAL_STORAGE` y `RECORD_AUDIO` para grabar en almacenamiento
externo. La reconstrucción conserva `RECORD_AUDIO`, pero evita el permiso de escritura general porque usa
almacenamiento app-specific y URIs de `FileProvider`, coherente con Android moderno.

También se sustituye `startActivityForResult()/onActivityResult()` por Activity Result APIs.
