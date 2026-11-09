package cl.esucar.tutor

import cl.esucar.tutor.domain.seguridad.PasswordHasher
import cl.esucar.tutor.domain.validacion.AuthValidator
import org.junit.Assert.*
import org.junit.Test

class PasswordHasherTest {
    @Test fun saltedHashesDifferAndOnlyCorrectPasswordVerifies() {
        val a=PasswordHasher.hash("ClaveSegura123"); val b=PasswordHasher.hash("ClaveSegura123")
        assertNotEquals(a,b); assertTrue(PasswordHasher.verify("ClaveSegura123",a))
        assertFalse(PasswordHasher.verify("otra",a)); assertFalse(PasswordHasher.verify("x","malformado"))
    }
    @Test fun credentialsHaveDefinedValidation() {
        assertNull(AuthValidator.email("ana@example.org")); assertNotNull(AuthValidator.email("ana@"))
        assertNotNull(AuthValidator.password("123")); assertNull(AuthValidator.password("12345678"))
    }
}
