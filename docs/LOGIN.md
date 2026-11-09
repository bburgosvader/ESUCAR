# Inicio de sesión local · versión 1.2.0

La aplicación comienza en **Inicia sesión**. Para el primer uso selecciona **Primera vez · Crear cuenta**, completa nombre, correo, contraseña de 8–128 caracteres y confirmación. La sesión se conserva al volver a abrir la app. En **Ajustes → Cerrar sesión** puedes salir sin borrar los avances.

Las cuentas pertenecen únicamente al dispositivo. No hay conexión con la identidad de ESUCAR ni recuperación por correo. Cada cuenta utiliza su propio archivo de persistencia. La primera cuenta creada recupera una sola vez los avances anteriores a esta versión; las siguientes comienzan con datos nuevos. Desinstalar borra cuentas y avances.

Las contraseñas se derivan con PBKDF2-HMAC-SHA256, sal aleatoria de 16 bytes y 120 000 iteraciones. No se almacenan como texto ni se incluyen en el estado guardado de la Activity. La validación y derivación se ejecutan fuera del hilo de interfaz. El botón permite mostrar/ocultar la contraseña y los errores aparecen junto al campo.

El perfil docente continúa como demostración académica dentro de la cuenta: no acredita autorización institucional.

## Código

- `ui/auth/LoginScreen.kt`: formularios de ingreso y registro.
- `ui/auth/LoginViewModel.kt`: estado, errores y operaciones de autenticación.
- `data/repository/AuthRepository.kt`: cuentas y sesión persistidas localmente.
- `domain/validacion/AuthValidator.kt`: correo y longitud de contraseña.
- `domain/seguridad/PasswordHasher.kt`: derivación y verificación.
- `di/AppContainer.kt`: construcción de dependencias.

Al entrar o salir se descartan los borradores del contexto anterior. Las descargas ya iniciadas conservan fijo el destino para no mezclar datos entre cuentas.

## Verificación

Versión 1.2.0: 35 pruebas aprobadas (23 de lógica/estado y 12 de integración Android simulada), APK debug generado y cero errores de lint.

Se verificó: validación, hashes distintos para una misma contraseña, contraseña incorrecta, rechazo de duplicados, persistencia de sesión, aislamiento de cuentas, migración del avance anterior, primera apertura con acceso restringido, errores visibles por campo y recorrido UI de registro a Inicio y cierre a Login.

Validación manual pendiente en dispositivo: teclado, mostrar/ocultar contraseña, giro de pantalla, reapertura con sesión guardada y uso de dos cuentas. Las pruebas simuladas no reemplazan esta revisión de hardware.
