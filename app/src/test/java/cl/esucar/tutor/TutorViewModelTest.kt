package cl.esucar.tutor

import cl.esucar.tutor.data.local.Catalog
import cl.esucar.tutor.domain.model.*
import cl.esucar.tutor.domain.validacion.*
import cl.esucar.tutor.domain.repository.*
import cl.esucar.tutor.ui.navegacion.*
import cl.esucar.tutor.ui.components.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class MemoryStore : TutorStore {
    override var role=Role.STUDENT
    override var name="Estudiante"
    override var endpoint=""
    override var reminders=false
    override val syncTime="Sin actualizaciones"
    private val progresses=mutableMapOf<String,Progress>()
    private val formats=mutableMapOf<String,String>()
    private val rubrics=mutableMapOf<String,String>()
    override fun progress(stage: Stage)=progresses.getOrPut(stage.id) { Progress(date=LocalDate.of(2026,12,1)) }
    override fun save(stage: Stage,progress: Progress) { progresses[stage.id]=progress }
    override fun format(stage: Stage)=formats[stage.id] ?: stage.format
    override fun rubric(stage: Stage)=rubrics[stage.id] ?: stage.rubric.joinToString("\n")
    override fun setGuidelines(stage: Stage,format: String,rubric: String) { formats[stage.id]=format; rubrics[stage.id]=rubric }
    override fun faqs()=Catalog.faqs
}

class TutorViewModelTest {
    private val store=MemoryStore()
    private val vm=TutorViewModel(store)
    private val stage=Catalog.stages.first()
    @Test fun invalidProfileNeverPersistsAndPublishesFieldError() {
        vm.edit("name",FieldKind.NAME,"")
        assertFalse(vm.saveName())
        assertEquals("Estudiante",store.name)
        assertTrue(vm.ui.errors.containsKey("name"))
        vm.edit("name",FieldKind.NAME,"Ana Pérez")
        assertTrue(vm.saveName())
        assertEquals("Ana Pérez",store.name)
        assertFalse(vm.ui.errors.containsKey("name"))
    }
    @Test fun incompleteChecklistCannotBeSubmitted() {
        assertTrue(vm.transition(stage,DeliveryStatus.IN_PROGRESS))
        assertFalse(vm.transition(stage,DeliveryStatus.SUBMITTED))
        assertTrue(vm.ui.errors.containsKey("delivery"))
        stage.steps.indices.forEach { vm.check(stage,it,true) }
        assertTrue(vm.transition(stage,DeliveryStatus.SUBMITTED))
    }
    @Test fun submittedWorkCannotBeMutatedByStudent() {
        store.save(stage,store.progress(stage).copy(status=DeliveryStatus.SUBMITTED))
        vm.saveNotes(stage,"Cambiar después del envío")
        vm.check(stage,0,true)
        assertEquals("",store.progress(stage).notes)
        assertTrue(store.progress(stage).checked.isEmpty())
        assertFalse(vm.attach(stage,"content://documents/42"))
    }
    @Test fun correctionRequiresExplanationAndUpdatesProgress() {
        store.save(stage,store.progress(stage).copy(status=DeliveryStatus.SUBMITTED))
        vm.switchRole()
        assertFalse(vm.transition(stage,DeliveryStatus.IN_PROGRESS))
        assertTrue(vm.ui.errors.containsKey("feedback_${stage.id}"))
        vm.saveFeedback(stage,"Delimita la población y el período de estudio.")
        assertTrue(vm.transition(stage,DeliveryStatus.IN_PROGRESS))
        assertEquals(DeliveryStatus.IN_PROGRESS,store.progress(stage).status)
    }
    @Test fun invalidDraftSurvivesNavigationWithoutOverwritingValidData() {
        vm.saveNotes(stage,"Nota válida")
        vm.saveNotes(stage,"x".repeat(2001))
        vm.navigate("Ajustes"); vm.navigate("Guía",stage.id)
        assertEquals(2001,vm.draft("notes_${stage.id}","").length)
        assertEquals("Nota válida",store.progress(stage).notes)
    }
    @Test fun stateCanBeRestoredAfterProcessRecreation() {
        vm.navigate("Guía",stage.id)
        vm.edit("name",FieldKind.NAME,"Nombre sin guardar")
        val recreated=TutorViewModel(store)
        recreated.restore(vm.ui)
        assertEquals(stage.id,recreated.ui.selected)
        assertEquals("Nombre sin guardar",recreated.draft("name",""))
    }
    @Test fun dateAndRubricRequireTeacherAndValidFields() {
        vm.edit("date_${stage.id}",FieldKind.DATE,"2026-12-20")
        assertFalse(vm.changeDate(stage))
        vm.switchRole()
        assertTrue(vm.changeDate(stage))
        assertEquals(LocalDate.of(2026,12,20),store.progress(stage).date)
        vm.edit("rubric_${stage.id}",FieldKind.RUBRIC,"")
        assertFalse(vm.saveGuidelines(stage))
    }
    @Test fun approvalReflectsInDashboardAndPersistence() {
        store.save(stage,store.progress(stage).copy(status=DeliveryStatus.SUBMITTED))
        vm.switchRole()
        assertTrue(vm.transition(stage,DeliveryStatus.APPROVED))
        assertEquals(1,vm.approved)
        assertEquals(1,TutorViewModel(store).approved)
    }
    @Test fun deniedNotificationPermissionDisablesPreference() {
        vm.setReminders(true)
        vm.permissionResult(false)
        assertFalse(store.reminders)
    }
    @Test fun backReturnsToListThenHome() {
        vm.navigate("Guía",stage.id)
        assertTrue(vm.back()); assertEquals("Guía",vm.ui.page); assertNull(vm.ui.selected)
        assertTrue(vm.back()); assertEquals("Inicio",vm.ui.page)
        assertFalse(vm.back())
    }
}
