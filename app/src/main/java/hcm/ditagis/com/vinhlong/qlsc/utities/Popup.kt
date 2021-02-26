package hcm.ditagis.com.vinhlong.qlsc.utities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.MapView
import hcm.ditagis.com.vinhlong.qlsc.AttachmentActivity
import hcm.ditagis.com.vinhlong.qlsc.MainActivity
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.UpdateActivity
import hcm.ditagis.com.vinhlong.qlsc.adapter.FeatureViewInfoAdapter
import hcm.ditagis.com.vinhlong.qlsc.async.*
import hcm.ditagis.com.vinhlong.qlsc.entities.DAddress
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.entities.HoSoVatTuSuCo
import hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB.ListObjectDB.Companion.instance
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant.RequestCode
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant.TrangThaiSuCo
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicReference

@SuppressLint("Registered")
class Popup(private val mMainActivity: MainActivity, mapView: MapView, serviceFeatureTable: ServiceFeatureTable,
            callout: Callout?, geocoder: Geocoder?) : AppCompatActivity(), View.OnClickListener {
    private var mListTenVatTu: MutableList<String>? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private val mServiceFeatureTable: ServiceFeatureTable
    val callout: Callout?
    private var lstFeatureType: MutableList<String>? = null
    private var linearLayout: LinearLayout? = null
    private val mMapView: MapView
    private var mListHoSoVatTuSuCo: List<HoSoVatTuSuCo>? = null
    private val mApplication: DApplication
    private var mServiceFeatureTableHanhChinh: ServiceFeatureTable? = null
    private val mServiceFeatureTableHanhChinhHuyen: ServiceFeatureTable? = null
    private var quanhuyen_features: ArrayList<Feature>? = null
    private var quanhuyen_feature: Feature? = null
    fun setmServiceFeatureTableHanhChinh(url_HanhChinh: String?) {
        mServiceFeatureTableHanhChinh = ServiceFeatureTable(url_HanhChinh)
        mApplication.mSFTAdministrator = mServiceFeatureTableHanhChinh
        QueryHanhChinhAsync(mMainActivity, mServiceFeatureTableHanhChinh!!, object : QueryHanhChinhAsync.AsyncResponse {
            override fun processFinish(output: ArrayList<Feature>?) {
                quanhuyen_features = output
            }
        }).execute()
    }

    private fun initializeVatTu() {
        if (mListTenVatTu == null) {
            mListTenVatTu = ArrayList()
            for (vatTu in instance!!.vatTus) (mListTenVatTu as ArrayList<String>).add(vatTu.tenVatTu)
        }
    }

    fun refreshPopup(arcGISFeature: ArcGISFeature?) {
        mSelectedArcGISFeature = arcGISFeature
        val attributes = arcGISFeature!!.attributes
        val listView = linearLayout!!.findViewById<ListView>(R.id.lstview_thongtinsuco)
        val featureViewInfoAdapter = FeatureViewInfoAdapter(mMainActivity, ArrayList())
        listView.adapter = featureViewInfoAdapter
        val outFields = mApplication.dFeatureLayer!!.getdLayerInfo().outFieldsArr
        val hiddenFields = mMainActivity.resources.getStringArray(R.array.hidden_Fields)
        var isHiddenField: Boolean
        var isOutField: Boolean
        val idHanhChinh = attributes[Constant.FieldSuCo.MA_PHUONG]
        if (idHanhChinh != null) {
            getHanhChinhFeature(idHanhChinh.toString(), attributes[Constant.FieldSuCo.MA_QUAN].toString())
        }
        for (field in arcGISFeature.featureTable.fields) {
            isOutField = true
            isHiddenField = false
            for (hiddenField in hiddenFields) {
                if (hiddenField == field.name) {
                    isHiddenField = true
                    break
                }
            }
            if (outFields.size > 0 && outFields[0] != "*" && outFields[0] != "") {
                isOutField = false
                for (outField in outFields) {
                    if (outField == field.name) {
                        isOutField = true
                        break
                    }
                }
            }
            val value = attributes[field.name]
            if (value != null && isOutField && !isHiddenField) {
                val item = FeatureViewInfoAdapter.Item()
                item.alias = field.alias
                item.fieldName = field.name
                if (item.fieldName == Constant.FieldSuCo.MA_PHUONG) {
                    if (quanhuyen_feature != null) item.value = quanhuyen_feature!!.attributes[Constant.FieldHanhChinh.TEN_HANH_CHINH].toString() else item.value = value.toString()
                } else if (item.fieldName == Constant.FieldSuCo.MA_QUAN) {
                    if (quanhuyen_feature != null) item.value = quanhuyen_feature!!.attributes[Constant.FieldHanhChinh.TEN_HUYEN].toString() else {
                        item.value = value.toString()
                    }
                } else if (field.domain != null) {
                    var codedValues: List<CodedValue> = ArrayList()
                    try {
                        codedValues = (arcGISFeature.featureTable.getField(item.fieldName).domain as CodedValueDomain).codedValues
                    } catch (ignored: Exception) {
                    }
                    val valueDomain = getValueDomain(codedValues, value.toString())
                    if (valueDomain != null) item.value = valueDomain.toString()
                } else if (item.fieldName == mMainActivity.getString(R.string.Field_SuCo_VatTu)) {
                    val builder = StringBuilder()
                    mListHoSoVatTuSuCo = instance!!.hoSoVatTuSuCos
                    for (hoSoVatTuSuCo in mListHoSoVatTuSuCo!!) {
                        builder.append(hoSoVatTuSuCo.tenVatTu).append(" ").append(hoSoVatTuSuCo.soLuong).append(" ").append(hoSoVatTuSuCo.donViTinh).append("\n")
                    }
                    if (builder.length > 0) builder.replace(builder.length - 2, builder.length, "")
                    item.value = builder.toString()
                } else when (field.fieldType) {
                    Field.Type.DATE -> item.value = Constant.Companion.DATE_FORMAT_VIEW.format((value as Calendar).time)
                    Field.Type.OID, Field.Type.TEXT, Field.Type.SHORT, Field.Type.DOUBLE, Field.Type.INTEGER, Field.Type.FLOAT -> item.value = value.toString()
                }
                featureViewInfoAdapter.add(item)
                featureViewInfoAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun getValueDomain(codedValues: List<CodedValue>, code: String): Any? {
        var value: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.code.toString() == code) {
                value = codedValue.name
                break
            }
        }
        return value
    }

    private fun deleteFeature() {
        val builder = AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
        builder.setTitle("Xác nhận")
        builder.setMessage("Bạn có chắc chắn xóa sự cố này?")
        builder.setPositiveButton("Có") { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            mSelectedArcGISFeature!!.loadAsync()

            // update the selected feature
            mSelectedArcGISFeature!!.addDoneLoadingListener {
                if (mSelectedArcGISFeature!!.loadStatus == LoadStatus.FAILED_TO_LOAD) {
                    Log.d(mMainActivity.resources.getString(R.string.app_name), "Error while loading feature")
                }
                try {
                    // update feature in the feature table
                    val mapViewResult = mServiceFeatureTable.deleteFeatureAsync(mSelectedArcGISFeature)
                    mapViewResult.addDoneListener {

                        // apply change to the server
                        val serverResult = mServiceFeatureTable.applyEditsAsync()
                        serverResult.addDoneListener {
                            val edits: List<FeatureEditResult>
                            try {
//                                            HoSoVatTuSuCoAsync hoSoVatTuSuCoDB = new HoSoVatTuSuCoAsync(mMainActivity);
//                                            hoSoVatTuSuCoDB.delete(mIDSuCo);
                                edits = serverResult.get()
                                if (edits.isNotEmpty()) {
                                    if (!edits[0].hasCompletedWithErrors()) {
                                        Log.e("", "Feature successfully updated")
                                    }
                                }
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            } catch (e: ExecutionException) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(mMainActivity.resources.getString(R.string.app_name), "deteting feature in the feature table failed: " + e.message)
                }
            }
            if (callout != null) callout.dismiss()
        }.setNegativeButton("Không") { dialog: DialogInterface, which: Int -> dialog.dismiss() }.setCancelable(false)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()
    }

    fun showPopupAdd(position: Point?) {
        try {
            if (position == null) return
            val addFeatures = arrayOf<Feature?>(null)
            val longtitude = AtomicReference(0.0)
            val latitdue = AtomicReference(0.0)
            val address = AtomicReference("")
            linearLayout = mMainActivity.layoutInflater.inflate(R.layout.layout_dialog_search_address, null) as LinearLayout
            val txtTitle = linearLayout!!.findViewById<TextView>(R.id.txt_dialog_search_address_title)
            txtTitle.text = "ĐỊA CHỈ"
            val txtAddress = linearLayout!!.findViewById<TextView>(R.id.txt_dialog_search_address_address)
            val txtUtity = linearLayout!!.findViewById<TextView>(R.id.txt_dialog_search_address_utity)
            txtUtity.text = "PHẢN ÁNH SỰ CỐ"
            linearLayout!!.findViewById<View>(R.id.txt_dialog_search_address_utity).setOnClickListener { view: View? ->
                val point = longLatToPoint(longtitude.get(), latitdue.get())
                mApplication.diemSuCo!!.vitri = address.get()
                mApplication.addFeaturePoint = point
                //Kiểm tra cùng ngày, cùng vị trí đã có sự cố nào chưa, nếu có thì cảnh báo, chưa thì thêm bình thường
                CheckExistFeatureAsync(mMainActivity, mMapView, mApplication.dFeatureLayer!!.serviceFeatureTable,
                        object : CheckExistFeatureAsync.AsyncResponse {
                            override fun processFinish(output: String?) {
                                if (output != null && output.isNotEmpty()) showDialogAddDuplicateGeometry(output) else {
                                    mMainActivity.addFeature()
                                }
                            }
                        }).execute()
            }
            linearLayout!!.findViewById<View>(R.id.imgBtn_dialog_search_address_cancel).setOnClickListener { mMainActivity.handlingCancelAdd() }
            linearLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            @SuppressLint("InflateParams") val findLocationAsycn = FindLocationAsycn(mMainActivity, false,
                    object : FindLocationAsycn.AsyncResponse {
                        override fun processFinish(output: List<DAddress>?) {
                            if (output != null && output.isNotEmpty()) {
//                    clearSelection();
//                        dimissCallout();
                                val dAddress = output[0]
                                val addressLine = dAddress.location
                                val geometry = GeometryEngine.project(position, SpatialReferences.getWgs84())
//                                QueryFeatureHanhChinhAsync(mMainActivity, mServiceFeatureTableHanhChinh!!, geometry,
//                                        object : QueryFeatureHanhChinhAsync.AsyncResponse {
//                                            override fun processFinish(output: Feature?) {
//                                                if (output != null) {
                                                    txtAddress.text = addressLine
                                                    address.set(addressLine)
                                                    longtitude.set(dAddress.longtitude)
                                                    latitdue.set(dAddress.latitude)
                                                    callout!!.location = position
                                                    callout.content = linearLayout
                                                    runOnUiThread {
                                                        callout.refresh()
                                                        callout.show()
                                                    }
//                                                } else {
//                                                    Toast.makeText(mMapView.context, String.format("%s không thuộc địa bàn quản lý", addressLine), Toast.LENGTH_LONG).show()
//                                                }
//                                            }
//                                        }).execute()

                            }
                        }


                    })
            val project = GeometryEngine.project(position, SpatialReferences.getWgs84())
            val location = doubleArrayOf(project.extent.center.x, project.extent.center.y)
            findLocationAsycn.setmLongtitude(location[0])
            findLocationAsycn.setmLatitude(location[1])
            findLocationAsycn.execute()
        } catch (e: Exception) {
            Log.e("Popup tìm kiếm", e.toString())
        }
    }

    private fun longLatToPoint(lon: Double, lat: Double): Point {
        val pointLongLat = Point(lon, lat)
        val geometryWgs84 = GeometryEngine.project(pointLongLat, SpatialReferences.getWgs84())
        val geometryWebMercator = GeometryEngine.project(geometryWgs84, SpatialReferences.getWebMercator())
        return geometryWebMercator.extent.center
    }

    private fun showDialogAddDuplicateGeometry(idSuCo: String) {
        val builder = AlertDialog.Builder(mMapView.context, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        builder.setCancelable(true)
                .setNegativeButton("HỦY") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                .setPositiveButton("TIẾP TỤC") { dialogInterface: DialogInterface?, i: Int -> mMainActivity.addFeature() }.setTitle("CẢNH BÁO")
                .setMessage(String.format("Hệ thống phát hiện ở khu vực này đã tiếp nhận sự cố với ID là %s trong ngày hôm nay. Bạn có muốn tiếp tục phản ánh sự cố?", idSuCo))
        val dialog = builder.create()
        dialog.show()
    }

    private fun clearSelection() {
        val featureLayer = mApplication.dFeatureLayer!!.layer
        featureLayer.clearSelection()
    }

    private fun dimissCallout() {
        if (callout != null && callout.isShowing) {
            callout.dismiss()
        }
    }

    private fun getHanhChinhFeature(idHanhChinh: String, maHuyenInput: String) {
        quanhuyen_feature = null
        if (quanhuyen_features != null) {
            for (feature in quanhuyen_features!!) {
                val maDonViHanhChinh = feature.attributes[Constant.FieldHanhChinh.ID_HANH_CHINH]
                val maHuyen = feature.attributes[Constant.FieldHanhChinh.MA_HUYEN]
                if (maDonViHanhChinh != null && maDonViHanhChinh == idHanhChinh && maHuyen != null && maHuyen == maHuyenInput ) {
                    quanhuyen_feature = feature
                    return
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    fun showPopup() {
        initializeVatTu()
        clearSelection()
        dimissCallout()
        mSelectedArcGISFeature = mApplication.selectedArcGISFeature
        val featureLayer = mApplication.dFeatureLayer!!.layer
        featureLayer.selectFeature(mSelectedArcGISFeature)
        lstFeatureType = ArrayList()
        for (i in mSelectedArcGISFeature!!.featureTable.featureTypes.indices) {
            (lstFeatureType as ArrayList<String>).add(mSelectedArcGISFeature!!.featureTable.featureTypes[i].name)
        }
        val inflater = LayoutInflater.from(mMainActivity.applicationContext)
        linearLayout = inflater.inflate(R.layout.layout_thongtinsuco, null) as LinearLayout
        refreshPopup(mSelectedArcGISFeature)
        (linearLayout!!.findViewById<View>(R.id.txt_thongtin_ten) as TextView).text = featureLayer.name
        linearLayout!!.findViewById<View>(R.id.imgBtn_layout_thongtinsuco_close).setOnClickListener(this)
        linearLayout!!.findViewById<View>(R.id.txt_thongtinsuco_prev).setOnClickListener { queryFeature(true) }
        linearLayout!!.findViewById<View>(R.id.txt_thongtinsuco_next).setOnClickListener { queryFeature(false) }
        val txtNumber = linearLayout!!.findViewById<TextView>(R.id.txt_thongtinsuco_number)
        if (featureLayer.name == mMainActivity.getString(R.string.ALIAS_DIEM_SU_CO)) {
            //user admin mới có quyền xóa
            if (mApplication.dFeatureLayer!!.getdLayerInfo().isDelete) {
                linearLayout!!.findViewById<View>(R.id.imgBtn_delete).setOnClickListener(this)
            } else {
                linearLayout!!.findViewById<View>(R.id.imgBtn_delete).visibility = View.GONE
            }
            linearLayout!!.findViewById<View>(R.id.imgBtn_ViewMoreInfo).setOnClickListener(this)
        } else {
            linearLayout!!.findViewById<View>(R.id.imgBtn_ViewMoreInfo).visibility = View.INVISIBLE
            linearLayout!!.findViewById<View>(R.id.imgBtn_delete).visibility = View.INVISIBLE
        }
        linearLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val envelope = mSelectedArcGISFeature!!.geometry.extent
        mMapView.setViewpointGeometryAsync(envelope, 0.0)
        // show CallOut
        callout!!.show(linearLayout, envelope.center)
    }

    @SuppressLint("InflateParams")
    fun showPopupFindLocation(position: Point?, location: String?) {
        try {
            if (position == null) return
            clearSelection()
            dimissCallout()
            val inflater = LayoutInflater.from(mMainActivity.applicationContext)
            linearLayout = inflater.inflate(R.layout.layout_timkiemdiachi, null) as LinearLayout
            (linearLayout!!.findViewById<View>(R.id.txt_timkiemdiachi) as TextView).text = location
            linearLayout!!.findViewById<View>(R.id.imgBtn_timkiemdiachi_themdiemsuco).setOnClickListener(this)
            linearLayout!!.findViewById<View>(R.id.imgBtn_timkiemdiachi).setOnClickListener(this)
            linearLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            // show CallOut
            callout!!.location = position
            callout.content = linearLayout
            runOnUiThread {
                callout.refresh()
                callout.show()
            }
        } catch (e: Exception) {
            Log.e("Popup tìm kiếm", e.toString())
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgBtn_layout_thongtinsuco_close -> {
                clearSelection()
                if (callout != null && callout.isShowing) callout.dismiss()
            }
            R.id.imgBtn_timkiemdiachi -> if (callout != null && callout.isShowing) callout.dismiss()
            R.id.imgBtn_ViewMoreInfo -> {
                val popup = PopupMenu(mMainActivity, view)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.menu_feature_popup, popup.menu)
                if (mSelectedArcGISFeature!!.canEditAttachments()) {
                    popup.menu.getItem(0).isVisible = true
                    popup.menu.getItem(1).isVisible = true
                }
                popup.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.item_popup_view_attachments -> {
                            viewAttachment()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.item_popup_find_route -> {
                            mMainActivity.findRoute()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.item_popup_edit -> {
                            val updateIntent = Intent(mMainActivity, UpdateActivity::class.java)
                            mMainActivity.startActivityForResult(updateIntent, RequestCode.UPDATE)
                            return@setOnMenuItemClickListener true
                        }
                        R.id.item_popup_change_geometry -> {
                            mMainActivity.setChangingGeometry(true)
                            if (callout!!.isShowing) callout.dismiss()
                            return@setOnMenuItemClickListener true
                        }
                        else -> return@setOnMenuItemClickListener false
                    }
                }
                popup.show()
            }
            R.id.imgBtn_delete -> {
                mSelectedArcGISFeature!!.featureTable.featureLayer.clearSelection()
                deleteFeature()
            }
            R.id.imgBtn_timkiemdiachi_themdiemsuco -> mMainActivity.onClick(view)

        }
    }

    private fun queryFeature(isPrev: Boolean) {
        QueryFeatureAsync(mMainActivity, TrangThaiSuCo.MOI_TIEP_NHAN.toInt(), "", "",
                object : QueryFeatureAsync.AsyncResponse {
                    override fun processFinish(output: List<Feature>?) {
                        if (output != null && output.isNotEmpty()) {
                            val objectID = getObjectID(output, comparator, isPrev)
                            mMainActivity.mapViewHandler!!.query(String.format(Constant.Companion.QUERY_BY_OBJECTID, objectID))
                        }
                    }
                }).execute()
    }

    private fun updateAttachment() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.path)
        val photo = ImageFile.getFile(mMainActivity)
        val uri = Uri.fromFile(photo)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        mMainActivity.setSelectedArcGISFeature(mSelectedArcGISFeature)
        mMainActivity.setUri(uri)
        mMainActivity.startActivityForResult(cameraIntent, RequestCode.REQUEST_ID_UPDATE_ATTACHMENT)
    }

    private fun viewAttachment() {

        mApplication.selectedArcGISFeature = mSelectedArcGISFeature
        val intent = Intent(mMainActivity, AttachmentActivity::class.java)
        mMainActivity.startActivity(intent)
//        val viewAttachmentAsync = ViewAttachmentAsync(mMainActivity, mSelectedArcGISFeature)
//        viewAttachmentAsync.execute()
    }

    var comparator = Comparator { o1: Long, o2: Long ->
        val i = o1 - o2
        if (i > 0) return@Comparator 1 else if (i == 0L) return@Comparator 0 else return@Comparator -1
    }


    private fun getObjectID(output: List<Feature>, comparator: Comparator<Long>, isPrev: Boolean): Long {
        val list: MutableList<Long> = ArrayList()
        for (feature in output) {
            list.add(feature.attributes[Constant.Field.OBJECTID].toString().toLong())
        }
        list.sortWith(comparator)
        val currentObjectID = mApplication.selectedArcGISFeature!!.attributes[Constant.Field.OBJECTID].toString().toLong()
        var i = 0
        while (i < list.size) {
            if (list[i] >= currentObjectID) break
            i++
        }
        return if (isPrev) if (i > 0) list[i - 1] else currentObjectID else if (i < list.size) list[i + 1] else currentObjectID
    }

    init {
        mApplication = mMainActivity.application as DApplication
        mMapView = mapView
        mServiceFeatureTable = serviceFeatureTable
        this.callout = callout
    }
}