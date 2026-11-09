# ESUCAR · Tutor Virtual

Aplicación Android escrita en **Kotlin**, preparada para **Android Studio**, basada en el caso académico DSY1105: acompañamiento de estudiantes en Trabajo de Título I, II y III.

## Equipo y planificación

- Integrante 1: pendiente de confirmar por el equipo.
- Integrante 2: pendiente de confirmar por el equipo.
- Número de equipo: pendiente de confirmar.
- [Tablero Trello ESUCAR](https://trello.com/b/wgburVCk/esucar).
- [Matriz completa de la rúbrica](docs/RUBRICA.md), [arquitectura MVVM](docs/ARQUITECTURA.md) y [planificación](docs/PLANIFICACION.md).

La versión 1.1.0 completa las mejoras técnicas de la Evaluación Parcial 2. Quedan pendientes la identificación de integrantes, su participación real y las validaciones en dispositivo indicadas en la matriz. No se han realizado entregas en AVA.

## Funcionalidades

- Inicio con progreso, próximo hito y alertas de fechas vencidas.
- MVVM con `TutorViewModel`, `StateFlow` y separación de datos, dominio, presentación y recursos nativos.
- Formularios con iconos, mensajes por campo y validaciones centralizadas; conserva borradores al recrear la pantalla.
- Transiciones de pantalla, barras animadas y retroalimentación de errores; respeta las animaciones desactivadas de Android.
- Integración con calendario mediante un borrador de evento que el usuario confirma.
- Seis etapas: planteamiento, marco teórico, diseño metodológico, recogida de información, análisis e informe final.
- Orientación paso a paso, formato requerido y criterios de revisión por etapa.
- Listas de avance, notas con guardado automático y documento local vinculado mediante el selector de Android.
- Estados: por iniciar → en desarrollo → enviado → aprobado; el docente puede solicitar correcciones.
- Perfil estudiante y perfil docente **de demostración**, intercambiables en Ajustes.
- Docente: modificar fechas, formato y criterios; guardar retroalimentación y aprobar hitos enviados.
- Buscador de 15 FAQ con categorías y búsqueda sin distinguir mayúsculas ni tildes.
- Contenido y avance disponibles sin conexión.
- Recordatorios locales con permiso de notificaciones en Android 13 o superior; reprogramación después del reinicio.
- Actualización opcional de FAQ desde JSON HTTPS, manual y periódica con conectividad.
- Diseño adaptable, área de lectura limitada en tabletas, texto escalable y controles de Android.

## Abrir y ejecutar en Android Studio

1. Clona este repositorio o descarga y descomprime el ZIP.
2. En Android Studio selecciona **Open** y abre la carpeta que contiene `settings.gradle.kts`.
3. Usa un JDK **17 o superior** como Gradle JDK (probado con JDK 21).
4. Instala en SDK Manager **Android SDK Platform 37** y **Build Tools 36.0.0**. Android Studio detectará o creará `local.properties` con la ruta de tu SDK.
5. Espera la sincronización de Gradle. Se necesita internet para descargar las dependencias por primera vez.
6. Ejecuta la configuración **app** en un emulador o teléfono Android 8.0/API 26 o superior.

Herramientas fijadas: Android Gradle Plugin 9.2.1 (Kotlin integrado), Gradle 9.4.1; compile/target SDK 37. Usa una versión de Android Studio compatible con AGP 9.2.1 o posterior. [Compatibilidad oficial de AGP 9.2](https://developer.android.com/build/releases/agp-9-2-0-release-notes).

En Windows:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

En macOS/Linux:

```sh
chmod +x gradlew
./gradlew assembleDebug testDebugUnitTest lintDebug
```

APK de prueba: `app/build/outputs/apk/debug/app-debug.apk`. No requiere claves privadas ni servicios externos. La firma debug es para desarrollo, no para publicar en Play Store.

## Recorrido para presentar el MVP

1. En **Guía**, abre el planteamiento y pulsa **Comenzar esta etapa**.
2. Revisa la orientación y la pauta, marca los cinco puntos y agrega una nota. Puedes vincular un documento.
3. Pulsa **Marcar como enviado** y confirma el registro local.
4. En **Ajustes**, cambia al perfil docente de demostración.
5. Abre el mismo hito, registra retroalimentación y solicita correcciones o aprueba la entrega.
6. Regresa a estudiante y comprueba el estado y el progreso del inicio.
7. En **Consultas**, busca `citacion` o `metodologia` y filtra una categoría.
8. Activa los recordatorios y usa **Probar notificación**. Cierra y vuelve a abrir la aplicación para comprobar la persistencia.

## Actualización de contenidos

En Ajustes puede configurarse una URL HTTPS directa a un documento como [docs/faq-ejemplo.json](docs/faq-ejemplo.json). Después de publicar este repositorio puede usarse:

```text
https://raw.githubusercontent.com/bburgosvader/ESUCAR/main/docs/faq-ejemplo.json
```

Pulsa **Guardar fuente** y **Actualizar ahora**. Las FAQ recibidas se agregan a la biblioteca base y quedan guardadas sin conexión. La siguiente actualización reemplaza el conjunto remoto anterior. No se transmiten notas, documentos ni estados. Se conservan los contenidos anteriores si la descarga falla. El archivo debe contener entre 1 y 200 preguntas y no superar 1 MB. Solo se aceptan respuestas HTTP 200, sin redirecciones.

Android intentará actualizar cada seis horas con conexión; el sistema puede posponerlo por batería. No hay servidor institucional incluido. La fuente viene vacía de forma intencionada para que el docente elija el contenido autorizado.

## Recordatorios

Se programa un aviso aproximado a las 09:00 del día anterior a una entrega. Si ese momento ya pasó, se intenta a las 09:00 de la fecha de entrega. No se programan avisos retroactivos ni para estados enviado/aprobado. Android puede retrasar alarmas inexactas; no se solicita permiso de alarmas exactas. Tras forzar detención, vuelve a abrir la aplicación para reprogramarlas. Es necesario permitir notificaciones.

## Estructura

```text
app/src/main/java/cl/esucar/tutor/
MainActivity.kt              Punto de entrada (6 líneas)
TutorApp.kt                  Inicio y contenedor de la aplicación
data/
  local/Catalog.kt           Guías y FAQ incluidas
  repository/Repository.kt  Persistencia local
  remote/ContentSyncService.kt  Actualización HTTPS
di/AppContainer.kt          Construcción e inyección de dependencias
domain/
  model/Domain.kt            Modelos y reglas de estados
  repository/TutorStore.kt   Contrato del repositorio
  validacion/FormValidator.kt  Validaciones puras
nativo/                     Documentos, calendario y notificaciones
ui/
  components/               Campos, controles y animaciones compartidas
  estudiante/               InicioScreen, GuiaScreen, EtapaScreen, AyudaScreen
  profesor/RevisionScreen.kt  Formulario y revisión docente
  perfil/PerfilScreen.kt     Perfil y ajustes
  navegacion/               Host de pantallas, navegación y ViewModel
  theme/TutorTheme.kt        Paleta de colores
```

Se usa ComponentActivity de AndroidX y vistas nativas, ViewModel/StateFlow para la lógica y estado, SharedPreferences para el pequeño conjunto local y JobScheduler/AlarmManager del sistema. La interfaz está en español. No se requieren Firebase ni claves API. Las pruebas de integración usan Robolectric 4.17 y descargan su Android simulado en la primera ejecución.

## Alcance y limitaciones

Este es un **MVP académico**, no una aplicación oficial de ESUCAR ni de Carabineros de Chile. Las guías, rúbricas y fechas iniciales son ilustrativas: el caso no entregó pautas oficiales, ponderaciones ni calendario. El docente debe validarlas antes de uso real.

Los perfiles son una simulación en un único dispositivo, sin autenticación ni múltiples estudiantes. **Enviado** registra un estado local: no manda el documento al profesor. El tutor es una biblioteca consultable, no un modelo de IA. Los documentos se vinculan por URI; si el proveedor retira acceso o elimina el archivo, hay que volver a vincularlo. No hay sincronización de avances entre dispositivos, entrega remota, calificaciones oficiales ni respaldo en nube. Para un despliegue real se necesitarían identidad, autorización y un backend institucional.

No incluyas datos personales u operativos sensibles en la demostración. El repositorio no contiene el PDF original, credenciales, almacenes de claves ni configuraciones privadas de la máquina.

## Verificación

Versión 1.1.0: APK debug generado, **27 pruebas aprobadas** (21 de lógica/estado y 6 de integración con Android API 35 simulado) y **0 errores de lint**. No se ocultaron las advertencias con una baseline. Se comprobaron campos inválidos y recuperación, borradores al recrear Activity, entrega/aprobación, persistencia, permisos de notificaciones y los intents de documentos/calendario.

El emulador no pudo arrancar por la configuración de aceleración de Windows. Las pruebas Robolectric no reemplazan la verificación de hardware: quedan por ejecutar los casos manuales de [docs/PRUEBAS.md](docs/PRUEBAS.md). El APK puede probarse en un teléfono Android 8.0 o superior.
