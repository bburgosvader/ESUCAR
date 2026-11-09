# Verificación

## Automatizada

Comando: `gradlew assembleDebug testDebugUnitTest lintDebug`.

- APK debug generado correctamente.
- 27 pruebas aprobadas: 5 de reglas, 6 de validadores, 10 de ViewModel y 6 de integración Android con Robolectric 4.17/API 35.
- Integración ejecutada: errores junto al campo y corrección, persistencia del nombre, borrador al recrear Activity, flujo estudiante/docente, datos guardados en SharedPreferences, rechazo de JSON sin perder la última biblioteca válida, notificación autorizada/denegada e intents con permisos mínimos.
- Las pruebas de integración se ejecutan en Android simulado dentro de la JVM; no son pruebas físicas ni de servicios externos.
- Lint: cero errores. Las advertencias no se ocultaron con una baseline.

## Entorno y límites

Compilación con JDK 21, Gradle 9.4.1 y AGP 9.2.1. El emulador local informa que la aceleración de Windows no está disponible; no se modificaron características del sistema. La versión 1.1.0 debe recorrerse en teléfono o emulador habilitado antes de la defensa.

## Validación manual en Android

Estos casos son un plan de validación; no deben confundirse con pruebas ejecutadas automáticamente:

1. Primera apertura: seis etapas por iniciar, fechas relativas al primer uso y progreso 0/6.
2. Sin internet: abrir guías y buscar `citacion`; obtener resultados de Citación.
3. Comenzar hito, marcar lista, guardar notas, cerrar y abrir: conservar todos los datos.
4. Intentar enviar una lista incompleta: impedir el cambio y mostrar explicación.
5. Completar lista y registrar envío: estado enviado y lista no editable.
6. Cambiar a docente, agregar comentarios y devolver: volver a desarrollo; estudiante puede editar.
7. Reenviar y aprobar: incrementar el progreso; estudiante no puede aprobar por sí mismo.
8. Cambiar fecha y pauta en docente: reflejar valores al regresar a estudiante.
9. Vincular documento, reiniciar app y abrirlo; si se elimina, mostrar error recuperable.
10. Autorizar notificaciones y probar aviso; denegar permiso y comprobar que no se produce cierre inesperado.
11. Guardar URL del JSON de ejemplo y actualizar: mostrar FAQ nueva también sin red. URL inválida o JSON incorrecto: conservar biblioteca anterior.
12. Rotar dispositivo y probar tableta, teclado y fuente grande: navegación legible y contenido desplazable.
13. Reiniciar dispositivo con avisos activos: comprobar reprogramación. Las alarmas son inexactas.

Para uso real también se deben validar contenidos con el docente, accesibilidad con TalkBack, comportamiento bajo ahorro de batería y política de archivos del proveedor de documentos.

14. En docente, escribir fecha inexistente: mostrar error junto al campo y conservar la anterior.
15. Intentar devolver sin explicación suficiente: error de retroalimentación; corregir y reintentar.
16. Abrir calendario desde un hito: verificar título y fecha antes de guardar; cancelar no modifica el hito.
17. Desactivar animaciones en Android y repetir navegación: todo debe seguir funcionando.
18. Ejecutar con documento eliminado/proveedor sin acceso y notificaciones bloqueadas: mensajes recuperables, sin cierre inesperado.
