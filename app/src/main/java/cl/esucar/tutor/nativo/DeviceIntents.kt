package cl.esucar.tutor.nativo

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import cl.esucar.tutor.domain.model.Stage
import java.time.LocalDate
import java.time.ZoneId

/** Intents explícitos de finalidad: no se pide acceso global a documentos ni al calendario. */
object DeviceIntents {
    fun openDocument(uri: String) = Intent(Intent.ACTION_VIEW).apply {
        data=Uri.parse(uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    fun calendar(stage: Stage,date: LocalDate) = Intent(Intent.ACTION_INSERT).apply {
        data=CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE,"Entrega · ${stage.title}")
        putExtra(CalendarContract.Events.DESCRIPTION,"Trabajo de Título ${stage.course}. Revisa la pauta y tu lista de avance en Tutor Virtual ESUCAR.")
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,date.atTime(9,0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME,date.atTime(9,30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }
}

