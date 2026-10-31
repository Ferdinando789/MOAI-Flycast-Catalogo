# MOAI Flycast Vulkan — módulo descargable ARM32

MOAI Lite Deluxe usa un núcleo opcional compatible con Android TV de **32 bits
(`armeabi-v7a`)**. No es una segunda aplicación: el núcleo está empaquetado
como un split APK (`flycast_core`) que Android instala dentro del mismo
identificador de paquete de MOAI.

El archivo público `flycast-vulkan-arm32.json` se habilita **solamente**
cuando el módulo firmado `flycast_core_arm32.apk` haya sido publicado. Incluye
la URL HTTPS, tamaño y SHA-256 para verificar la descarga. Antes de instalar,
Android también comprueba que el split y el MOAI base tengan idénticos
paquete, versión y certificado.

**No hospedar claves de firma aquí.** El núcleo debe firmarse en el
workflow privado junto con su APK base. La firma solo pertenece al proyecto
MOAI y nunca se publica.

Los ejecutables `.so` son código abierto de Flycast y los avisos de licencia
correspondientes se mantienen en el repositorio de código fuente. Aquí no
se alojan juegos, ROMs ni BIOS comerciales.
