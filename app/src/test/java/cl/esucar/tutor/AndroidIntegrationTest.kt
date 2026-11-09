package cl.esucar.tutor

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.provider.CalendarContract
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import cl.esucar.tutor.data.local.Catalog
import cl.esucar.tutor.data.repository.Repository
import cl.esucar.tutor.nativo.DeviceIntents
import cl.esucar.tutor.nativo.Reminders
import cl.esucar.tutor.domain.model.DeliveryStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import java.time.LocalDate

/** Integración en Android simulado. No reemplaza pruebas de hardware ni de servicios de terceros. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class AndroidIntegrationTest {
    private val context get()=RuntimeEnvironment.getApplication()
    @Before fun resetDemoData() {
        context.getSharedPreferences("auth_v1",Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("tutor_v1",Context.MODE_PRIVATE).edit().clear().commit()
        cl.esucar.tutor.data.repository.AuthRepository(context).register("Estudiante","fixture@example.org","Test12345!")
    }
    private fun idle() { shadowOf(Looper.getMainLooper()).idle() }
    private fun descendants(view: View): List<View> = listOf(view) + if(view is ViewGroup) (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) } else emptyList()
    private fun views(activity: MainActivity)=descendants(activity.window.decorView)
    private fun click(activity: MainActivity,text: String) { views(activity).filterIsInstance<Button>().first { it.text.toString()==text }.performClick(); idle() }
    private fun field(activity: MainActivity,title: String)=views(activity).filterIsInstance<EditText>().first { it.contentDescription.toString()==title }

    @Test fun profileErrorsAppearBesideFieldAndValidDataPersists() {
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity=controller.get(); idle(); click(activity,"Ajustes")
        field(activity,"Nombre para mostrar").setText(""); idle(); click(activity,"Guardar nombre")
        assertNotNull(field(activity,"Nombre para mostrar").error)
        assertEquals("Estudiante",Repository(context).name)
        field(activity,"Nombre para mostrar").setText("Ana Pérez"); idle(); click(activity,"Guardar nombre")
        assertNull(field(activity,"Nombre para mostrar").error)
        assertEquals("Ana Pérez",Repository(context).name)
        controller.pause().stop().destroy()
    }
    @Test fun draftAndPageSurviveActivityRecreation() {
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup()
        idle(); click(controller.get(),"Ajustes")
        field(controller.get(),"Nombre para mostrar").setText("Borrador sin guardar"); idle()
        controller.recreate(); idle()
        assertEquals("Borrador sin guardar",field(controller.get(),"Nombre para mostrar").text.toString())
        assertEquals("Estudiante",Repository(context).name)
        controller.pause().stop().destroy()
    }
    @Test fun deliveryFlowRequiresChecklistAndTeacherApproval() {
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity=controller.get(); idle(); click(activity,"Abrir etapa"); click(activity,"Comenzar esta etapa")
        views(activity).filterIsInstance<CheckBox>().forEach { it.performClick() }; idle()
        click(activity,"Marcar como enviado")
        ShadowAlertDialog.getLatestAlertDialog().getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick(); idle()
        assertEquals(DeliveryStatus.SUBMITTED,Repository(context).progress(Catalog.stages.first()).status)
        click(activity,"Ajustes"); click(activity,"Cambiar a docente (demo)")
        click(activity,"Inicio"); click(activity,"Abrir etapa"); click(activity,"Aprobar hito")
        assertEquals(DeliveryStatus.APPROVED,Repository(context).progress(Catalog.stages.first()).status)
        controller.pause().stop().destroy()
    }
    @Test fun repositoryKeepsProgressAndLastGoodRemoteContent() {
        val repo=Repository(context); val stage=Catalog.stages.first()
        repo.save(stage,repo.progress(stage).copy(notes="Evidencia persistida",checked=setOf(0,2)))
        assertEquals("Evidencia persistida",Repository(context).progress(stage).notes)
        repo.cacheFaqs("""{"faqs":[{"question":"Pregunta de prueba","category":"Pruebas","answer":"Respuesta válida"}]}""")
        assertTrue(runCatching { repo.cacheFaqs("{malformado}") }.isFailure)
        assertTrue(Repository(context).faqs().any { it.question=="Pregunta de prueba" })
    }
    @Test fun notificationUsesChannelAndDoesNotCrashWhenPermissionIsDenied() {
        shadowOf(context).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        Reminders.show(context,41,"Prueba","Sin permiso")
        val manager=context.getSystemService(NotificationManager::class.java)
        assertNull(shadowOf(manager).getNotification(41))
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        Reminders.show(context,42,"Entrega","Revisa tu avance")
        assertNotNull(shadowOf(manager).getNotification(42))
        assertNotNull(manager.getNotificationChannel("entregas"))
    }
    @Test fun nativeIntentsGrantOnlyReadAccessAndLetUserConfirmCalendarEvent() {
        val document=DeviceIntents.openDocument("content://provider/archivo.pdf")
        assertEquals(Intent.ACTION_VIEW,document.action)
        assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION,document.flags)
        val calendar=DeviceIntents.calendar(Catalog.stages.first(),LocalDate.of(2026,12,1))
        assertEquals(Intent.ACTION_INSERT,calendar.action)
        assertEquals(CalendarContract.Events.CONTENT_URI,calendar.data)
        assertTrue(calendar.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME,0)>calendar.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,0))
    }
}

