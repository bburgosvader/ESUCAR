package cl.esucar.tutor.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.esucar.tutor.data.repository.AuthRepository
import cl.esucar.tutor.ui.auth.LoginViewModel
import cl.esucar.tutor.data.repository.Repository
import cl.esucar.tutor.domain.repository.TutorStore
import cl.esucar.tutor.ui.navegacion.TutorViewModel

/** Composición manual de dependencias; usa el contexto de aplicación. */
class AppContainer(context: Context) {
    val authRepository by lazy { AuthRepository(context.applicationContext) }
    val tutorStore: TutorStore by lazy { Repository(context.applicationContext) }
    val viewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(LoginViewModel::class.java)) return LoginViewModel(authRepository) as T
            require(modelClass.isAssignableFrom(TutorViewModel::class.java))
            return TutorViewModel(tutorStore) as T
        }
    }
}
