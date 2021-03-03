package hcm.ditagis.com.mekong.qlsc.async

import android.os.AsyncTask
import android.util.Log
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment

/**
 * Created by ThanLe on 4/16/2018.
 */
class GetAttachmentsAsync(private val mDelegate: AsyncResponse) : AsyncTask<ArcGISFeature?, List<Attachment?>?, Void?>() {

    interface AsyncResponse {
        fun processFinish(attachments: List<Attachment?>?)
    }

    override fun doInBackground(vararg params: ArcGISFeature?): Void? {
        if (params.isNotEmpty()) {
            val attachmentResults = params[0]?.fetchAttachmentsAsync()
            attachmentResults?.addDoneListener {
                try {
                    val attachments = attachmentResults.get()
                    publishProgress(attachments)
                } catch (e: Exception) {
                    Log.e("Lỗi attachment", e.toString())
                    publishProgress()
                }
            }
        } else publishProgress()
        return null
    }

    override fun onProgressUpdate(vararg values: List<Attachment?>?) {
        if (values.isNotEmpty()) {
            mDelegate.processFinish(values[0])
        } else mDelegate.processFinish(null)
    }

}