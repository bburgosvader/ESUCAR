package cl.esucar.tutor

import android.app.job.*
import android.content.ComponentName
import android.content.Context
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object ContentSync {
    fun update(context: Context): String {
        val repo = Repository(context)
        val address = repo.endpoint
        require(address.startsWith("https://")) { "Configura una URL HTTPS de contenidos." }
        val connection = URL(address).openConnection() as HttpsURLConnection
        try {
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.instanceFollowRedirects = false
            require(connection.responseCode == 200) { "La fuente respondió HTTP ${connection.responseCode}. Usa la URL directa al JSON." }
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    require(output.size() + count <= 1_048_576) { "El archivo supera 1 MB." }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            repo.cacheFaqs(bytes.toString(Charsets.UTF_8))
            return "Preguntas actualizadas y disponibles sin conexión."
        } finally { connection.disconnect() }
    }
}
class ContentSyncService : JobService() {
    private var task: Thread? = null
    override fun onStartJob(params: JobParameters): Boolean {
        task = Thread {
            val retry = runCatching { ContentSync.update(this) }.isFailure
            if (!Thread.currentThread().isInterrupted) jobFinished(params, retry)
        }.also { it.start() }
        return true
    }
    override fun onStopJob(params: JobParameters): Boolean { task?.interrupt(); return true }
    companion object {
        fun schedule(context: Context) {
            val scheduler = context.getSystemService(JobScheduler::class.java)
            scheduler.cancel(100)
            if (Repository(context).endpoint.isNotBlank()) scheduler.schedule(JobInfo.Builder(100,
                ComponentName(context, ContentSyncService::class.java)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(6 * 60 * 60 * 1000L).setPersisted(true).build())
        }
    }
}
