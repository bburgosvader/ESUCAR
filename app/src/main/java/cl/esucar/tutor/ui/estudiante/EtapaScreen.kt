package cl.esucar.tutor.ui.estudiante

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

internal fun TutorScreenHost.detail(stage: Stage) {
        val p = vm.progress(stage)
        val student = vm.role == Role.STUDENT
        val editable = student && p.status in listOf(DeliveryStatus.NOT_STARTED, DeliveryStatus.IN_PROGRESS)
        button(content, "‹ Volver a las etapas") { vm.navigate("Guía") }
        addText(content, "TRABAJO DE TÍTULO ${stage.course}", 12f, green, true)
        addText(content, stage.title, 28f, ink, true)
        addText(content, "${p.status.label} · ${p.date.format(dateFormat)}", 16f, green, true)
        button(content,"Agregar fecha a mi calendario") {
            try { startActivity(DeviceIntents.calendar(stage,p.date)) }
            catch (_: android.content.ActivityNotFoundException) { vm.message("Instala o habilita una aplicación de calendario para agregar esta fecha.") }
        }
        addText(content,"El calendario abrirá un borrador. Revisa y guarda el evento allí.",13f,muted)
        card { box ->
            addText(box, "01  /  Orientación", 20f, ink, true)
            addText(box, stage.description)
            stage.steps.forEachIndexed { i, step -> addText(box, "${i + 1}. $step", 16f) }
        }
        card { box ->
            addText(box, "02  /  Formato y pauta", 20f, ink, true)
            addText(box, vm.format(stage))
            addText(box, "Criterios de revisión", 17f, green, true)
            vm.rubric(stage).lines().forEach { addText(box, "• $it", 16f) }
            addText(box, "Pauta orientativa, sin ponderaciones oficiales.", 13f, muted)
        }
        card { box ->
            addText(box, "03  /  Lista de avance", 20f, ink, true)
            stage.steps.forEachIndexed { i, step ->
                box.addView(com.google.android.material.checkbox.MaterialCheckBox(this).apply {
                    text = step; textSize = 16f; setTextColor(ink); minHeight = dp(48); isChecked = i in p.checked; isEnabled = editable
                    setOnCheckedChangeListener { _, checked ->
                        vm.check(stage,i,checked)
                        checklistProgress?.let { UiAnimations.progress(it,vm.progress(stage).checked.size) }
                        checklistLabel?.text="Lista completada: ${vm.progress(stage).checked.size}/${stage.steps.size}"
                    }
                })
            }
            checklistLabel=label("Lista completada: ${p.checked.size}/${stage.steps.size}",14f,green,true).also { box.addView(it) }
            checklistProgress=com.google.android.material.progressindicator.LinearProgressIndicator(this).apply { max=stage.steps.size; progress=p.checked.size }.also { box.addView(it) }
            deliveryError=label(vm.ui.errors["delivery"].orEmpty(),14f,Color.rgb(160,32,32)).also { it.accessibilityLiveRegion=View.ACCESSIBILITY_LIVE_REGION_POLITE; box.addView(it) }
            val notes = input(box,"Notas de trabajo",p.notes,true,"notes_${stage.id}",FieldKind.NOTES,"Opcional · máximo 2000 caracteres · guardado automático si es válido")
            notes.isEnabled = editable
            watch(notes) { vm.saveNotes(stage,it) }
            addText(box, if (p.attachment.isBlank()) "Sin documento vinculado." else "Documento vinculado en este dispositivo.", 14f, muted)
            if (editable) button(box, "Vincular documento") {
                pickingStage = stage.id
                try { documentPicker.launch(arrayOf("application/pdf","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","text/plain")) }
                catch (_: android.content.ActivityNotFoundException) { vm.message("No hay un selector de documentos disponible en este dispositivo.") }
            }
            if (p.attachment.isNotBlank()) button(box, "Abrir documento vinculado") {
                try { startActivity(DeviceIntents.openDocument(p.attachment)) }
                catch (_: Exception) { toast("No se pudo abrir. Comprueba que el archivo existe y vuelve a vincularlo.") }
            }
        }
        if (p.feedback.isNotBlank()) card(tint = pale) { addText(it, "Retroalimentación docente", 19f, green, true); addText(it, p.feedback) }
        if (student) {
            when (p.status) {
                DeliveryStatus.NOT_STARTED -> button(content, "Comenzar esta etapa", true) { transition(stage, DeliveryStatus.IN_PROGRESS) }
                DeliveryStatus.IN_PROGRESS -> button(content, "Marcar como enviado", true) {
                    AlertDialog.Builder(this).setTitle("Registrar entrega local")
                        .setMessage("Se guardará el estado para revisión en este dispositivo. No se enviará ningún archivo por internet. Usa también el canal de entrega indicado por tu docente.")
                        .setNegativeButton("Cancelar", null).setPositiveButton("Registrar") { _, _ -> transition(stage, DeliveryStatus.SUBMITTED) }.show()
                }
                DeliveryStatus.SUBMITTED -> addText(content, "Pendiente de revisión docente en este dispositivo.", 16f, green)
                DeliveryStatus.APPROVED -> addText(content, "Etapa aprobada. Continúa con el siguiente hito.", 16f, green)
            }
        } else {
            teacherPanel(stage,p)
        }
    }
