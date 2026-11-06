package cl.esucar.tutor

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : Activity() {
    private lateinit var repo: Repository
    private lateinit var content: LinearLayout
    private var page = "Inicio"
    private var selected: String? = null
    private var course = 0
    private var query = ""
    private var category = "Todas"
    private var pickingStage: String? = null
    private val green = Color.rgb(0, 91, 71)
    private val ink = Color.rgb(24, 46, 40)
    private val muted = Color.rgb(78, 98, 90)
    private val pale = Color.rgb(232, 242, 236)
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = Repository(this)
        savedInstanceState?.let {
            page = it.getString("page", "Inicio"); selected = it.getString("selected")
            course = it.getInt("course"); query = it.getString("query", ""); category = it.getString("category", "Todas")
            pickingStage = it.getString("picking")
        }
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        Reminders.schedule(this)
        ContentSyncService.schedule(this)
        render()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("page", page); outState.putString("selected", selected)
        outState.putInt("course", course); outState.putString("query", query); outState.putString("category", category)
        outState.putString("picking", pickingStage)
    }
    private fun dp(value: Int) = (resources.displayMetrics.density * value).toInt()
    private fun bg(color: Int, radius: Int = 18) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun label(value: String, size: Float = 16f, color: Int = ink, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color); setPadding(0, dp(4), 0, dp(7))
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun addText(parent: LinearLayout, value: String, size: Float = 16f, color: Int = ink, bold: Boolean = false) {
        parent.addView(label(value, size, color, bold))
    }
    private fun button(parent: LinearLayout, title: String, primary: Boolean = false, action: () -> Unit) {
        parent.addView(Button(this).apply {
            text = title; isAllCaps = false; textSize = 15f; minHeight = dp(52)
            setTextColor(if (primary) Color.WHITE else green)
            backgroundTintList = android.content.res.ColorStateList.valueOf(if (primary) green else pale)
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
    }
    private fun card(parent: LinearLayout = content, tint: Int = Color.WHITE, block: (LinearLayout) -> Unit) {
        val box = column().apply { setPadding(dp(18), dp(16), dp(18), dp(16)); background = bg(tint) }
        parent.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })
        block(box)
    }
    private fun input(parent: LinearLayout, title: String, initial: String, multiline: Boolean = false): EditText {
        addText(parent, title, 14f, muted, true)
        return EditText(this).apply {
            setText(initial); textSize = 16f; setTextColor(ink); minHeight = dp(52)
            setSingleLine(!multiline)
            if (multiline) { minLines = 3; gravity = Gravity.TOP; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES }
            contentDescription = title
            parent.addView(this, LinearLayout.LayoutParams(-1, -2))
        }
    }
    private fun watch(edit: EditText, action: (String) -> Unit) {
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { action(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_LONG).show()
    private fun navigate(destination: String) { page = destination; selected = null; render() }

    private fun render() {
        val root = column().apply { setBackgroundColor(Color.rgb(244, 247, 245)) }
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.ime())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        val header = column().apply { setPadding(dp(24), dp(16), dp(24), dp(8)) }
        addText(header, "ESUCAR  /  TUTOR VIRTUAL", 13f, green, true)
        addText(header, if (repo.role == Role.STUDENT) "Tu trabajo de título, paso a paso." else "Panel docente · demostración", 15f, muted)
        root.addView(header)
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val frame = android.widget.FrameLayout(this)
        content = column().apply { setPadding(dp(20), dp(12), dp(20), dp(20)) }
        val maxWidth = minOf(resources.displayMetrics.widthPixels, dp(840))
        frame.addView(content, android.widget.FrameLayout.LayoutParams(maxWidth, -2, Gravity.TOP or Gravity.CENTER_HORIZONTAL))
        scroll.addView(frame)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setBackgroundColor(Color.WHITE) }
        listOf("Inicio", "Guía", "Consultas", "Ajustes").forEach { title ->
            nav.addView(Button(this).apply {
                text = title; isAllCaps = false; textSize = 12f; setPadding(0, dp(4), 0, dp(4)); minWidth = 0; minHeight = dp(56)
                setTextColor(if (title == page) Color.WHITE else green)
                backgroundTintList = android.content.res.ColorStateList.valueOf(if (title == page) green else Color.WHITE)
                setOnClickListener { navigate(title) }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(nav)
        setContentView(root)
        when (page) {
            "Inicio" -> home()
            "Guía" -> selected?.let { id -> detail(Catalog.stages.first { it.id == id }) } ?: guide()
            "Consultas" -> faqs()
            else -> settings()
        }
    }
    private fun home() {
        addText(content, "Hola, ${repo.name}", 28f, ink, true)
        addText(content, "Cada avance cuenta. Continúa con tu próximo hito.", 16f, muted)
        val approved = Catalog.stages.count { repo.progress(it).status == DeliveryStatus.APPROVED }
        card(tint = green) { box ->
            addText(box, "MI RUTA ACADÉMICA", 12f, Color.WHITE, true)
            addText(box, "$approved de ${Catalog.stages.size} hitos aprobados", 25f, Color.WHITE, true)
            box.addView(ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = Catalog.stages.size; progress = approved
                progressTintList = android.content.res.ColorStateList.valueOf(Color.rgb(204, 230, 143))
                contentDescription = "$approved de ${Catalog.stages.size} hitos aprobados"
            }, LinearLayout.LayoutParams(-1, dp(14)))
            addText(box, "Trabajo de Título I · II · III", 14f, Color.WHITE)
        }
        val next = Catalog.stages.filter { repo.progress(it).status != DeliveryStatus.APPROVED }.minByOrNull { repo.progress(it).date }
        addText(content, "Próximo paso", 21f, ink, true)
        if (next == null) card { addText(it, "¡Ruta completada! Todos tus hitos están aprobados.", 18f, green, true) }
        else stageCard(next)
        val late = Catalog.stages.count { Rules.overdue(repo.progress(it), LocalDate.now()) }
        if (late > 0) card(tint = Color.rgb(255, 236, 220)) { addText(it, "$late hito(s) con fecha vencida. Revisa tu planificación con el docente.", 16f) }
        button(content, "Explorar guía metodológica", true) { navigate("Guía") }
        button(content, "Resolver una duda") { navigate("Consultas") }
        addText(content, "PROTOTIPO ACADÉMICO · Sin conexión", 12f, muted, true)
        addText(content, "Contenido y fechas de ejemplo. Requieren validación del docente. Los datos se guardan en este dispositivo.", 14f, muted)
    }
    private fun stageCard(stage: Stage) {
        val p = repo.progress(stage)
        card { box ->
            addText(box, "TRABAJO DE TÍTULO ${stage.course}  •  ${p.status.label}", 12f, green, true)
            addText(box, stage.title, 21f, ink, true)
            addText(box, stage.description, 15f, muted)
            addText(box, "Entrega: ${p.date.format(dateFormat)}${if (Rules.overdue(p, LocalDate.now())) " · Vencido" else ""}", 14f, ink, true)
            addText(box, "Lista de avance: ${p.checked.size}/${stage.steps.size}", 14f, muted)
            button(box, "Abrir etapa") { page = "Guía"; selected = stage.id; render() }
        }
    }
    private fun guide() {
        addText(content, "Tu guía metodológica", 28f, ink, true)
        addText(content, "Aprende, prepara tu entrega y sigue tu avance.", 16f, muted)
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Todas las asignaturas", "Trabajo de Título I", "Trabajo de Título II", "Trabajo de Título III"))
        spinner.setSelection(course)
        content.addView(spinner, LinearLayout.LayoutParams(-1, dp(56)))
        val stages = column(); content.addView(stages)
        fun fill() {
            stages.removeAllViews()
            val original = content; content = stages
            Catalog.stages.filter { course == 0 || it.course == course }.forEach { stageCard(it) }
            content = original
        }
        fill()
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { course = position; fill() }
        }
    }
    private fun detail(stage: Stage) {
        val p = repo.progress(stage)
        val student = repo.role == Role.STUDENT
        val editable = student && p.status in listOf(DeliveryStatus.NOT_STARTED, DeliveryStatus.IN_PROGRESS)
        button(content, "‹ Volver a las etapas") { selected = null; render() }
        addText(content, "TRABAJO DE TÍTULO ${stage.course}", 12f, green, true)
        addText(content, stage.title, 28f, ink, true)
        addText(content, "${p.status.label} · ${p.date.format(dateFormat)}", 16f, green, true)
        card { box ->
            addText(box, "01  /  Orientación", 20f, ink, true)
            addText(box, stage.description)
            stage.steps.forEachIndexed { i, step -> addText(box, "${i + 1}. $step", 16f) }
        }
        card { box ->
            addText(box, "02  /  Formato y pauta", 20f, ink, true)
            addText(box, repo.format(stage))
            addText(box, "Criterios de revisión", 17f, green, true)
            repo.rubric(stage).lines().forEach { addText(box, "• $it", 16f) }
            addText(box, "Pauta orientativa, sin ponderaciones oficiales.", 13f, muted)
        }
        card { box ->
            addText(box, "03  /  Lista de avance", 20f, ink, true)
            stage.steps.forEachIndexed { i, step ->
                box.addView(CheckBox(this).apply {
                    text = step; textSize = 16f; setTextColor(ink); minHeight = dp(48); isChecked = i in p.checked; isEnabled = editable
                    setOnCheckedChangeListener { _, checked ->
                        val current = repo.progress(stage)
                        repo.save(stage, current.copy(checked = if (checked) current.checked + i else current.checked - i))
                    }
                })
            }
            val notes = input(box, "Notas de trabajo (guardado automático)", p.notes, true)
            notes.isEnabled = editable
            watch(notes) { repo.save(stage, repo.progress(stage).copy(notes = it)) }
            addText(box, if (p.attachment.isBlank()) "Sin documento vinculado." else "Documento vinculado en este dispositivo.", 14f, muted)
            if (editable) button(box, "Vincular documento") {
                pickingStage = stage.id
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE); type = "*/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }, 42)
            }
            if (p.attachment.isNotBlank()) button(box, "Abrir documento vinculado") {
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.attachment)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) }
                catch (_: Exception) { toast("No se pudo abrir. Comprueba que el archivo existe y vuelve a vincularlo.") }
            }
        }
        if (p.feedback.isNotBlank()) card(tint = pale) { addText(it, "Retroalimentación docente", 19f, green, true); addText(it, p.feedback) }
        if (student) {
            when (p.status) {
                DeliveryStatus.NOT_STARTED -> button(content, "Comenzar esta etapa", true) { transition(stage, DeliveryStatus.IN_PROGRESS) }
                DeliveryStatus.IN_PROGRESS -> button(content, "Marcar como enviado", true) {
                    AlertDialog.Builder(this).setTitle("Registrar entrega local")
                        .setMessage("Se guardará el estado para revisión en este dispositivo. No se enviará ningún archivo por internet. Usa también el canal de entrega indicado por tu docente.")
                        .setNegativeButton("Cancelar", null).setPositiveButton("Registrar") { _, _ -> transition(stage, DeliveryStatus.SUBMITTED) }.show()
                }
                DeliveryStatus.SUBMITTED -> addText(content, "Pendiente de revisión docente en este dispositivo.", 16f, green)
                DeliveryStatus.APPROVED -> addText(content, "Etapa aprobada. Continúa con el siguiente hito.", 16f, green)
            }
        } else {
            card { box ->
                addText(box, "Gestión docente · demo", 20f, green, true)
                button(box, "Cambiar fecha límite") {
                    DatePickerDialog(this, { _, year, month, day ->
                        repo.save(stage, repo.progress(stage).copy(date = LocalDate.of(year, month + 1, day)))
                        Reminders.schedule(this); render()
                    }, p.date.year, p.date.monthValue - 1, p.date.dayOfMonth).show()
                }
                val format = input(box, "Formato requerido", repo.format(stage), true)
                val rubric = input(box, "Criterios (uno por línea)", repo.rubric(stage), true)
                button(box, "Guardar pauta") {
                    if (format.text.isBlank() || rubric.text.isBlank()) toast("Completa el formato y al menos un criterio.")
                    else { repo.setGuidelines(stage, format.text.toString().trim(), rubric.text.toString().trim()); toast("Pauta guardada"); render() }
                }
                val feedback = input(box, "Retroalimentación (guardado automático)", p.feedback, true)
                watch(feedback) { repo.save(stage, repo.progress(stage).copy(feedback = it)) }
                if (p.status == DeliveryStatus.SUBMITTED) {
                    button(box, "Aprobar hito", true) { transition(stage, DeliveryStatus.APPROVED) }
                    button(box, "Solicitar correcciones") {
                        if (repo.progress(stage).feedback.isBlank()) toast("Describe las correcciones en la retroalimentación.")
                        else transition(stage, DeliveryStatus.IN_PROGRESS)
                    }
                } else addText(box, "La revisión se habilita cuando el estudiante marca el hito como enviado.", 14f, muted)
            }
        }
    }
    private fun transition(stage: Stage, to: DeliveryStatus) {
        val current = repo.progress(stage)
        if (!Rules.canChange(repo.role, current.status, to, current.checked.containsAll(stage.steps.indices.toList()))) {
            toast("Completa todos los puntos de la lista antes de registrar el envío."); return
        }
        repo.save(stage, current.copy(status = to)); Reminders.schedule(this); render()
    }
    private fun faqs() {
        addText(content, "Resuelve tus dudas", 28f, ink, true)
        addText(content, "Respuestas de una biblioteca local. No es un chatbot de inteligencia artificial.", 15f, muted)
        val search = input(content, "Buscar por tema o pregunta", query)
        val all = repo.faqs()
        val categories = listOf("Todas") + all.map { it.category }.distinct()
        if (category !in categories) category = "Todas"
        val spinner = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, categories) }
        spinner.setSelection(categories.indexOf(category))
        content.addView(spinner, LinearLayout.LayoutParams(-1, dp(56)))
        val results = column(); content.addView(results)
        fun fill() {
            results.removeAllViews()
            val matches = all.filter { Rules.matches(it, query, category) }
            addText(results, "${matches.size} respuestas", 14f, muted)
            if (matches.isEmpty()) addText(results, "No encontramos coincidencias. Prueba con «objetivo», «cita» o «informe».")
            matches.forEach { faq -> card(results) {
                addText(it, faq.category.uppercase(), 12f, green, true)
                addText(it, faq.question, 20f, ink, true); addText(it, faq.answer, 16f)
            } }
        }
        fill()
        watch(search) { query = it; fill() }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { category = categories[position]; fill() }
        }
    }
    private fun settings() {
        addText(content, "Tu espacio de estudio", 28f, ink, true)
        card { box ->
            addText(box, "Perfil de demostración", 20f, ink, true)
            val name = input(box, "Nombre para mostrar", repo.name)
            button(box, "Guardar nombre") {
                if (name.text.isBlank()) toast("Escribe un nombre.")
                else { repo.name = name.text.toString().trim().take(60); toast("Nombre guardado") }
            }
            addText(box, "Perfil actual: ${if (repo.role == Role.STUDENT) "Estudiante" else "Docente"}", 16f, green, true)
            addText(box, "Ambos perfiles comparten los datos locales para demostrar el flujo. No hay autenticación ni separación segura de cuentas.", 14f, muted)
            button(box, if (repo.role == Role.STUDENT) "Cambiar a docente (demo)" else "Cambiar a estudiante") {
                repo.role = if (repo.role == Role.STUDENT) Role.TEACHER else Role.STUDENT; render()
            }
        }
        card { box ->
            addText(box, "Recordatorios de entrega", 20f, ink, true)
            addText(box, "Aviso local a las 09:00 del día anterior; si ya pasó, se programa para las 09:00 del día de entrega. Android puede retrasarlo para ahorrar batería.", 15f, muted)
            val toggle = Switch(this).apply {
                text = "Activar recordatorios"; textSize = 16f; minHeight = dp(56); isChecked = repo.reminders
                setOnCheckedChangeListener { _, enabled ->
                    repo.reminders = enabled
                    if (enabled && android.os.Build.VERSION.SDK_INT >= 33 && !Reminders.allowed(this@MainActivity))
                        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 9)
                    Reminders.schedule(this@MainActivity)
                }
            }
            box.addView(toggle)
            button(box, "Probar notificación") {
                if (!Reminders.allowed(this)) toast("Autoriza las notificaciones en Ajustes de Android.")
                else { Reminders.show(this, 999, "Tutor Virtual ESUCAR", "Tus recordatorios están disponibles. Revisa tus próximas entregas."); toast("Notificación de prueba enviada") }
            }
        }
        card { box ->
            addText(box, "Contenidos sin conexión", 20f, ink, true)
            addText(box, "Guías y avances siempre disponibles. Puedes agregar FAQ desde una fuente JSON HTTPS del docente. No se suben tus notas ni documentos.", 15f, muted)
            val endpoint = input(box, "URL de actualización (opcional)", repo.endpoint)
            endpoint.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
            addText(box, repo.syncTime, 14f, green)
            button(box, "Guardar fuente") {
                val value = endpoint.text.toString().trim()
                if (value.isNotBlank() && !validEndpoint(value)) toast("Utiliza una URL HTTPS válida, sin credenciales.")
                else { repo.endpoint = value; ContentSyncService.schedule(this); toast("Fuente guardada"); render() }
            }
            val status = label("La actualización automática se intentará cada 6 horas con conexión.", 14f, muted); box.addView(status)
            button(box, "Actualizar ahora") {
                if (repo.endpoint.isBlank()) toast("Guarda primero una fuente HTTPS.")
                else {
                    status.text = "Consultando contenidos…"
                    Thread {
                        val message = runCatching { ContentSync.update(applicationContext) }.getOrElse { "No se pudo actualizar. Se conserva el contenido local. ${it.message.orEmpty().take(160)}" }
                        runOnUiThread { if (!isDestroyed) status.text = message.toString() }
                    }.start()
                }
            }
        }
        card { box ->
            addText(box, "Acerca del proyecto", 20f, ink, true)
            addText(box, "Tutor Virtual · DSY1105\nMVP académico basado en el caso ESUCAR. No es una aplicación oficial de Carabineros de Chile.\n\nSin servidor institucional, sin IA remota y sin envío de entregas. Las fechas iniciales son ejemplos relativos al primer uso; el docente puede editarlas. Al desinstalar se borran los datos locales.", 15f, muted)
        }
    }
    private fun validEndpoint(value: String): Boolean = runCatching {
        val url = java.net.URI(value)
        url.scheme == "https" && !url.host.isNullOrBlank() && url.userInfo == null
    }.getOrDefault(false)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 9) {
            if (grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED) { repo.reminders = false; toast("Recordatorios desactivados. Puedes autorizarlos después.") }
            Reminders.schedule(this); render()
        }
    }
    @Deprecated("Compatible con API 26")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val stage = Catalog.stages.find { it.id == pickingStage } ?: return
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                repo.save(stage, repo.progress(stage).copy(attachment = uri.toString()))
                toast("Documento vinculado. No se ha subido a internet."); render()
            } catch (_: SecurityException) { toast("No se pudo conservar acceso al archivo. Selecciónalo desde otro proveedor.") }
        }
    }
}
