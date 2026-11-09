package cl.esucar.tutor.ui.components

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.widget.ProgressBar

/** Respeta la preferencia global de Android de desactivar animaciones. */
object UiAnimations {
    fun enter(view: View) {
        if (!ValueAnimator.areAnimatorsEnabled()) return
        view.alpha=0f
        view.translationY=12f*view.resources.displayMetrics.density
        view.animate().alpha(1f).translationY(0f).setDuration(180).start()
    }
    fun error(view: View) {
        if (!ValueAnimator.areAnimatorsEnabled()) return
        ObjectAnimator.ofFloat(view,"translationX",0f,-8f,8f,-4f,4f,0f).apply { duration=220; start() }
    }
    fun progress(view: ProgressBar, target: Int) {
        if (!ValueAnimator.areAnimatorsEnabled()) { view.progress=target; return }
        ObjectAnimator.ofInt(view,"progress",view.progress,target).apply { duration=350; start() }
    }
}
