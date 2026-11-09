package cl.esucar.tutor.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.esucar.tutor.data.repository.AuthRepository
import cl.esucar.tutor.domain.validacion.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class LoginState(val email: String="",val name: String="",val password: String="",val confirmation: String="",
    val register: Boolean=false,val busy: Boolean=false,val errors: Map<String,String> = emptyMap(),val message: String="",val session: String?=null)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutable=MutableStateFlow(LoginState(session=repository.currentEmail))
    val state=mutable.asStateFlow()
    fun edit(key: String,value: String) {
        val s=state.value
        val error=when(key) { "email" -> AuthValidator.email(value); "password" -> AuthValidator.password(value); "name" -> FormValidator.validate(FieldKind.NAME,value); else -> if(value!=s.password) "Las contraseñas no coinciden." else null }
        mutable.value=when(key) { "email"->s.copy(email=value); "name"->s.copy(name=value); "password"->s.copy(password=value); else->s.copy(confirmation=value) }
            .copy(errors=if(error==null) s.errors-key else s.errors+(key to error),message="")
    }
    fun mode(register: Boolean) { if(!state.value.busy) mutable.value=LoginState(register=register,email=state.value.email) }
    fun submit() {
        val s=state.value; if(s.busy) return
        val errors=mutableMapOf<String,String>()
        AuthValidator.email(s.email)?.let { errors["email"]=it }
        AuthValidator.password(s.password)?.let { errors["password"]=it }
        if(s.register) {
            FormValidator.validate(FieldKind.NAME,s.name)?.let { errors["name"]=it }
            if(s.confirmation!=s.password) errors["confirmation"]="Las contraseñas no coinciden."
        }
        if(errors.isNotEmpty()) { mutable.value=s.copy(errors=errors); return }
        mutable.value=s.copy(busy=true,errors=emptyMap(),message="")
        viewModelScope.launch {
            val result=withContext(Dispatchers.IO) { runCatching { if(s.register) repository.register(s.name,s.email,s.password) else repository.login(s.email,s.password) } }
            mutable.value=if(result.isSuccess) LoginState(session=repository.currentEmail) else s.copy(password="",confirmation="",message=result.exceptionOrNull()?.message ?: "No se pudo ingresar.")
        }
    }
    fun logout() { if(!state.value.busy) { repository.logout(); mutable.value=LoginState() } }
}
