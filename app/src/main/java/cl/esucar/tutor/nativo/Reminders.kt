package cl.esucar.tutor.nativo

import cl.esucar.tutor.domain.model.*
import cl.esucar.tutor.domain.validacion.*
import cl.esucar.tutor.domain.repository.*
import cl.esucar.tutor.data.local.Catalog
import cl.esucar.tutor.data.repository.Repository
import cl.esucar.tutor.data.remote.ContentSyncService
import cl.esucar.tutor.MainActivity
import cl.esucar.tutor.R

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId

object Reminders {
    private const val CHANNEL = "entregas"
    fun allowed(context: Context) = Build.VERSION.SDK_INT < 33 ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    fun schedule(context: Context) {
        val repo = Repository(context)
        val alarms = context.getSystemService(AlarmManager::class.java)
        Catalog.stages.forEachIndexed { index, stage ->
            val intent = Intent(context, ReminderReceiver::class.java).putExtra("stage", stage.id)
            val pending = PendingIntent.getBroadcast(context, index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarms.cancel(pending)
            val p = repo.progress(stage)
            if (repo.reminders && p.status != DeliveryStatus.APPROVED && p.status != DeliveryStatus.SUBMITTED) {
                val dayBefore = p.date.minusDays(1).atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val sameDay = p.date.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val target = if (dayBefore > System.currentTimeMillis()) dayBefore else sameDay
                if (target > System.currentTimeMillis()) alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target, pending)
            }
        }
    }
    fun show(context: Context, id: Int, title: String, body: String) {
        if (!allowed(context)) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Fechas de entrega", NotificationManager.IMPORTANCE_DEFAULT))
        val pending = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        manager.notify(id, Notification.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_book)
            .setContentTitle(title).setContentText(body).setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(pending).setAutoCancel(true).build())
    }
}
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = Repository(context)
        val stage = Catalog.stages.find { it.id == intent.getStringExtra("stage") } ?: return
        val p = repo.progress(stage)
        if (repo.reminders && p.status != DeliveryStatus.APPROVED && p.status != DeliveryStatus.SUBMITTED && !p.date.isBefore(LocalDate.now())) {
            Reminders.show(context, stage.id.hashCode(), "Entrega: ${stage.title}", "Fecha límite: ${p.date}. Revisa tu lista de avance.")
        }
    }
}
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Reminders.schedule(context)
            ContentSyncService.schedule(context)
        }
    }
}
