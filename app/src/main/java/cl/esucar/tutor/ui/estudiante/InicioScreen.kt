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

internal fun TutorScreenHost.home() {
        addText(content, "Hola, ${vm.name}", 28f, ink, true)
        addText(content, "Cada avance cuenta. Continúa con tu próximo hito.", 16f, muted)
        val approved = vm.approved
        card(tint = green) { box ->
            addText(box, "MI RUTA ACADÉMICA", 12f, Color.WHITE, true)
            addText(box, "$approved de ${vm.stages.size} hitos aprobados", 25f, Color.WHITE, true)
            box.addView(ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = vm.stages.size; UiAnimations.progress(this,approved)
                progressTintList = android.content.res.ColorStateList.valueOf(Color.rgb(204, 230, 143))
                contentDescription = "$approved de ${vm.stages.size} hitos aprobados"
            }, LinearLayout.LayoutParams(-1, dp(14)))
            addText(box, "Trabajo de Título I · II · III", 14f, Color.WHITE)
        }
        val next = vm.next
        addText(content, "Próximo paso", 21f, ink, true)
        if (next == null) card { addText(it, "¡Ruta completada! Todos tus hitos están aprobados.", 18f, green, true) }
        else stageCard(next)
        val late = vm.overdue
        if (late > 0) card(tint = Color.rgb(255, 236, 220)) { addText(it, "$late hito(s) con fecha vencida. Revisa tu planificación con el docente.", 16f) }
        button(content, "Explorar guía metodológica", true) { navigate("Guía") }
        button(content, "Resolver una duda") { navigate("Consultas") }
        addText(content, "PROTOTIPO ACADÉMICO · Sin conexión", 12f, muted, true)
        addText(content, "Contenido y fechas de ejemplo. Requieren validación del docente. Los datos se guardan en este dispositivo.", 14f, muted)
    }

internal fun TutorScreenHost.stageCard(stage: Stage) {
        val p = vm.progress(stage)
        card { box ->
            addText(box, "TRABAJO DE TÍTULO ${stage.course}  •  ${p.status.label}", 12f, green, true)
            addText(box, stage.title, 21f, ink, true)
            addText(box, stage.description, 15f, muted)
            addText(box, "Entrega: ${p.date.format(dateFormat)}${if (Rules.overdue(p, LocalDate.now())) " · Vencido" else ""}", 14f, ink, true)
            addText(box, "Lista de avance: ${p.checked.size}/${stage.steps.size}", 14f, muted)
            button(box, "Abrir etapa") { vm.navigate("Guía",stage.id) }
        }
    }
