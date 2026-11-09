package cl.esucar.tutor

import android.app.Application
import cl.esucar.tutor.di.AppContainer

class TutorApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
