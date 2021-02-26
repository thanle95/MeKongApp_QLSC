package hcm.ditagis.com.vinhlong.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import java.util.*

/**
 * Created by ThanLe on 4/16/2018.
 */
@SuppressLint("StaticFieldLeak")
class CheckExistFeatureAsync constructor(private val mActivity: Activity, mapView: MapView,
                                         private val mServiceFeatureTable: ServiceFeatureTable, delegate: AsyncResponse) : AsyncTask<Void?, String?, Void?>() {

    @SuppressLint("StaticFieldLeak")
    private val mDelegate: AsyncResponse = delegate
    private val mApplication: DApplication = mActivity.application as DApplication
    private val mMapView: MapView = mapView

    interface AsyncResponse {
        fun processFinish(output: String?)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        //Kiểm tra vị trí hiện tại đã có điểm sự cố hay chưa
        @SuppressLint("WrongThread") val listListenableFuture: ListenableFuture<List<IdentifyLayerResult>> = mMapView
                .identifyLayersAsync(mMapView.locationToScreen(mApplication.addFeaturePoint), 5.0, false)
        listListenableFuture.addDoneListener {
            val identifyLayerResults: List<IdentifyLayerResult>
            try {
                identifyLayerResults = listListenableFuture.get()
                if (identifyLayerResults.isNotEmpty()) for (identifyLayerResult: IdentifyLayerResult in identifyLayerResults) {
                    val elements: List<GeoElement> = identifyLayerResult.getElements()
                    if (elements.isNotEmpty() && elements[0] is ArcGISFeature) {
                        //Nếu có điểm sự cố, kiểm tra ngày phản ánh có phải là hôm nay hay không
                        val feature: ArcGISFeature = elements[0] as ArcGISFeature
                        val ngayPhanAnh: Any? = feature.attributes[Constant.FieldSuCo.TG_PHAN_ANH]
                        if (ngayPhanAnh != null) {
                            val c1: Calendar = ngayPhanAnh as Calendar
                            val c2: Calendar = Calendar.getInstance()
                            if (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                                    c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                                publishProgress(feature.attributes[Constant.FieldSuCo.ID_SUCO].toString())
                            else publishProgress()
                        } else publishProgress()
                    } else publishProgress()
                } else publishProgress()
            } catch (e: Exception) {
                publishProgress()
            }
        }
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        if (values.isEmpty()) {
            mDelegate.processFinish(null)
        } else mDelegate.processFinish(values.get(0))
    }

    override fun onPostExecute(result: Void?) {}

}