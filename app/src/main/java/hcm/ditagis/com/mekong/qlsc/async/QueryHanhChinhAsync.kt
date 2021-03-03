package hcm.ditagis.com.mekong.qlsc.async

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.mekong.qlsc.R
import java.util.*

/**
 * Created by ThanLe on 4/16/2018.
 */
class QueryHanhChinhAsync(private val mContext: Context, private val mServiceFeatureTable: ServiceFeatureTable, delegate: AsyncResponse?)
    : AsyncTask<Void?, ArrayList<Feature>?, Void?>() {
    interface AsyncResponse {
        fun processFinish(output: ArrayList<Feature>?)
    }

    var delegate: AsyncResponse? = null
    private val mDialog: ProgressDialog
    var features: ArrayList<Feature>? = null
    override fun doInBackground(vararg voids: Void?): Void? {
        val queryParameters = QueryParameters()
        val query = "1=1"
        queryParameters.whereClause = query
        queryParameters.isReturnGeometry = false
        val featureQueryResultListenableFuture = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        featureQueryResultListenableFuture.addDoneListener {
            try {
                val result = featureQueryResultListenableFuture.get()
                val iterator: Iterator<Feature> = result.iterator()
                features = ArrayList()
                while (iterator.hasNext()) {
                    val feature = iterator.next()
                    features!!.add(feature)
                }
                publishProgress(features)
            } catch (e: Exception) {
                publishProgress()
            }
        }
        return null
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog.setMessage(mContext.getString(R.string.async_dang_tai_du_lieu))
        mDialog.setCancelable(false)
        //        mDialog.setButton("Hủy", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                publishProgress(null);
//            }
//        });
        mDialog.show()
    }

    override fun onProgressUpdate(vararg values: ArrayList<Feature>?) {
        super.onProgressUpdate(*values)
        if (values != null && values.isNotEmpty())
            delegate!!.processFinish(values[0])
        else {
            delegate!!.processFinish(null)
        }

        mDialog.dismiss()
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
    }

    init {
        mDialog = ProgressDialog(mContext, android.R.style.Theme_Material_Dialog_Alert)
        this.delegate = delegate
    }
}