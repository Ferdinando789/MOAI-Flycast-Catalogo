# Catálogo de juegos para la biblioteca Flycast de MOAI

MOAI consulta `manifest.json` en este repositorio. El archivo existente conserva su lista `games`; este cambio no añade ROMs ni BIOS. Solo publicar contenido que se pueda distribuir.

Cada juego tiene `id` (letras, números, guion o guion bajo), `title`, `system` y los archivos a descargar. Para un CHD/CDI/ZIP de un solo archivo:

```json
{
  "id": "demo-dreamcast",
  "title": "Demo Dreamcast",
  "system": "Dreamcast",
  "file": "demo.chd",
  "url": "https://github.com/Ferdinando789/MOAI-Flycast-Catalogo/releases/download/JUEGO/demo.chd",
  "size": 123456,
  "sha256": "REEMPLAZAR_POR_SHA256_REAL_DE_64_CARACTERES"
}
```

El ejemplo indica el formato, no un juego publicado. El tamaño y el hash deben calcularse sobre los bytes finales del archivo. GitHub Releases permite distribuir archivos grandes sin añadirlos al historial del repositorio.

Para GDI/CUE con pistas separadas, usar `entry` y `files`:

```json
{
  "id": "demo-multitrack",
  "title": "Demo con pistas",
  "system": "Dreamcast",
  "entry": "disc.gdi",
  "files": [
    {"path":"disc.gdi","url":"URL_GITHUB_DEL_DESCRIPTOR","size":123,"sha256":"SHA256_REAL"},
    {"path":"track01.bin","url":"URL_GITHUB_DE_LA_PISTA","size":456,"sha256":"SHA256_REAL"}
  ]
}
```

Incluir todas las pistas y conservar los nombres relativos escritos en el descriptor. MOAI verifica cada archivo, rechaza rutas que salen de la carpeta del juego y solo muestra la descarga cuando está completa. Se permiten enlaces HTTPS a `raw.githubusercontent.com/Ferdinando789/MOAI-Flycast-Catalogo/` o a Releases del mismo repositorio. Un máximo de 100 archivos y 12 GiB por juego.

Los juegos se guardan en la carpeta externa propia de MOAI. Los juegos existentes se pueden buscar por carpeta o en los volúmenes internos/USB accesibles; Android puede pedir acceso a archivos. La búsqueda lista formatos compatibles y no valida que una ROM o BIOS sea la correcta. El catálogo sigue siendo independiente del catálogo FinalBurn Neo.

La versión de prueba 0.1.79 usa un módulo ARM32 específico, publicado en `modules/flycast-vulkan-arm32-v79.json`. El manifiesto r1 para la base anterior sigue intacto. El sistema de actualización estable de MOAI no se cambia.
