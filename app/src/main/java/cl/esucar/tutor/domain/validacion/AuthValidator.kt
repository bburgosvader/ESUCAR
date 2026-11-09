package cl.esucar.tutor.domain.validacion

object AuthValidator {
    fun email(value: String): String? = if(value.trim().length <= 254 && value.trim().matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) null else "Escribe un correo válido."
    fun password(value: String): String? = if(value.length in 8..128) null else "Usa una contraseña de 8 a 128 caracteres."
}
