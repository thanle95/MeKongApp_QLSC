package hcm.ditagis.com.vinhlong.qlsc.entities

import android.app.Application
import android.graphics.Bitmap
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.Point
import hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB.DFeatureLayer
import hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB.User
import hcm.ditagis.com.vinhlong.qlsc.utities.DAlertDialog
import java.util.ArrayList

class DApplication : Application() {
    lateinit var alertDialog: DAlertDialog
    var dFeatureLayer: DFeatureLayer? = null
    var mSFTAdministrator: ServiceFeatureTable? = null
    var diemSuCo: DiemSuCo? = null
        get() {
            if (field == null) field = DiemSuCo()
            return field
        }
        private set
    var geometry: Geometry? = null

    var selectedArcGISFeature: ArcGISFeature? = null

    var user: User? = null
    var appInfo: DAppInfo? = null
    var addFeaturePoint: Point? = null

    var layerInfos: List<DLayerInfo>? = null

    var images: List<ByteArray>? = null

    var isCheckedVersion = false

    var bitmaps: ArrayList<Bitmap>? = null
    var selectedAttachment: Attachment? = null
    var selectedBitmap: Bitmap? = null
}