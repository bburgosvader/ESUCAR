# Cobertura de Evaluación Parcial 2 · DSY1105

Revisión de la versión 1.1.0 contra los siete indicadores del encargo. Este documento distingue implementación, verificación automatizada y requisitos académicos pendientes; no promete una calificación.

| Indicador | Peso | Implementación / evidencia | Estado y comprobación |
|---|---:|---|---|
| IE 2.1.1: estructura visual y navegación | 15% | Inicio, Guía, Consultas, Ajustes, jerarquía de títulos, lectura limitada a 840 dp, desplazamiento y botón Atrás del sistema | Implementado. Integración automatizada de navegación; revisión visual en teléfono/tableta y TalkBack pendiente |
| IE 2.1.2: formularios, iconos y errores por campo | 15% | `ValidatedField`, iconos de edición, éxito y advertencia; campos nombre, notas, fecha, formato, criterios, retroalimentación, URL y búsqueda | Implementado. Pruebas de límites, errores visibles, recuperación al corregir y guardado válido |
| IE 2.2.1: validación desacoplada y estado | 10% | `FormValidator` puro, `TutorViewModel`, `TutorUiState`, `StateFlow` observado con ciclo de vida | Implementado. La Activity no determina reglas de validación; borradores y errores sobreviven recreación |
| IE 2.2.2: animaciones funcionales | 10% | Entrada de pantallas, avance de barras, movimiento breve del campo inválido e indicador de actualización | Implementado. Respeta la desactivación de animaciones de Android; fluidez en hardware pendiente |
| IE 2.3.1: modularidad y persistencia | 15% | Paquetes `domain`, `data`, `di`, `ui`, `nativo`; contrato `TutorStore`; SharedPreferences y documento vinculado por URI | Implementado. Integración de repositorio y persistencia comprobada; conserva las claves de la versión 1.0 |
| IE 2.3.2: colaboración y control de versiones | 20% | Repositorio público, commits técnicos, [tablero Trello](https://trello.com/b/wgburVCk/esucar) y [planificación](PLANIFICACION.md) | Preparado, pero falta identificar integrantes, asignar responsables y evidenciar aportes reales de ambos. La asistencia automatizada no prueba participación grupal |
| IE 2.4.1: dos o más recursos nativos | 15% | Selector de documentos (SAF), notificaciones locales y calendario del sistema | Tres integraciones. Pruebas de permisos/notificación e intents en Android simulado; interacción real con proveedor de archivos, calendario y alarmas pendiente |

## Formularios y reglas

| Campo | Regla | Respuesta ante error |
|---|---|---|
| Nombre | 2–60 caracteres, al menos una letra | Error junto al campo, icono, no reemplaza el nombre guardado |
| Notas | Opcional, máximo 2000 caracteres | Conserva borrador inválido, mantiene último valor persistido válido |
| Retroalimentación | Máximo 2000; al solicitar correcciones, al menos 10 | Bloquea devolución y explica la corrección necesaria |
| Fecha | Formato AAAA-MM-DD y fecha existente | No guarda fechas inexistentes; ofrece selector de fecha |
| Formato de entrega | 10–2000 caracteres | Impide guardar una pauta incompleta |
| Rúbrica | 1–12 líneas; cada criterio 5–200 caracteres | Valida todos los criterios, no solo el primero |
| URL | Opcional; HTTPS, host, sin credenciales ni fragmentos; máximo 2048 | Rechaza URL inválida y conserva la fuente anterior |
| Búsqueda | Máximo 120 caracteres | Muestra explicación sin ejecutar búsqueda inválida |

Las reglas viven en dominio; `ValidatedField` solamente muestra resultados. Enviar requiere completar la lista; aprobar corresponde al docente demo; los hitos enviados/aprobados no se editan como estudiante.

## Pendientes que no puede sustituir el código

- Nombres y apellidos de ambos integrantes y número de equipo: pendientes por indicación del usuario.
- Asignar miembros a las tarjetas y registrar contribuciones reales, revisiones y acuerdos. No se inventaron responsables ni actividad pasada.
- Validar con el docente el contenido metodológico, los formatos y las fechas de ejemplo.
- Probar en teléfono o emulador habilitado: permisos, proveedor de documentos, calendario, alarmas, fuente grande y TalkBack.
- Comprobar acceso del docente y de los miembros al repositorio/tablero.
- Realizar la entrega grupal en AVA y activar el enlace individual; no se enviaron entregas académicas ni se activaron enlaces desde este proyecto.

El usuario indicó que el profesor no fijó fecha. El equipo debe confirmar y respetar el plazo final antes de entregar; esta versión no constituye una congelación académica automática del repositorio.

## Actualización visual 1.3.0

Se incorporó Material 3 con componentes reales de Google y la identidad visual de referencia. Consulta [DISENO_MATERIAL3.md](DISENO_MATERIAL3.md) para los siete puntos y los recursos institucionales utilizados.
