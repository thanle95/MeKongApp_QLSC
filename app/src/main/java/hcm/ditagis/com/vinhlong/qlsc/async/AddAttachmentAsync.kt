package hcm.ditagis.com.vinhlong.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.AsyncTask
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.LinearLayout
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import kotlinx.android.synthetic.main.layout_progress_dialog.view.*
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
@SuppressLint("StaticFieldLeak")
class AddAttachmentAsync(private val mActivity: Activity, selectedArcGISFeature: ArcGISFeature, private val mImage: ByteArray, private val mDelegate: AsyncResponse) : AsyncTask<String, Boolean, Void>() {
    private lateinit var mDialog: BottomSheetDialog
    private val mServiceFeatureTable: ServiceFeatureTable = selectedArcGISFeature.featureTable as ServiceFeatureTable
    private var mSelectedArcGISFeature: ArcGISFeature = selectedArcGISFeature
    private var mApplication: DApplication = mActivity.application as DApplication
    interface AsyncResponse {
        fun processFinish(success: Boolean?)
    }

    @SuppressLint("SetTextI18n")
    override fun onPreExecute() {
        super.onPreExecute()
        mDialog = BottomSheetDialog(this.mActivity)
        val view = mActivity.layoutInflater.inflate(R.layout.layout_progress_dialog, null, false) as LinearLayout
        view.txt_progress_dialog_title.text = "Đang thêm ảnh..."
        mDialog.setContentView(view)
        mDialog.setCancelable(false)

        mDialog.show()
    }

    override fun doInBackground(vararg params: String): Void? {
        if (params.isEmpty()) {
            publishProgress()
            return null
        }
        val attachmentName = String.format(Constant.AttachmentName.ADD,  mApplication.user?.userName, System.currentTimeMillis())
        val addResult = mSelectedArcGISFeature.addAttachmentAsync(mImage, Constant.CompressFormat.TYPE_UPDATE.toString(), attachmentName)
        addResult.addDoneListener {
            try {
                val attachment = addResult.get()
                if (attachment.size > 0) {
                    val voidListenableFuture = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature)
                    voidListenableFuture.addDoneListener {
                        val applyEditsAsync = mServiceFeatureTable.applyEditsAsync()
                        applyEditsAsync.addDoneListener {
                            try {
                                val featureEditResults = applyEditsAsync.get()
                                if (featureEditResults.size > 0) {
                                    if (!featureEditResults[0].hasCompletedWithErrors()) {
                                        publishProgress(true)
                                    } else {
                                        publishProgress()
                                    }
                                } else {
                                    publishProgress()
                                }
                            } catch (e: Exception) {
                                publishProgress()
                            }


                        }


                    }
                } else {
                    publishProgress()
                }

            } catch (e: Exception) {
                publishProgress()
            }
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Boolean?) {
        super.onProgressUpdate(*values)
        if (mDialog.isShowing) {
            mDialog.dismiss()
        }
        if (values.isNotEmpty() && values[0]!!) {
            mDelegate.processFinish(true)
        } else
            mDelegate.processFinish(false)

    }


}
