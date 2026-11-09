package cl.esucar.tutor.ui.estudiante

import cl.esucar.tutor.domain.model.*
import cl.esucar.tutor.domain.validacion.*
import cl.esucar.tutor.domain.repository.*
import cl.esucar.tutor.data.repository.Repository
import cl.esucar.tutor.data.remote.ContentSync
import cl.esucar.tutor.data.remote.ContentSyncService
import cl.esucar.tutor.nativo.*
import cl.esucar.tutor.ui.navegacion.*
import cl.esucar.tutor.ui.components.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
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

import cl.esucar.tutor.ui.estudiante.*
import cl.esucar.tutor.ui.perfil.*
import cl.esucar.tutor.ui.profesor.*
import cl.esucar.tutor.ui.theme.TutorTheme
import cl.esucar.tutor.R

internal fun TutorScreenHost.faqs() {
        addText(content, "Resuelve tus dudas", 28f, ink, true)
        addText(content, "Respuestas de una biblioteca local. No es un chatbot de inteligencia artificial.", 15f, muted)
        val search = input(content,"Buscar por tema o pregunta",query,false,"search",FieldKind.SEARCH,"Máximo 120 caracteres · búsqueda sin distinguir tildes")
        val all = vm.faqs()
        val categories = listOf("Todas") + all.map { it.category }.distinct()
        if (category !in categories) vm.setCategory("Todas")
        val spinner = Spinner(this).apply { adapter = ArrayAdapter(this@faqs, android.R.layout.simple_spinner_dropdown_item, categories) }
        spinner.setSelection(categories.indexOf(category))
        content.addView(spinner, LinearLayout.LayoutParams(-1, dp(56)))
        val results = column(); content.addView(results)
        fun fill() {
            results.removeAllViews()
            val matches = vm.searchResults()
            addText(results, "${matches.size} respuestas", 14f, muted)
            if (matches.isEmpty()) addText(results, "No encontramos coincidencias. Prueba con «objetivo», «cita» o «informe».")
            matches.forEach { faq -> card(results) {
                addText(it, faq.category.uppercase(), 12f, green, true)
                addText(it, faq.question, 20f, ink, true); addText(it, faq.answer, 16f)
            } }
        }
        fill()
        watch(search) { vm.setQuery(it); fill() }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { vm.setCategory(categories[position]); fill() }
        }
    }
