# Verificación

## Automatizada

Comando: `gradlew assembleDebug testDebugUnitTest lintDebug`.

- APK debug generado correctamente.
- Cinco pruebas unitarias: restricciones del estudiante, revisión docente, búsqueda normalizada y categorías, vencimiento de entregas y consistencia del catálogo.
- Lint: cero errores. Las advertencias no se ocultaron con una baseline.

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
