package cl.esucar.tutor.ui.navegacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.esucar.tutor.data.local.Catalog
import cl.esucar.tutor.domain.model.*
import cl.esucar.tutor.domain.validacion.*
import cl.esucar.tutor.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class TutorUiState(
    val page: String = "Inicio", val selected: String? = null,
    val course: Int = 0, val query: String = "", val category: String = "Todas",
    val drafts: Map<String, String> = emptyMap(), val errors: Map<String, String> = emptyMap(),
    val touched: Set<String> = emptySet(), val revision: Int = 0,
    val message: String = "", val messageId: Int = 0, val busy: Boolean = false,
    val syncMessage: String = "La actualización automática requiere una fuente HTTPS y conexión."
)

class TutorViewModel(private val store: TutorStore) : ViewModel() {
    private val mutableState = MutableStateFlow(TutorUiState())
    val state: StateFlow<TutorUiState> = mutableState.asStateFlow()
    val ui: TutorUiState get() = state.value
    private fun update(transform: (TutorUiState) -> TutorUiState) { mutableState.value = transform(ui) }
    private fun refresh() = update { it.copy(revision = it.revision + 1) }
    fun message(text: String) = update { it.copy(message = text, messageId = it.messageId + 1) }
    val role get() = store.role
    val name get() = store.name
    val endpoint get() = store.endpoint
    val reminders get() = store.reminders
    val syncTime get() = store.syncTime
    val stages get() = Catalog.stages
    val approved get() = stages.count { progress(it).status == DeliveryStatus.APPROVED }
    val overdue get() = stages.count { Rules.overdue(progress(it), LocalDate.now()) }
    val next get() = stages.filter { progress(it).status != DeliveryStatus.APPROVED }.minByOrNull { progress(it).date }
    fun progress(stage: Stage) = store.progress(stage)
    fun format(stage: Stage) = store.format(stage)
    fun rubric(stage: Stage) = store.rubric(stage)
    fun faqs() = store.faqs()
    fun searchResults() = if (ui.errors.containsKey("search")) emptyList() else faqs().filter { Rules.matches(it, ui.query, ui.category) }
    fun navigate(page: String, selected: String? = null) = update { it.copy(page=page,selected=selected,revision=it.revision+1) }
    fun back(): Boolean {
        if (ui.selected != null) navigate("Guía") else if (ui.page != "Inicio") navigate("Inicio") else return false
        return true
    }
    fun setCourse(course: Int) = update { it.copy(course=course) }
    fun setCategory(category: String) = update { it.copy(category=category) }
    fun restore(saved: TutorUiState) { if (ui.revision==0 && ui.drafts.isEmpty()) mutableState.value=saved }
    fun draft(key: String, fallback: String) = ui.drafts[key] ?: fallback
    fun edit(key: String, kind: FieldKind, text: String): Boolean {
        val error=FormValidator.validate(kind,text)
        update { it.copy(drafts=it.drafts+(key to text), touched=it.touched+key,
            errors=if(error==null) it.errors-key else it.errors+(key to error)) }
        return error==null
    }
    fun setQuery(query: String) { edit("search",FieldKind.SEARCH,query); update { it.copy(query=query) } }
    fun saveName(): Boolean {
        val value=draft("name",name)
        if (!edit("name",FieldKind.NAME,value)) return false
        store.name=value.trim(); message("Nombre guardado"); return true
    }
    fun switchRole() { store.role=if(role==Role.STUDENT) Role.TEACHER else Role.STUDENT; refresh() }
    private fun editable(stage: Stage)=role==Role.STUDENT && progress(stage).status in listOf(DeliveryStatus.NOT_STARTED,DeliveryStatus.IN_PROGRESS)
    fun check(stage: Stage,index: Int,checked: Boolean) {
        if(!editable(stage) || index !in stage.steps.indices) return
        val current=progress(stage)
        store.save(stage,current.copy(checked=if(checked) current.checked+index else current.checked-index))
        update { it.copy(errors=it.errors-"delivery") }
    }
    fun saveNotes(stage: Stage,text: String) {
        if(editable(stage) && edit("notes_${stage.id}",FieldKind.NOTES,text)) store.save(stage,progress(stage).copy(notes=text))
    }
    fun saveFeedback(stage: Stage,text: String) {
        if(role==Role.TEACHER && edit("feedback_${stage.id}",FieldKind.FEEDBACK,text)) store.save(stage,progress(stage).copy(feedback=text))
    }
    fun saveGuidelines(stage: Stage): Boolean {
        if(role!=Role.TEACHER) return false
        val format=draft("format_${stage.id}",format(stage)); val rubric=draft("rubric_${stage.id}",rubric(stage))
        val a=edit("format_${stage.id}",FieldKind.FORMAT,format)
        val b=edit("rubric_${stage.id}",FieldKind.RUBRIC,rubric)
        if(!a || !b) return false
        store.setGuidelines(stage,format.trim(),rubric.trim()); message("Pauta guardada"); refresh(); return true
    }
    fun changeDate(stage: Stage): Boolean {
        if(role!=Role.TEACHER) return false
        val value=draft("date_${stage.id}",progress(stage).date.toString())
        if(!edit("date_${stage.id}",FieldKind.DATE,value)) return false
        store.save(stage,progress(stage).copy(date=LocalDate.parse(value.trim())))
        message("Fecha guardada"); refresh(); return true
    }
    fun transition(stage: Stage,target: DeliveryStatus): Boolean {
        val notes=draft("notes_${stage.id}",progress(stage).notes)
        val feedback=draft("feedback_${stage.id}",progress(stage).feedback)
        val valid=if(role==Role.STUDENT) edit("notes_${stage.id}",FieldKind.NOTES,notes)
            else edit("feedback_${stage.id}",FieldKind.FEEDBACK,feedback)
        if(!valid) return false
        val error=FormValidator.transition(role,progress(stage),target,stage,feedback)
        if(error!=null) {
            val key=if(role==Role.TEACHER && target==DeliveryStatus.IN_PROGRESS) "feedback_${stage.id}" else "delivery"
            update { it.copy(errors=it.errors+(key to error), touched=it.touched+key) }
            message(error); return false
        }
        store.save(stage,progress(stage).copy(status=target))
        update { it.copy(errors=it.errors-"delivery") }; message("Estado actualizado: ${target.label}"); refresh(); return true
    }
    fun attach(stage: Stage,uri: String): Boolean {
        if(!editable(stage) || !uri.startsWith("content://")) { message("No se puede vincular el documento a este hito."); return false }
        store.save(stage,progress(stage).copy(attachment=uri)); message("Documento vinculado en este dispositivo"); refresh(); return true
    }
    fun saveEndpoint(): Boolean {
        val value=draft("endpoint",endpoint)
        if(!edit("endpoint",FieldKind.ENDPOINT,value)) return false
        if(ui.busy) { message("Espera a que termine la actualización actual."); return false }
        store.endpoint=value.trim(); message("Fuente guardada"); refresh(); return true
    }
    fun setReminders(enabled: Boolean) { store.reminders=enabled }
    fun permissionResult(granted: Boolean) { if(!granted) { store.reminders=false; message("Permiso denegado: recordatorios desactivados.") }; refresh() }
    fun synchronize(download: () -> String) {
        if(ui.busy) return
        if(endpoint.isBlank()) { edit("endpoint",FieldKind.ENDPOINT,draft("endpoint",endpoint)); message("Guarda primero una fuente HTTPS."); return }
        update { it.copy(busy=true,syncMessage="Consultando contenidos…") }
        viewModelScope.launch {
            val result=withContext(Dispatchers.IO) { runCatching(download) }
            val text=result.getOrElse { "No se pudo actualizar. Se conserva la biblioteca local. Revisa la conexión y la URL." }
            update { it.copy(busy=false,syncMessage=text) }
        }
    }
}
