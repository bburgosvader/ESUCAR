package cl.esucar.tutor.data.repository

import android.content.Context
import cl.esucar.tutor.domain.seguridad.PasswordHasher
import cl.esucar.tutor.domain.validacion.AuthValidator
import cl.esucar.tutor.domain.validacion.FormValidator
import cl.esucar.tutor.domain.validacion.FieldKind
import java.util.Locale
import org.json.JSONObject

/** Cuentas locales del MVP; no valida identidad ni roles de la institución. */
class AuthRepository(private val context: Context) {
    private val prefs=context.getSharedPreferences("auth_v1",Context.MODE_PRIVATE)
    val currentEmail: String? get()=prefs.getString("session",null)?.takeIf { prefs.contains("account_$it") }
    private fun normalized(email: String)=email.trim().lowercase(Locale.ROOT)
    private fun key(email: String)=java.security.MessageDigest.getInstance("SHA-256").digest(email.toByteArray()).joinToString("") { "%02x".format(it) }
    val dataFile: String get()=currentEmail?.let { "tutor_user_${key(it)}" } ?: "tutor_v1"
    @Synchronized fun register(name: String,email: String,password: String) {
        require(FormValidator.validate(FieldKind.NAME,name)==null && AuthValidator.email(email)==null && AuthValidator.password(password)==null) { "Revisa los campos del formulario." }
        val address=normalized(email)
        require(!prefs.contains("account_$address")) { "Ya existe una cuenta local con este correo. Inicia sesión." }
        val record=JSONObject().put("name",name.trim()).put("hash",PasswordHasher.hash(password))
        check(prefs.edit().putString("account_$address",record.toString()).commit()) { "No se pudo guardar la cuenta." }
        // Una sola migración del avance anterior a la primera cuenta local.
        val target=context.getSharedPreferences("tutor_user_${key(address)}",Context.MODE_PRIVATE).edit()
        if(!prefs.getBoolean("legacy_migrated",false)) {
            context.getSharedPreferences("tutor_v1",Context.MODE_PRIVATE).all.forEach { (k,v) -> when(v) { is String -> target.putString(k,v); is Boolean -> target.putBoolean(k,v) } }
            prefs.edit().putBoolean("legacy_migrated",true).commit()
        }
        target.putString("name",name.trim()).putString("role","STUDENT").commit()
        check(prefs.edit().putString("session",address).commit()) { "No se pudo iniciar la sesión." }
    }
    @Synchronized fun login(email: String,password: String) {
        val address=normalized(email)
        val record=prefs.getString("account_$address",null)
        val hash=record?.let { JSONObject(it).getString("hash") }
        require(hash!=null && PasswordHasher.verify(password,hash)) { "Correo o contraseña incorrectos." }
        check(prefs.edit().putString("session",address).commit()) { "No se pudo guardar la sesión." }
    }
    fun logout() { prefs.edit().remove("session").commit() }
}
