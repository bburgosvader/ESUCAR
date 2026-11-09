package cl.esucar.tutor

import cl.esucar.tutor.domain.model.*
import cl.esucar.tutor.domain.validacion.*
import cl.esucar.tutor.domain.repository.*
import org.junit.Assert.*
import org.junit.Test

class FormValidatorTest {
    @Test fun namesHaveExplicitBoundaries() {
        assertNotNull(FormValidator.validate(FieldKind.NAME," "))
        assertNotNull(FormValidator.validate(FieldKind.NAME,"A"))
        assertNotNull(FormValidator.validate(FieldKind.NAME,"1234"))
        assertNull(FormValidator.validate(FieldKind.NAME,"María José"))
        assertNull(FormValidator.validate(FieldKind.NAME,"a".repeat(60)))
        assertNotNull(FormValidator.validate(FieldKind.NAME,"a".repeat(61)))
    }
    @Test fun optionalNotesDoNotSilentlyTruncate() {
        assertNull(FormValidator.validate(FieldKind.NOTES,""))
        assertNull(FormValidator.validate(FieldKind.NOTES,"x".repeat(2000)))
        assertNotNull(FormValidator.validate(FieldKind.NOTES,"x".repeat(2001)))
        assertNotNull(FormValidator.validate(FieldKind.FEEDBACK,"x".repeat(2001)))
    }
    @Test fun datesMustExistAndUseIsoFormat() {
        assertNull(FormValidator.validate(FieldKind.DATE,"2028-02-29"))
        assertNotNull(FormValidator.validate(FieldKind.DATE,"2026-02-29"))
        assertNotNull(FormValidator.validate(FieldKind.DATE,"29/02/2028"))
        assertNotNull(FormValidator.validate(FieldKind.DATE,""))
    }
    @Test fun endpointIsOptionalButRejectsInsecureOrCredentialUrls() {
        assertNull(FormValidator.validate(FieldKind.ENDPOINT,""))
        assertNull(FormValidator.validate(FieldKind.ENDPOINT,"https://example.org/faq.json"))
        listOf("http://example.org","https://user:pass@example.org","javascript:alert(1)","https://example.org/#fragment","not a url").forEach {
            assertNotNull(it,FormValidator.validate(FieldKind.ENDPOINT,it))
        }
    }
    @Test fun rubricValidatesEveryLine() {
        assertNull(FormValidator.validate(FieldKind.RUBRIC,"Claridad del problema\nCoherencia metodológica"))
        assertNotNull(FormValidator.validate(FieldKind.RUBRIC,"Claridad\nx"))
        assertNotNull(FormValidator.validate(FieldKind.RUBRIC,(1..13).joinToString("\n") { "Criterio válido $it" }))
        assertNotNull(FormValidator.validate(FieldKind.FORMAT,""))
    }
    @Test fun searchAndControlCharactersHaveLimits() {
        assertNotNull(FormValidator.validate(FieldKind.SEARCH,"x".repeat(121)))
        assertNotNull(FormValidator.validate(FieldKind.NOTES,"texto\u0000oculto"))
        assertNull(FormValidator.validate(FieldKind.NOTES,"Párrafo uno\nPárrafo dos"))
    }
}
