package cl.esucar.tutor.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import cl.esucar.tutor.R

/** Componente visual; recibe el resultado de validación, nunca decide reglas. */
class ValidatedField(context: Context, title: String, initial: String, multiline: Boolean, private val hint: String) : LinearLayout(context) {
    val editor=EditText(context)
    private val status=TextView(context)
    private var previousError: String?=null
    init {
        orientation=VERTICAL
        val dp=context.resources.displayMetrics.density
        val label=TextView(context).apply {
            text=title; textSize=14f; setTextColor(Color.rgb(40,76,60)); setTypeface(null,Typeface.BOLD)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit,0,0,0); compoundDrawablePadding=(8*dp).toInt()
            setPadding(0,(10*dp).toInt(),0,(4*dp).toInt())
        }
        addView(label)
        editor.apply {
            setText(initial); textSize=16f; minHeight=(52*dp).toInt(); setTextColor(Color.rgb(24,46,40))
            setSingleLine(!multiline); contentDescription=title
            if(multiline) { minLines=3; gravity=Gravity.TOP; inputType=android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES }
        }
        addView(editor,LayoutParams(-1,-2))
        status.apply { textSize=13f; accessibilityLiveRegion=View.ACCESSIBILITY_LIVE_REGION_POLITE; setPadding(0,0,0,(8*dp).toInt()); compoundDrawablePadding=(6*dp).toInt() }
        addView(status,LayoutParams(-1,-2))
        showResult(null,false)
    }
    fun showResult(error: String?, touched: Boolean) {
        editor.error=error
        status.text=error ?: if(touched) "Campo válido · $hint" else hint
        status.setTextColor(if(error!=null) Color.rgb(160,32,32) else Color.rgb(38,94,68))
        status.setCompoundDrawablesWithIntrinsicBounds(if(error!=null) R.drawable.ic_warning else if(touched) R.drawable.ic_check else 0,0,0,0)
        if(error!=null && error!=previousError) UiAnimations.error(editor)
        previousError=error
    }
}
