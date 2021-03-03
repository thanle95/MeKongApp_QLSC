package hcm.ditagis.com.mekong.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.AsyncTask
import android.widget.LinearLayout
import android.widget.TextView
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.mekong.qlsc.R
import hcm.ditagis.com.mekong.qlsc.databinding.LayoutProgressDialogBinding
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
@SuppressLint("StaticFieldLeak")
open class QueryServiceFeatureTableGetListAsync constructor(private val mActivity: Activity, serviceFeatureTable: ServiceFeatureTable, delegate: AsyncResponse)
    : AsyncTask<QueryParameters?, List<Feature>?, Void?>() {

    @SuppressLint("StaticFieldLeak")
    private val mDelegate: AsyncResponse = delegate
    private val mApplication: DApplication = mActivity.application as DApplication
    private val mServiceFeatureTable: ServiceFeatureTable = serviceFeatureTable
    private var mDialog: AlertDialog? = null

    open interface AsyncResponse {
        fun processFinish(output: List<Feature>?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        val bindingLayout = LayoutProgressDialogBinding.inflate(mActivity.layoutInflater)
        bindingLayout.txtProgressDialogTitle.setText(mActivity.getApplicationContext().getString(R.string.message_list_task_title))
        bindingLayout.txtProgressDialogMessage.setText(mActivity.getApplicationContext().getString(R.string.message_list_task_message))
        val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
        builder.setCancelable(false)
        builder.setView(bindingLayout.root)
        mDialog = builder.create()
        mDialog!!.show()
        //        Window window = mDialog.getWindow();
//        if (window != null) {
//            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//            layoutParams.copyFrom(mDialog.getWindow().getAttributes());
//            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
//            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
//            mDialog.getWindow().setAttributes(layoutParams);
//        }
    }

    override fun doInBackground(vararg params: QueryParameters?): Void? {
        if (params.isNotEmpty()) try {
            val featureQueryResultListenableFuture: ListenableFuture<FeatureQueryResult> = mServiceFeatureTable.queryFeaturesAsync(params.get(0),
                    ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
            featureQueryResultListenableFuture.addDoneListener {
                try {
                    val result: FeatureQueryResult = featureQueryResultListenableFuture.get()
                    val iterator: Iterator<Feature> = result.iterator()
                    var item: Feature
                    val features: MutableList<Feature> = ArrayList()
                    while (iterator.hasNext()) {
                        item = iterator.next()
                        features.add(item)
                    }
                    publishProgress(features)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    publishProgress()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                    publishProgress()
                }
            }
        } catch (e: Exception) {
            publishProgress()
        }
        return null
    }

    protected override fun onProgressUpdate(vararg values: List<Feature>?) {
        if (values.isNotEmpty()) mDelegate.processFinish(values.get(0)) else mDelegate.processFinish(null)
        if (mDialog != null && mDialog!!.isShowing) mDialog!!.dismiss()
    }

}