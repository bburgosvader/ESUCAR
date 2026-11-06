package cl.esucar.tutor

import java.text.Normalizer
import java.time.LocalDate

enum class DeliveryStatus(val label: String) {
    NOT_STARTED("Por iniciar"), IN_PROGRESS("En desarrollo"), SUBMITTED("Enviado"), APPROVED("Aprobado")
}
enum class Role { STUDENT, TEACHER }
data class Stage(val id: String, val course: Int, val title: String, val description: String,
    val format: String, val steps: List<String>, val rubric: List<String>, val days: Long)
data class Faq(val question: String, val category: String, val answer: String)
data class Progress(val status: DeliveryStatus = DeliveryStatus.NOT_STARTED, val date: LocalDate,
    val notes: String = "", val feedback: String = "", val checked: Set<Int> = emptySet(), val attachment: String = "")

object Rules {
    fun normalize(value: String) = Normalizer.normalize(value.lowercase(java.util.Locale.ROOT), Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
    fun matches(faq: Faq, query: String, category: String): Boolean =
        (category == "Todas" || category == faq.category) && normalize("${faq.question} ${faq.answer} ${faq.category}").contains(normalize(query.trim()))
    fun canChange(role: Role, from: DeliveryStatus, to: DeliveryStatus, complete: Boolean): Boolean = when (role) {
        Role.STUDENT -> (from == DeliveryStatus.NOT_STARTED && to == DeliveryStatus.IN_PROGRESS) ||
            (from == DeliveryStatus.IN_PROGRESS && to == DeliveryStatus.SUBMITTED && complete)
        Role.TEACHER -> from == DeliveryStatus.SUBMITTED && (to == DeliveryStatus.APPROVED || to == DeliveryStatus.IN_PROGRESS)
    }
    fun overdue(progress: Progress, today: LocalDate) = progress.date < today &&
        progress.status != DeliveryStatus.APPROVED && progress.status != DeliveryStatus.SUBMITTED
}
