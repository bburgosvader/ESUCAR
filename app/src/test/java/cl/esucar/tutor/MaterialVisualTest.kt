package cl.esucar.tutor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import cl.esucar.tutor.data.repository.AuthRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Duration

/** Exporta capturas del renderizador Android para revisar tipografía, colores y proporciones. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w393dp-h851dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MaterialVisualTest {
    @Test fun renderLoginAndDashboard() {
        val context=RuntimeEnvironment.getApplication()
        context.getSharedPreferences("auth_v1",Context.MODE_PRIVATE).edit().clear().commit()
        fun capture(name: String) {
            val controller=Robolectric.buildActivity(MainActivity::class.java).setup().visible()
            val view=controller.get().window.decorView
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(700))
            view.measure(View.MeasureSpec.makeMeasureSpec(786,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1702,View.MeasureSpec.EXACTLY))
            view.layout(0,0,786,1702)
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300))
            val bitmap=Bitmap.createBitmap(786,1702,Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val folder=File("build/visual-review").apply { mkdirs() }
            File(folder,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            controller.pause().stop().destroy()
        }
        capture("login-material3")
        AuthRepository(context).register("Estudiante","visual@example.org","PruebaVisual123")
        capture("inicio-material3")
    }
}
