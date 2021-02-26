package hcm.ditagis.com.vinhlong.qlsc.async

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import hcm.ditagis.com.vinhlong.qlsc.R
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
class EditGeometryAsync(@field:SuppressLint("StaticFieldLeak") private val mContext: Context, private val mServiceFeatureTable: ServiceFeatureTable,
                        private val mSelectedArcGISFeature: ArcGISFeature, private val mDelegate: AsyncResponse) : AsyncTask<Point?, Boolean?, Void?>() {
    private val mDialog: ProgressDialog?

    interface AsyncResponse {
        fun processFinish(feature: Boolean?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog!!.setMessage(mContext.getString(R.string.async_dang_xu_ly))
        mDialog.setCancelable(false)
        mDialog.show()
    }

    override fun doInBackground(vararg params: Point?): Void? {
        if (params.isNotEmpty()) {
            mSelectedArcGISFeature.geometry = params[0]
            val updateFuture = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature)
            updateFuture.addDoneListener {
                try {
                    // track the update
                    updateFuture.get()
                    // apply edits once the update has completed
                    if (updateFuture.isDone) {
                        applyEditsToServer()
                    } else {
                        publishProgress()
                    }
                } catch (e1: InterruptedException) {
                    publishProgress()
                } catch (e1: ExecutionException) {
                    publishProgress()
                }
            }
        } else publishProgress()
        return null
    }

    private fun applyEditsToServer() {
        val applyEditsFuture = (mSelectedArcGISFeature
                .featureTable as ServiceFeatureTable).applyEditsAsync()
        applyEditsFuture.addDoneListener {
            try {
                // get results of edit
                val featureEditResultsList = applyEditsFuture.get()
                if (!featureEditResultsList[0].hasCompletedWithErrors()) {
                    publishProgress(true)
                } else {
                    publishProgress()
                }
            } catch (e: InterruptedException) {
                publishProgress()
            } catch (e: ExecutionException) {
                publishProgress()
            }
        }
    }

    override fun onProgressUpdate(vararg values: Boolean?) {
        super.onProgressUpdate(*values)
        if (mDialog != null && mDialog.isShowing) {
            mDialog.dismiss()
        }
        if (values.isNotEmpty()) mDelegate.processFinish(values[0]) else mDelegate.processFinish(null)
    }

    init {
        mDialog = ProgressDialog(mContext, android.R.style.Theme_Material_Dialog_Alert)
    }
}