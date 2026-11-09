package cl.esucar.tutor.ui.navegacion

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
import cl.esucar.tutor.ui.auth.loginScreen
import cl.esucar.tutor.R

internal fun TutorScreenHost.navigate(destination: String) { vm.navigate(destination) }

internal fun TutorScreenHost.render() {
        authFields.clear(); authError=null; fields.clear(); syncStatus=null; syncSpinner=null; syncButton=null; deliveryError=null; checklistLabel=null; checklistProgress=null
        val root = column().apply { setBackgroundColor(Color.rgb(244, 247, 243)) }
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
        institutionBrand(header)
        addText(header, "ESUCAR  /  TUTOR VIRTUAL", 13f, green, true)
        addText(header, if (vm.role == Role.STUDENT) "Tu trabajo de título, paso a paso." else "Panel docente · demostración", 15f, muted)
        root.addView(header)
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val frame = android.widget.FrameLayout(this)
        content = column().apply { setPadding(dp(20), dp(12), dp(20), dp(20)) }
        val maxWidth = minOf(resources.displayMetrics.widthPixels, dp(840))
        frame.addView(content, android.widget.FrameLayout.LayoutParams(maxWidth, -2, Gravity.TOP or Gravity.CENTER_HORIZONTAL))
        scroll.addView(frame)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        if(auth.state.value.session==null) {
            setContentView(root); loginScreen(); UiAnimations.enter(content); return
        }
        val nav=com.google.android.material.bottomnavigation.BottomNavigationView(this).apply {
            labelVisibilityMode=com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED
            setBackgroundColor(Color.WHITE)
        }
        val destinations=listOf("Inicio","Guía","Consultas","Ajustes")
        val icons=listOf(R.drawable.ic_home,R.drawable.ic_guide,R.drawable.ic_help,R.drawable.ic_settings)
        destinations.forEachIndexed { index,title -> nav.menu.add(0,100+index,index,title).setIcon(icons[index]) }
        nav.selectedItemId=100+destinations.indexOf(page).coerceAtLeast(0)
        nav.setOnItemSelectedListener { item -> navigate(destinations[item.itemId-100]); true }
        root.addView(nav)
        setContentView(root)
        when (page) {
            "Inicio" -> home()
            "Guía" -> selected?.let { id -> detail(vm.stages.first { it.id == id }) } ?: guide()
            "Consultas" -> faqs()
            else -> settings()
        }
        UiAnimations.enter(content)
    }

internal fun TutorScreenHost.transition(stage: Stage,to: DeliveryStatus) {
        if(vm.transition(stage,to)) Reminders.schedule(this) else focusFirstError()
    }
