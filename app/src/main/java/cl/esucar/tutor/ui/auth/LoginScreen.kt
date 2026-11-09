package cl.esucar.tutor.ui.auth

import android.graphics.Color
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.*
import cl.esucar.tutor.ui.components.*
import cl.esucar.tutor.ui.navegacion.TutorScreenHost

internal fun TutorScreenHost.loginScreen() {
    val state=auth.state.value
    addText(content,"Tu próxima meta comienza aquí",30f,ink,true)
    addText(content,"Organiza tu trabajo de título, consulta las guías y sigue tu avance.",16f,muted)
    card(tint=green) { box ->
        addText(box,"TUTOR VIRTUAL",13f,Color.WHITE,true)
        addText(box,"Aprende. Investiga. Avanza.",24f,Color.WHITE,true)
    }
    card { box ->
        addText(box,if(state.register) "Crear tu cuenta" else "Inicia sesión",24f,ink,true)
        fun field(key: String,title: String,value: String,password: Boolean=false) {
            val component=cl.esucar.tutor.ui.components.ValidatedField(this,title,value,false,if(password) "De 8 a 128 caracteres" else if(key=="email") "Correo de tu cuenta local" else "De 2 a 60 caracteres")
            component.editor.isSaveEnabled=false
            component.editor.inputType=when { password -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD; key=="email" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; else -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS }
            if(password) component.editor.transformationMethod=PasswordTransformationMethod.getInstance()
            authFields[key]=component; box.addView(component)
            watch(component.editor) { auth.edit(key,it) }
            component.showResult(state.errors[key],false)
        }
        if(state.register) field("name","Nombre completo",state.name)
        field("email","Correo electrónico",state.email)
        field("password","Contraseña",state.password,true)
        if(state.register) field("confirmation","Confirmar contraseña",state.confirmation,true)
        box.addView(CheckBox(this).apply {
            text="Mostrar contraseña"; minHeight=dp(48)
            setOnCheckedChangeListener { _,shown ->
                listOf("password","confirmation").forEach { key -> authFields[key]?.editor?.let { edit ->
                    edit.transformationMethod=if(shown) null else PasswordTransformationMethod.getInstance(); edit.setSelection(edit.text.length)
                } }
            }
        })
        authError=label(state.message,14f,Color.rgb(160,32,32)).also { it.accessibilityLiveRegion=android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE; box.addView(it) }
        if(state.busy) box.addView(ProgressBar(this).apply { contentDescription="Comprobando tu cuenta" },LinearLayout.LayoutParams(dp(40),dp(40)))
        button(box,if(state.register) "Crear cuenta" else "Ingresar",true) { auth.submit() }
        (box.getChildAt(box.childCount-1) as Button).isEnabled=!state.busy
        button(box,if(state.register) "Ya tengo cuenta · Iniciar sesión" else "Primera vez · Crear cuenta") { auth.mode(!state.register) }
        (box.getChildAt(box.childCount-1) as Button).isEnabled=!state.busy
    }
    addText(content,"Acceso local de este dispositivo. No utiliza cuentas institucionales ni sincroniza con un servidor. Al desinstalar la app se eliminan las cuentas y los datos.",14f,muted)
    if(state.register) addText(content,"La primera cuenta recuperará los avances de la versión anterior. Después, cada cuenta tendrá sus propios datos. El perfil docente sigue siendo una demostración académica.",14f,muted)
}
