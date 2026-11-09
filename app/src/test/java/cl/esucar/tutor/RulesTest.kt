package cl.esucar.tutor

import cl.esucar.tutor.domain.model.*
import cl.esucar.tutor.domain.validacion.*
import cl.esucar.tutor.domain.repository.*
import cl.esucar.tutor.data.local.Catalog

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class RulesTest {
    @Test fun studentCannotApproveOrSkipStages() {
        assertFalse(Rules.canChange(Role.STUDENT, DeliveryStatus.IN_PROGRESS, DeliveryStatus.APPROVED, true))
        assertFalse(Rules.canChange(Role.STUDENT, DeliveryStatus.NOT_STARTED, DeliveryStatus.SUBMITTED, true))
        assertFalse(Rules.canChange(Role.STUDENT, DeliveryStatus.IN_PROGRESS, DeliveryStatus.SUBMITTED, false))
        assertTrue(Rules.canChange(Role.STUDENT, DeliveryStatus.IN_PROGRESS, DeliveryStatus.SUBMITTED, true))
    }
    @Test fun teacherReviewsOnlySubmittedWork() {
        assertTrue(Rules.canChange(Role.TEACHER, DeliveryStatus.SUBMITTED, DeliveryStatus.APPROVED, true))
        assertTrue(Rules.canChange(Role.TEACHER, DeliveryStatus.SUBMITTED, DeliveryStatus.IN_PROGRESS, false))
        assertFalse(Rules.canChange(Role.TEACHER, DeliveryStatus.NOT_STARTED, DeliveryStatus.APPROVED, true))
    }
    @Test fun searchIgnoresAccentsAndCaseButHonorsCategory() {
        val faq = Faq("¿Cómo cito?", "Citación", "Revisa la norma académica.")
        assertTrue(Rules.matches(faq, " CITACION ", "Todas"))
        assertTrue(Rules.matches(faq, "academica", "Citación"))
        assertFalse(Rules.matches(faq, "cito", "Formato"))
        assertFalse(Rules.matches(faq, "inexistente", "Todas"))
    }
    @Test fun submittedAndApprovedWorkIsNotOverdue() {
        val today = LocalDate.of(2026, 11, 5)
        val previous = Progress(date = today.minusDays(1))
        assertTrue(Rules.overdue(previous, today))
        assertFalse(Rules.overdue(previous.copy(status = DeliveryStatus.SUBMITTED), today))
        assertFalse(Rules.overdue(previous.copy(status = DeliveryStatus.APPROVED), today))
        assertFalse(Rules.overdue(previous.copy(date = today), today))
    }
    @Test fun catalogHasUniqueIdsAndCoversThreeCourses() {
        assertEquals(Catalog.stages.size, Catalog.stages.map { it.id }.distinct().size)
        assertEquals(setOf(1, 2, 3), Catalog.stages.map { it.course }.toSet())
        assertTrue(Catalog.stages.all { it.steps.isNotEmpty() && it.rubric.isNotEmpty() })
    }
}
