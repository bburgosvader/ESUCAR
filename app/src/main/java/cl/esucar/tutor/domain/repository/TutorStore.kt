package cl.esucar.tutor.domain.repository

import cl.esucar.tutor.domain.model.*
/** Contrato de datos: el ViewModel no conoce SharedPreferences ni Activity. */
interface TutorStore {
    var role: Role
    var name: String
    var endpoint: String
    var reminders: Boolean
    val syncTime: String
    fun progress(stage: Stage): Progress
    fun save(stage: Stage, progress: Progress)
    fun format(stage: Stage): String
    fun rubric(stage: Stage): String
    fun setGuidelines(stage: Stage, format: String, rubric: String)
    fun faqs(): List<Faq>
}
