package hcm.ditagis.com.mekong.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.android.material.bottomsheet.BottomSheetDialog
import hcm.ditagis.com.mekong.qlsc.databinding.LayoutProgressDialogBinding
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.utities.Constant
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by ThanLe on 4/16/2018.
 */
@SuppressLint("StaticFieldLeak")
class CheckExistFeatureTask (private val delegate: Response) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mDialog: BottomSheetDialog
    interface Response {
        fun post(output: String?)
    }
    fun execute(activity: Activity, mapView: MapView, application: DApplication){
        preExecute(activity)
        executor.execute {
            //Kiểm tra vị trí hiện tại đã có điểm sự cố hay chưa
            @SuppressLint("WrongThread") val listListenableFuture: ListenableFuture<List<IdentifyLayerResult>> = mapView
                    .identifyLayersAsync(mapView.locationToScreen(application.addFeaturePoint), 5.0, false)
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
                                    postExecute(feature.attributes[Constant.FieldSuCo.ID_SUCO].toString())
                                else postExecute()
                            } else postExecute()
                        } else postExecute()
                    } else postExecute()
                } catch (e: Exception) {
                    postExecute()
                }
            }
        }
    }
    private fun preExecute(activity: Activity){
        mDialog = BottomSheetDialog(activity)
        val bindingView = LayoutProgressDialogBinding.inflate(activity.layoutInflater)
        bindingView.txtProgressDialogTitle.text = "Đang kiểm tra..."
        mDialog.setContentView(bindingView.root)
        mDialog.setCancelable(false)

        mDialog.show()
    }
    private fun postExecute(vararg values: String?){
        handler.post {
            if (mDialog.isShowing)
                mDialog.dismiss()
            if (values.isEmpty()) {
                delegate.post(null)
            } else delegate.post(values[0])
        }
    }

}