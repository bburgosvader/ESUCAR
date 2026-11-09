package cl.esucar.tutor.data.repository

import cl.esucar.tutor.data.local.Catalog
import cl.esucar.tutor.domain.model.*
import cl.esucar.tutor.domain.validacion.*
import cl.esucar.tutor.domain.repository.*

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class Repository(context: Context) : TutorStore {
    private val prefs = context.getSharedPreferences("tutor_v1", Context.MODE_PRIVATE)
    override var role: Role
        get() = Role.valueOf(prefs.getString("role", Role.STUDENT.name)!!)
        set(value) { prefs.edit().putString("role", value.name).apply() }
    override var name: String
        get() = prefs.getString("name", "Estudiante")!!
        set(value) { prefs.edit().putString("name", value).apply() }
    override var endpoint: String
        get() = prefs.getString("endpoint", "")!!
        set(value) { prefs.edit().putString("endpoint", value).apply() }
    override var reminders: Boolean
        get() = prefs.getBoolean("reminders", false)
        set(value) { prefs.edit().putBoolean("reminders", value).apply() }
    override val syncTime: String get() = prefs.getString("sync_time", "Sin actualizaciones remotas")!!
    private val start: LocalDate
        get() {
            val stored = prefs.getString("start", null)
            if (stored != null) return LocalDate.parse(stored)
            val now = LocalDate.now()
            prefs.edit().putString("start", now.toString()).apply()
            return now
        }
    override fun progress(stage: Stage): Progress {
        val raw = prefs.getString("stage_${stage.id}", null)
        if (raw == null) return Progress(date = start.plusDays(stage.days))
        val json = JSONObject(raw)
        val checked = json.optJSONArray("checked") ?: JSONArray()
        return Progress(DeliveryStatus.valueOf(json.getString("status")), LocalDate.parse(json.getString("date")),
            json.optString("notes"), json.optString("feedback"), (0 until checked.length()).map { checked.getInt(it) }.toSet(), json.optString("attachment"))
    }
    override fun save(stage: Stage, progress: Progress) {
        val json = JSONObject().put("status", progress.status.name).put("date", progress.date.toString())
            .put("notes", progress.notes).put("feedback", progress.feedback)
            .put("checked", JSONArray(progress.checked.toList())).put("attachment", progress.attachment)
        prefs.edit().putString("stage_${stage.id}", json.toString()).apply()
    }
    override fun format(stage: Stage) = prefs.getString("format_${stage.id}", stage.format)!!
    override fun rubric(stage: Stage) = prefs.getString("rubric_${stage.id}", stage.rubric.joinToString("\n"))!!
    override fun setGuidelines(stage: Stage, format: String, rubric: String) {
        prefs.edit().putString("format_${stage.id}", format).putString("rubric_${stage.id}", rubric).apply()
    }
    override fun faqs(): List<Faq> {
        val raw = prefs.getString("remote_faq", null) ?: return Catalog.faqs
        return Catalog.faqs + decodeFaqs(raw)
    }
    fun cacheFaqs(raw: String) {
        decodeFaqs(raw) // Valida el documento completo antes de reemplazar el caché.
        prefs.edit().putString("remote_faq", raw).putString("sync_time", java.time.LocalDateTime.now().toString().take(16).replace('T', ' ')).apply()
    }
    companion object {
        fun decodeFaqs(raw: String): List<Faq> {
            val array = JSONObject(raw).getJSONArray("faqs")
            require(array.length() in 1..200) { "La fuente debe incluir entre 1 y 200 preguntas." }
            return (0 until array.length()).map {
                val item = array.getJSONObject(it)
                fun field(key: String, max: Int): String = item.getString(key).trim().also { value ->
                    require(value.isNotEmpty() && value.length <= max) { "Campo inválido: $key" }
                }
                Faq(field("question", 240), field("category", 60), field("answer", 5000))
            }
        }
    }
}
