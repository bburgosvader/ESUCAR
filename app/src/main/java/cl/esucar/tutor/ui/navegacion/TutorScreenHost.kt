package cl.esucar.tutor.ui.navegacion

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

open class TutorScreenHost : ComponentActivity() {
    internal lateinit var vm: TutorViewModel
    internal lateinit var content: LinearLayout
    internal val page get() = vm.ui.page
    internal val selected get() = vm.ui.selected
    internal val course get() = vm.ui.course
    internal val query get() = vm.ui.query
    internal val category get() = vm.ui.category
    internal var pickingStage: String? = null
    internal val fields = mutableMapOf<String, ValidatedField>()
    internal var revision = -1
    internal var lastMessage = 0
    internal var syncStatus: TextView? = null
    internal var syncSpinner: ProgressBar? = null
    internal var syncButton: Button? = null
    internal var deliveryError: TextView? = null
    internal var checklistLabel: TextView? = null
    internal var checklistProgress: ProgressBar? = null
    internal val documentPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val stage=vm.stages.find { it.id==pickingStage }
        if(uri!=null && stage!=null) {
            try {
                contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
                vm.attach(stage,uri.toString())
            } catch (_: SecurityException) { vm.message("No se pudo conservar acceso. Selecciona otro documento.") }
        }
        pickingStage=null
    }
    internal val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.permissionResult(granted); Reminders.schedule(this)
    }
    internal val green = TutorTheme.green
    internal val ink = TutorTheme.ink
    internal val muted = TutorTheme.muted
    internal val pale = TutorTheme.pale
    internal val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this,(application as cl.esucar.tutor.TutorApp).container.viewModelFactory)[TutorViewModel::class.java]
        savedInstanceState?.let { saved ->
            fun strings(key: String): Map<String,String> = saved.getBundle(key)?.let { b -> b.keySet().associateWith { b.getString(it).orEmpty() } } ?: emptyMap()
            vm.restore(TutorUiState(page=saved.getString("page","Inicio"),selected=saved.getString("selected"),
                course=saved.getInt("course"),query=saved.getString("query",""),category=saved.getString("category","Todas"),
                drafts=strings("drafts"),errors=strings("errors"),touched=saved.getStringArrayList("touched")?.toSet() ?: emptySet()))
            pickingStage=saved.getString("picking")
        }
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if(!vm.back()) { isEnabled=false; onBackPressedDispatcher.onBackPressed() } }
        })
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        Reminders.schedule(this)
        ContentSyncService.schedule(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    if(revision!=state.revision) { revision=state.revision; render() }
                    fields.forEach { (key,field) -> field.showResult(state.errors[key],key in state.touched) }
                    deliveryError?.text=state.errors["delivery"].orEmpty()
                    syncStatus?.text=state.syncMessage
                    syncSpinner?.visibility=if(state.busy) View.VISIBLE else View.GONE
                    syncButton?.isEnabled=!state.busy
                    vm.ui.selected?.let { id -> vm.stages.find { it.id==id }?.let { stage ->
                        val done=vm.progress(stage).checked.size
                        checklistLabel?.text="Lista completada: $done/${stage.steps.size}"
                        checklistProgress?.let { UiAnimations.progress(it,done) }
                    } }
                    if(lastMessage!=state.messageId) { lastMessage=state.messageId; if(state.message.isNotBlank()) toast(state.message) }
                }
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("page", page); outState.putString("selected", selected)
        outState.putInt("course", course); outState.putString("query", query); outState.putString("category", category)
        outState.putString("picking", pickingStage)
        outState.putBundle("drafts",Bundle().apply { vm.ui.drafts.forEach { (key,value) -> putString(key,value) } })
        outState.putBundle("errors",Bundle().apply { vm.ui.errors.forEach { (key,value) -> putString(key,value) } })
        outState.putStringArrayList("touched",ArrayList(vm.ui.touched))
    }
}
