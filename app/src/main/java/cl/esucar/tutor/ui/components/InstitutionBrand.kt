package cl.esucar.tutor.ui.components

import android.graphics.Color
import android.widget.ImageView
import android.widget.LinearLayout
import cl.esucar.tutor.R
import cl.esucar.tutor.ui.navegacion.TutorScreenHost
import com.google.android.material.card.MaterialCardView

/** Recurso original del sitio institucional, conservado sin modificar el logotipo. */
internal fun TutorScreenHost.institutionBrand(parent: LinearLayout) {
    val brand=MaterialCardView(this).apply {
        setCardBackgroundColor(Color.BLACK); radius=dp(12).toFloat(); cardElevation=0f; strokeWidth=0
    }
    brand.addView(ImageView(this).apply {
        setImageResource(R.drawable.logo_carabineros)
        contentDescription="Logotipo de Carabineros de Chile"
        scaleType=ImageView.ScaleType.FIT_CENTER
        setPadding(dp(12),dp(8),dp(12),dp(8))
    },LinearLayout.LayoutParams(-1,dp(72)))
    parent.addView(brand,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(10) })
}
