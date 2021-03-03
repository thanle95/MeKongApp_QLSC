package hcm.ditagis.com.mekong.qlsc.async

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Geometry
import hcm.ditagis.com.mekong.qlsc.R
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
class QueryFeatureHanhChinhAsync constructor(private val mContext: Context, private val mServiceFeatureTable: ServiceFeatureTable, private val mGeometry: Geometry, delegate: AsyncResponse?) : AsyncTask<Void?, Feature?, Void?>() {
    open interface AsyncResponse {
        fun processFinish(output: Feature?)
    }

    var delegate: AsyncResponse? = null
    private val mDialog: ProgressDialog
    var mFeature: Feature? = null
    override fun doInBackground(vararg voids: Void?): Void? {
        val queryParameters = QueryParameters()
        queryParameters.geometry = mGeometry
        queryParameters.isReturnGeometry = true
        val featureQueryResultListenableFuture: ListenableFuture<FeatureQueryResult> = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        featureQueryResultListenableFuture.addDoneListener(object : Runnable {
            public override fun run() {
                try {
                    val result: FeatureQueryResult = featureQueryResultListenableFuture.get()
                    val iterator: Iterator<Feature> = result.iterator()
                    while (iterator.hasNext()) {
                        mFeature = iterator.next()
                    }
                    mDialog.dismiss()
                    publishProgress(mFeature)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            }
        })
        return null
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog.setMessage(mContext.getString(R.string.async_dang_tai_du_lieu))
        mDialog.setCancelable(false)
        mDialog.show()
    }

    override fun onProgressUpdate(vararg values: Feature?) {
        super.onProgressUpdate(*values)
        delegate!!.processFinish(values.get(0))
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
    }

    init {
        mDialog = ProgressDialog(mContext, android.R.style.Theme_Material_Dialog_Alert)
        this.delegate = delegate
    }
}