package cl.esucar.tutor.ui.profesor

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

internal fun TutorScreenHost.teacherPanel(stage: Stage,p: Progress) {
            card { box ->
                addText(box, "Gestión docente · demo", 20f, green, true)
                val date=input(box,"Fecha límite (AAAA-MM-DD)",p.date.toString(),false,"date_${stage.id}",FieldKind.DATE,"Fecha existente · formato año-mes-día")
                date.inputType=android.text.InputType.TYPE_CLASS_DATETIME or android.text.InputType.TYPE_DATETIME_VARIATION_DATE
                button(box,"Elegir fecha") {
                    DatePickerDialog(this,{ _,year,month,day -> date.setText(LocalDate.of(year,month+1,day).toString()) },p.date.year,p.date.monthValue-1,p.date.dayOfMonth).show()
                }
                button(box,"Guardar fecha") { if(vm.changeDate(stage)) Reminders.schedule(this) else focusFirstError() }
                input(box,"Formato requerido",vm.format(stage),true,"format_${stage.id}",FieldKind.FORMAT,"Obligatorio · de 10 a 2000 caracteres")
                input(box,"Criterios (uno por línea)",vm.rubric(stage),true,"rubric_${stage.id}",FieldKind.RUBRIC,"De 1 a 12 criterios · de 5 a 200 caracteres por línea")
                button(box,"Guardar pauta") { if(!vm.saveGuidelines(stage)) focusFirstError() }
                val feedback=input(box,"Retroalimentación docente",p.feedback,true,"feedback_${stage.id}",FieldKind.FEEDBACK,"Máximo 2000 caracteres · al devolver se requieren al menos 10")
                watch(feedback) { vm.saveFeedback(stage,it) }
                if (p.status == DeliveryStatus.SUBMITTED) {
                    button(box, "Aprobar hito", true) { transition(stage, DeliveryStatus.APPROVED) }
                    button(box, "Solicitar correcciones") {
                        transition(stage,DeliveryStatus.IN_PROGRESS)
                    }
                } else addText(box, "La revisión se habilita cuando el estudiante marca el hito como enviado.", 14f, muted)
            }
    }
