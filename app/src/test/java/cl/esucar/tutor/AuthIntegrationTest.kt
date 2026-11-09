package cl.esucar.tutor

import android.content.Context
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import cl.esucar.tutor.data.repository.AuthRepository
import cl.esucar.tutor.data.repository.Repository
import cl.esucar.tutor.data.local.Catalog
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class AuthIntegrationTest {
    private val context get()=RuntimeEnvironment.getApplication()
    @Before fun reset() {
        context.getSharedPreferences("auth_v1",Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("tutor_v1",Context.MODE_PRIVATE).edit().clear().commit()
    }
    private fun idle()=Shadows.shadowOf(Looper.getMainLooper()).idle()
    private fun children(v: View): List<View> = listOf(v)+if(v is ViewGroup) (0 until v.childCount).flatMap { children(v.getChildAt(it)) } else emptyList()
    @Test fun freshLaunchShowsLoginAndNoAcademicNavigation() {
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup(); idle()
        val buttons=children(controller.get().window.decorView).filterIsInstance<Button>().map { it.text.toString() }
        assertTrue("Ingresar" in buttons); assertFalse("Guía" in buttons)
        controller.pause().stop().destroy()
    }
    @Test fun loginFormShowsErrorsForEmptyFields() {
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup(); idle()
        children(controller.get().window.decorView).filterIsInstance<Button>().first { it.text=="Ingresar" }.performClick(); idle()
        val fields=children(controller.get().window.decorView).filterIsInstance<EditText>()
        assertNotNull(fields.first { it.contentDescription=="Correo electrónico" }.error)
        assertNotNull(fields.first { it.contentDescription=="Contraseña" }.error)
        controller.pause().stop().destroy()
    }
    @Test fun registrationPersistsSessionAndNeverStoresPlainPassword() {
        val auth=AuthRepository(context)
        auth.register("Ana Pérez"," ANA@example.org ","ClaveSegura123")
        assertEquals("ana@example.org",AuthRepository(context).currentEmail)
        assertFalse(context.getSharedPreferences("auth_v1",Context.MODE_PRIVATE).all.values.any { it.toString().contains("ClaveSegura123") })
        auth.logout(); assertNull(auth.currentEmail)
        assertTrue(runCatching { auth.login("ana@example.org","incorrecta") }.isFailure)
        assertNull(auth.currentEmail)
        auth.login("ANA@example.org","ClaveSegura123"); assertEquals("ana@example.org",auth.currentEmail)
    }
    @Test fun duplicateRegistrationDoesNotReplacePassword() {
        val auth=AuthRepository(context); auth.register("Ana","ana@example.org","ClaveSegura123")
        assertTrue(runCatching { auth.register("Otra","ANA@example.org","NuevaClave123") }.isFailure)
        auth.logout(); auth.login("ana@example.org","ClaveSegura123")
    }
    @Test fun accountsKeepSeparateProgressAndFirstAccountMigratesLegacyData() {
        val repo=Repository(context); val stage=Catalog.stages.first()
        repo.save(stage,repo.progress(stage).copy(notes="Avance anterior"))
        val auth=AuthRepository(context); auth.register("Ana","ana@example.org","ClaveSegura123")
        assertEquals("Avance anterior",repo.progress(stage).notes)
        repo.save(stage,repo.progress(stage).copy(notes="Solo Ana")); auth.logout()
        auth.register("Luis","luis@example.org","ClaveSegura123")
        assertEquals("",repo.progress(stage).notes)
        auth.logout(); auth.login("ana@example.org","ClaveSegura123")
        assertEquals("Solo Ana",repo.progress(stage).notes)
    }
    @Test fun registrationUiEntersHomeAndLogoutReturnsToLogin() {
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup(); idle()
        fun views()=children(controller.get().window.decorView)
        fun click(title: String) { views().filterIsInstance<Button>().first { it.text==title }.performClick(); idle() }
        fun fill(title: String,value: String) { views().filterIsInstance<EditText>().first { it.contentDescription==title }.setText(value); idle() }
        click("Primera vez · Crear cuenta")
        fill("Nombre completo","Ana Pérez"); fill("Correo electrónico","ana@example.org")
        fill("Contraseña","ClaveSegura123"); fill("Confirmar contraseña","ClaveSegura123")
        click("Crear cuenta")
        val deadline=System.nanoTime()+10_000_000_000L
        while(AuthRepository(context).currentEmail==null && System.nanoTime()<deadline) { Thread.sleep(20); idle() }
        Thread.sleep(100); idle()
        assertTrue(views().filterIsInstance<Button>().any { it.text=="Guía" })
        click("Ajustes"); click("Cerrar sesión")
        org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick(); idle()
        assertNull(AuthRepository(context).currentEmail)
        assertTrue(views().filterIsInstance<Button>().any { it.text=="Ingresar" })
        controller.pause().stop().destroy()
    }
}
