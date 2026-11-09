package cl.esucar.tutor.ui.components

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

internal fun TutorScreenHost.dp(value: Int) = (resources.displayMetrics.density * value).toInt()

internal fun TutorScreenHost.bg(color: Int, radius: Int = 18) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }

internal fun TutorScreenHost.column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

internal fun TutorScreenHost.label(value: String, size: Float = 16f, color: Int = ink, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color); setPadding(0, dp(4), 0, dp(7))
        if (bold) setTypeface(null, Typeface.BOLD)
    }

internal fun TutorScreenHost.addText(parent: LinearLayout, value: String, size: Float = 16f, color: Int = ink, bold: Boolean = false) {
        parent.addView(label(value, size, color, bold))
    }

internal fun TutorScreenHost.button(parent: LinearLayout, title: String, primary: Boolean = false, action: () -> Unit) {
        parent.addView(Button(this).apply {
            text = title; isAllCaps = false; textSize = 15f; minHeight = dp(52)
            setTextColor(if (primary) Color.WHITE else green)
            backgroundTintList = android.content.res.ColorStateList.valueOf(if (primary) green else pale)
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
    }

internal fun TutorScreenHost.card(parent: LinearLayout = content, tint: Int = Color.WHITE, block: (LinearLayout) -> Unit) {
        val box = column().apply { setPadding(dp(18), dp(16), dp(18), dp(16)); background = bg(tint) }
        parent.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })
        block(box)
    }

internal fun TutorScreenHost.input(parent: LinearLayout,title: String,initial: String,multiline: Boolean=false,
        key: String,kind: FieldKind,hint: String): EditText {
        val field=ValidatedField(this,title,vm.draft(key,initial),multiline,hint)
        fields[key]=field
        parent.addView(field,LinearLayout.LayoutParams(-1,-2))
        watch(field.editor) { vm.edit(key,kind,it) }
        field.showResult(vm.ui.errors[key],key in vm.ui.touched)
        return field.editor
    }

internal fun TutorScreenHost.watch(edit: EditText, action: (String) -> Unit) {
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { action(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

internal fun TutorScreenHost.toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_LONG).show()

internal fun TutorScreenHost.focusFirstError() {
        fields.entries.firstOrNull { vm.ui.errors.containsKey(it.key) }?.value?.editor?.requestFocus()
    }
