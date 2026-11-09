package cl.esucar.tutor.ui.components

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import cl.esucar.tutor.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/** Campo Material 3; recibe reglas calculadas en el ViewModel. */
class ValidatedField(context: Context,title: String,initial: String,multiline: Boolean,private val guidance: String) : LinearLayout(context) {
    val editor=TextInputEditText(context)
    val fieldLayout=TextInputLayout(context,null,com.google.android.material.R.attr.textInputOutlinedStyle)
    private var previousError: String?=null
    init {
        orientation=VERTICAL
        val dp=context.resources.displayMetrics.density
        fieldLayout.apply {
            hint=title
            boxBackgroundMode=TextInputLayout.BOX_BACKGROUND_OUTLINE
            setStartIconDrawable(R.drawable.ic_edit)
            startIconContentDescription=null
            isErrorEnabled=true
            helperText=guidance
            setBoxCornerRadii(12*dp,12*dp,12*dp,12*dp)
        }
        editor.apply {
            background=null
            setText(initial); textSize=16f; minHeight=(56*dp).toInt()
            setSingleLine(!multiline); contentDescription=title
            if(multiline) { minLines=3; gravity=Gravity.TOP; inputType=android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES }
        }
        fieldLayout.addView(editor,LayoutParams(-1,-2))
        addView(fieldLayout,LayoutParams(-1,-2).apply { topMargin=(8*dp).toInt(); bottomMargin=(8*dp).toInt() })
    }
    fun showResult(error: String?,touched: Boolean) {
        fieldLayout.helperText=if(touched && error==null) "Campo válido · $guidance" else guidance
        fieldLayout.error=error
        if(error!=null && error!=previousError) UiAnimations.error(fieldLayout)
        previousError=error
    }
}
