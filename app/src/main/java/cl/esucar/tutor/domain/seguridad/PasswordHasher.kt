package cl.esucar.tutor.domain.seguridad

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private fun derive(password: String,salt: ByteArray): ByteArray {
        val spec=PBEKeySpec(password.toCharArray(),salt,120000,256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded } finally { spec.clearPassword() }
    }
    private fun hex(bytes: ByteArray)=bytes.joinToString("") { "%02x".format(it) }
    private fun bytes(hex: String)=hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    fun hash(password: String): String {
        val salt=ByteArray(16).also { SecureRandom().nextBytes(it) }
        return "${hex(salt)}:${hex(derive(password,salt))}"
    }
    fun verify(password: String,stored: String): Boolean = runCatching {
        val parts=stored.split(':'); MessageDigest.isEqual(derive(password,bytes(parts[0])),bytes(parts[1]))
    }.getOrDefault(false)
}
