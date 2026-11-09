# Diseño institucional y Material 3 · versión 1.3.0

La app usa `com.google.android.material:material:1.14.0` y el tema **Theme.Material3.Light.NoActionBar**, con vistas Android y arquitectura MVVM. Material 3 no requiere reescribir la aplicación en Compose.

| Punto solicitado | Implementación |
|---|---|
| Diseño visual Material 3 | MaterialButton, MaterialCardView, TextInputLayout/TextInputEditText, BottomNavigationView, MaterialCheckBox, MaterialSwitch y controles de progreso Material |
| Formularios validados | Nombre, correo, contraseña y confirmación; notas, retroalimentación, fecha, formato, criterios, URL y búsqueda. Reglas en dominio y estado de errores en ViewModel |
| Navegación funcional | Login/registro, acceso académico con cuatro destinos, etapas, revisión docente y cierre de sesión. Navegación inferior Material y botón Atrás |
| Gestión de estado | ViewModels y StateFlow observados con ciclo de vida; borradores y restauración de pantalla |
| Almacenamiento local | Cuentas y sesión locales; avances por cuenta; persistencia de pautas, notas, fechas, checklist y FAQ |
| Recursos nativos | Documentos mediante SAF, notificaciones y alarmas locales, calendario con confirmación del usuario |
| Animaciones | Entrada de pantalla, barras de avance y respuesta visual de errores; indicador de carga de contenidos |

## Colores y logo

Fuente: [sitio de Carabineros de Chile](https://www.carabineros.cl/), consultado durante el desarrollo. El sitio mostraba una página de mantenimiento con variables CSS: `--verde-institucional: #1d7d5f`, `--verde-secundario: #1d5139` y `--verde-acento: #8aaa45`. Se tomaron esos valores como referencia visual, sin afirmar que se disponga de un manual institucional completo. El tema agrega colores de superficies, texto, contornos y errores para legibilidad.

Logotipo original: [logomenu.jpg del sitio institucional](https://www.carabineros.cl/logomenu.jpg), almacenado sin redibujarlo en `res/drawable-nodpi/logo_carabineros.jpg`. Se presenta con proporción conservada, margen y descripción accesible. Funciona sin internet.

No se encontró un recurso independiente verificable del escudo ESUCAR en las páginas oficiales consultadas. La escuela se identifica mediante su nombre; no se inventó un escudo. Si el equipo recibe el archivo institucional autorizado, puede añadirse como segundo recurso.

La aplicación es un prototipo académico DSY1105. Los nombres y símbolos pertenecen a la institución; su inclusión no implica que el producto sea oficial ni que cuente con aval institucional.

## Revisión

Las pruebas automatizadas comprueban reglas, estado, login, persistencia y navegación. La revisión de accesibilidad con TalkBack y de notificaciones/calendario/proveedor documental en un dispositivo real sigue siendo necesaria antes de la defensa.
