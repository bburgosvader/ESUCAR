package cl.esucar.tutor.domain.validacion

import cl.esucar.tutor.domain.model.*
import java.net.URI
import java.time.LocalDate

enum class FieldKind { NAME, NOTES, FEEDBACK, FORMAT, RUBRIC, ENDPOINT, DATE, SEARCH }

/** Reglas puras, compartidas por formularios y acciones. No importa clases de Android. */
object FormValidator {
    fun validate(kind: FieldKind, raw: String): String? {
        val value = raw.trim()
        if (raw.any { it.isISOControl() && it != '\n' && it != '\r' && it != '\t' }) return "Elimina los caracteres de control."
        return when (kind) {
            FieldKind.NAME -> when {
                value.length !in 2..60 -> "Escribe entre 2 y 60 caracteres."
                !value.any { it.isLetter() } -> "Incluye al menos una letra en el nombre."
                else -> null
            }
            FieldKind.NOTES, FieldKind.FEEDBACK -> if (raw.length > 2000) "Máximo 2000 caracteres; actualmente ${raw.length}." else null
            FieldKind.FORMAT -> if (value.length !in 10..2000) "Describe el formato con entre 10 y 2000 caracteres." else null
            FieldKind.RUBRIC -> when {
                value.isBlank() -> "Agrega al menos un criterio."
                value.lines().size > 12 -> "Incluye como máximo 12 criterios."
                value.lines().any { it.trim().length !in 5..200 } -> "Cada línea debe tener un criterio de 5 a 200 caracteres."
                else -> null
            }
            FieldKind.ENDPOINT -> when {
                value.isEmpty() -> null // Es opcional: vacío desactiva la actualización.
                value.length > 2048 -> "La URL no puede superar 2048 caracteres."
                !runCatching { val uri=URI(value); uri.scheme=="https" && !uri.host.isNullOrBlank() && uri.userInfo==null && uri.fragment==null }.getOrDefault(false) -> "Usa una URL HTTPS completa, sin credenciales ni fragmentos."
                else -> null
            }
            FieldKind.DATE -> when {
                !value.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> "Usa el formato AAAA-MM-DD, por ejemplo 2026-12-15."
                runCatching { LocalDate.parse(value) }.isFailure -> "La fecha no existe. Revisa el día y el mes."
                else -> null
            }
            FieldKind.SEARCH -> if (raw.length > 120) "Busca con un máximo de 120 caracteres." else null
        }
    }

    fun transition(role: Role, progress: Progress, target: DeliveryStatus, stage: Stage, feedback: String): String? {
        if (role == Role.TEACHER && target == DeliveryStatus.IN_PROGRESS && feedback.trim().length < 10)
            return "Describe las correcciones con al menos 10 caracteres."
        if (!Rules.canChange(role, progress.status, target, progress.checked.containsAll(stage.steps.indices.toList()))) {
            return if (role == Role.STUDENT && progress.status == DeliveryStatus.IN_PROGRESS && target == DeliveryStatus.SUBMITTED)
                "Completa todos los puntos de la lista antes de registrar la entrega."
            else "Este cambio de estado no está permitido para el perfil y estado actuales."
        }
        return null
    }
}
