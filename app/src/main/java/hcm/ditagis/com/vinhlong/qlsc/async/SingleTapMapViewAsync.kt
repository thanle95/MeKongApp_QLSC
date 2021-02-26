package hcm.ditagis.com.vinhlong.qlsc.async

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.Point
import android.os.AsyncTask
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ArcGISFeatureTable
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import hcm.ditagis.com.vinhlong.qlsc.MainActivity
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB.DFeatureLayer
import hcm.ditagis.com.vinhlong.qlsc.utities.Popup
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
class SingleTapMapViewAsync constructor(activity: MainActivity, private val mDFeatureLayers: List<DFeatureLayer>, @field:SuppressLint("StaticFieldLeak") private val mPopUp: Popup,
                                        private val mClickPoint: Point, @field:SuppressLint("StaticFieldLeak") private val mMapView: MapView) : AsyncTask<com.esri.arcgisruntime.geometry.Point?, DFeatureLayer?, Void?>() {
    private val mDialog: ProgressDialog?

    @SuppressLint("StaticFieldLeak")
    private val mActivity: Activity

    private var mSelectedArcGISFeature: ArcGISFeature? = null

    private var isFound: Boolean = false
    private val mApplication: DApplication
    override fun doInBackground(vararg points: com.esri.arcgisruntime.geometry.Point?): Void? {
        val listListenableFuture: ListenableFuture<List<IdentifyLayerResult>> = mMapView
                .identifyLayersAsync(mClickPoint, 5.0, false)
        listListenableFuture.addDoneListener {
            val identifyLayerResults: List<IdentifyLayerResult>
            try {
                identifyLayerResults = listListenableFuture.get()
                for (identifyLayerResult: IdentifyLayerResult in identifyLayerResults) {
                    run {
                        val elements: List<GeoElement> = identifyLayerResult.elements
                        if ((elements.isNotEmpty()) && elements[0] is ArcGISFeature && !isFound) {
                            isFound = true
                            mSelectedArcGISFeature = elements[0] as ArcGISFeature?
                            val serviceLayerId: Long = mSelectedArcGISFeature!!.getFeatureTable().getServiceLayerId()
                            val dFeatureLayer: DFeatureLayer? = getmFeatureLayerDTG(serviceLayerId)
                            publishProgress(dFeatureLayer)
                        }
                    }
                }
                publishProgress()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getmFeatureLayerDTG(serviceLayerId: Long): DFeatureLayer? {
        for (DFeatureLayer: DFeatureLayer in mDFeatureLayers) {
            val serviceLayerDTGId: Long = (DFeatureLayer.layer.getFeatureTable() as ArcGISFeatureTable).getServiceLayerId()
            if (serviceLayerDTGId == serviceLayerId) return DFeatureLayer
        }
        return null
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog!!.setMessage("Đang xử lý...")
        mDialog.setCancelable(false)
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hủy", DialogInterface.OnClickListener({ dialogInterface: DialogInterface?, i: Int -> publishProgress() }))
        mDialog.show()
    }

    override fun onProgressUpdate(vararg values: DFeatureLayer?) {
        super.onProgressUpdate(*values)
        if ((values.isNotEmpty()) && (mSelectedArcGISFeature != null)) {
//            HoSoVatTuSuCoAsync hoSoVatTuSuCoAsync = new HoSoVatTuSuCoAsync(mActivity, object -> {
//                if (object != null) {
            mApplication.selectedArcGISFeature = mSelectedArcGISFeature
            mPopUp.showPopup()
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss()
        }
        //            });
//            hoSoVatTuSuCoAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Constant.HOSOVATTUSUCO_METHOD.FIND, mSelectedArcGISFeature.getAttributes()
//                    .get(mActivity.getString(R.string.Field_SuCo_IDSuCo)));
    }

    init {
        mActivity = activity
        mApplication = activity.getApplication() as DApplication
        mDialog = ProgressDialog(activity, R.style.Theme_Material_Dialog_Alert)
    }
}