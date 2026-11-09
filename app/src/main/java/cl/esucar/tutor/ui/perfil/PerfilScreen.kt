package cl.esucar.tutor.ui.perfil

import cl.esucar.tutor.domain.model.*
import cl.esucar.tutor.domain.validacion.*
import cl.esucar.tutor.domain.repository.*
import cl.esucar.tutor.data.repository.Repository
import cl.esucar.tutor.data.remote.ContentSync
import cl.esucar.tutor.data.remote.ContentSyncService
import cl.esucar.tutor.nativo.*
import cl.esucar.tutor.ui.navegacion.*
import cl.esucar.tutor.ui.components.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cl.esucar.tutor.ui.estudiante.*
import cl.esucar.tutor.ui.perfil.*
import cl.esucar.tutor.ui.profesor.*
import cl.esucar.tutor.ui.theme.TutorTheme
import cl.esucar.tutor.R

internal fun TutorScreenHost.settings() {
        addText(content, "Tu espacio de estudio", 28f, ink, true)
        addText(content,auth.state.value.session.orEmpty(),14f,green)
        button(content,"Cerrar sesión") {
            AlertDialog.Builder(this).setTitle("Cerrar sesión")
                .setMessage("Tus avances quedarán guardados en esta cuenta local.")
                .setNegativeButton("Cancelar",null).setPositiveButton("Cerrar sesión") { _,_ -> auth.logout() }.show()
        }
        card { box ->
            addText(box, "Tu cuenta", 20f, ink, true)
            val name = input(box,"Nombre para mostrar",vm.name,false,"name",FieldKind.NAME,"Obligatorio · de 2 a 60 caracteres")
            button(box, "Guardar nombre") {
                if(!vm.saveName()) focusFirstError()
            }
            addText(box, "Perfil actual: ${if (vm.role == Role.STUDENT) "Estudiante" else "Docente"}", 16f, green, true)
            addText(box, "La sesión pertenece a esta cuenta local. El cambio de perfil docente es una simulación; no acredita permisos institucionales.", 14f, muted)
            button(box, if (vm.role == Role.STUDENT) "Cambiar a docente (demo)" else "Cambiar a estudiante") {
                vm.switchRole()
            }
        }
        card { box ->
            addText(box, "Recordatorios de entrega", 20f, ink, true)
            addText(box, "Aviso local a las 09:00 del día anterior; si ya pasó, se programa para las 09:00 del día de entrega. Android puede retrasarlo para ahorrar batería.", 15f, muted)
            val toggle = com.google.android.material.materialswitch.MaterialSwitch(this).apply {
                text = "Activar recordatorios"; textSize = 16f; minHeight = dp(56); isChecked = vm.reminders
                setOnCheckedChangeListener { _, enabled ->
                    vm.setReminders(enabled)
                    if (enabled && android.os.Build.VERSION.SDK_INT >= 33 && !Reminders.allowed(this@settings))
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    Reminders.schedule(this@settings)
                }
            }
            box.addView(toggle)
            button(box, "Probar notificación") {
                if (!Reminders.allowed(this)) toast("Autoriza las notificaciones en Ajustes de Android.")
                else { Reminders.show(this, 999, "Tutor Virtual ESUCAR", "Tus recordatorios están disponibles. Revisa tus próximas entregas."); toast("Notificación de prueba enviada") }
            }
        }
        card { box ->
            addText(box, "Contenidos sin conexión", 20f, ink, true)
            addText(box, "Guías y avances siempre disponibles. Puedes agregar FAQ desde una fuente JSON HTTPS del docente. No se suben tus notas ni documentos.", 15f, muted)
            val endpoint = input(box,"URL de actualización (opcional)",vm.endpoint,false,"endpoint",FieldKind.ENDPOINT,"HTTPS · dejar vacío desactiva la actualización remota")
            endpoint.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
            addText(box, vm.syncTime, 14f, green)
            button(box, "Guardar fuente") {
                if(vm.saveEndpoint()) ContentSyncService.schedule(this) else focusFirstError()
            }
            syncStatus=label(vm.ui.syncMessage,14f,muted).also { it.accessibilityLiveRegion=View.ACCESSIBILITY_LIVE_REGION_POLITE; box.addView(it) }
            syncSpinner=com.google.android.material.progressindicator.CircularProgressIndicator(this).apply { isIndeterminate=true; contentDescription="Actualizando preguntas"; visibility=if(vm.ui.busy) View.VISIBLE else View.GONE }.also { box.addView(it,LinearLayout.LayoutParams(dp(40),dp(40))) }
            button(box,"Actualizar ahora") {
                val appContext=applicationContext
                vm.synchronize { ContentSync.update(appContext) }
            }
            syncButton=(box.getChildAt(box.childCount-1) as Button).apply { isEnabled=!vm.ui.busy }
        }

        card { box ->
            addText(box, "Acerca del proyecto", 20f, ink, true)
            addText(box, "Tutor Virtual · DSY1105\nMVP académico basado en el caso ESUCAR. No es una aplicación oficial de Carabineros de Chile.\n\nSin servidor institucional, sin IA remota y sin envío de entregas. Las fechas iniciales son ejemplos relativos al primer uso; el docente puede editarlas. Al desinstalar se borran los datos locales.", 15f, muted)
        }
    }
