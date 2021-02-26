package hcm.ditagis.com.vinhlong.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
class QueryServiceFeatureTableAsync constructor(@field:SuppressLint("StaticFieldLeak") private val mActivity: Activity, delegate: AsyncResponse) : AsyncTask<QueryParameters?, Feature?, Void?>() {

    @SuppressLint("StaticFieldLeak")
    private val mDelegate: AsyncResponse
    private val mApplication: DApplication
    private val mServiceFeatureTable: ServiceFeatureTable

    open interface AsyncResponse {
        fun processFinish(output: Feature?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: QueryParameters?): Void? {
        try {
            if (params.isNotEmpty()) {
                val featureQueryResultListenableFuture: ListenableFuture<FeatureQueryResult> = mServiceFeatureTable.queryFeaturesAsync(params.get(0), ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                featureQueryResultListenableFuture.addDoneListener {
                    try {
                        val result: FeatureQueryResult = featureQueryResultListenableFuture.get()
                        val iterator: Iterator<*> = result.iterator()
                        if (iterator.hasNext()) {
                            val feature: Feature = iterator.next() as Feature
                            publishProgress(feature)
                        } else {
                            publishProgress()
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        publishProgress()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                        publishProgress()
                    }
                }
            } else publishProgress()
        } catch (e: Exception) {
            publishProgress()
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Feature?) {
        when {
            values.isNotEmpty() -> mDelegate.processFinish(values[0])
            else -> mDelegate.processFinish(null)
        }
    }

    override fun onPostExecute(result: Void?) {}

    init {
        mApplication = mActivity.getApplication() as DApplication
        mServiceFeatureTable = mApplication.dFeatureLayer!!.layer.getFeatureTable() as ServiceFeatureTable
        mDelegate = delegate
    }
}