package hcm.ditagis.com.mekong.qlsc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.CompoundButtonCompat
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISMapImageSublayer
import com.esri.arcgisruntime.layers.ArcGISSublayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import com.esri.arcgisruntime.util.ListenableList
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import hcm.ditagis.com.mekong.qlsc.async.FindLocationAsycn
import hcm.ditagis.com.mekong.qlsc.async.PreparingTask
import hcm.ditagis.com.mekong.qlsc.async.QueryServiceFeatureTableGetListAsync
import hcm.ditagis.com.mekong.qlsc.async.UpdateAttachmentAsync
import hcm.ditagis.com.mekong.qlsc.databinding.*
import hcm.ditagis.com.mekong.qlsc.entities.DAddress
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.entities.DLayerInfo
import hcm.ditagis.com.mekong.qlsc.entities.entitiesDB.DFeatureLayer
import hcm.ditagis.com.mekong.qlsc.fragment.task.HandlingSearchHasDone.handleFromItems
import hcm.ditagis.com.mekong.qlsc.utities.CheckConnectInternet.isOnline
import hcm.ditagis.com.mekong.qlsc.utities.Constant
import hcm.ditagis.com.mekong.qlsc.utities.DAlertDialog
import hcm.ditagis.com.mekong.qlsc.utities.MapViewHandler
import hcm.ditagis.com.mekong.qlsc.utities.MySnackBar.make
import hcm.ditagis.com.mekong.qlsc.utities.Popup
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private var mPopUp: Popup? = null
    private var mDFeatureLayer: DFeatureLayer? = null

    var mapViewHandler: MapViewHandler? = null
        private set
    private var mLocationDisplay: LocationDisplay? = null
    private var mGraphicsOverlay: GraphicsOverlay? = null
    private var mIsSearchingFeature = false
    private var mDFeatureLayers: MutableList<DFeatureLayer>? = null
    private var mGeocoder: Geocoder? = null
    private var mImageOpenStreetMap: ImageView? = null
    private var mImageStreetMap: ImageView? = null
    private var mImageImageWithLabel: ImageView? = null
    private var mTxtOpenStreetMap: TextView? = null
    private var mTxtStreetMap: TextView? = null
    private var mTxtImageWithLabel: TextView? = null
    private var mTxtSearchView: SearchView? = null
    private lateinit var states: Array<IntArray>
    private lateinit var colors: IntArray
    private var imageLayersHanhChinh: ArcGISMapImageLayer? = null
    private var imageLayersChuyenDe: ArcGISMapImageLayer? = null
    private var mListLayerID: MutableList<String>? = null
    private var mApplication: DApplication? = null
    private var mIsFirstLocating = true
    private var isChangingGeometry = false
    private var mFeatureLayer: FeatureLayer? = null
    private var mURL_HanhChinh: String? = null
    private lateinit var mBinding: ActivityQuanLySuCoBinding
    fun setChangingGeometry(changingGeometry: Boolean) {
        isChangingGeometry = changingGeometry
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityQuanLySuCoBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mListLayerID = ArrayList()
        mApplication = application as DApplication
        mApplication!!.alertDialog = DAlertDialog()
        startSignIn()
    }

    private fun startSignIn() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        this@MainActivity.startActivityForResult(intent, Constant.RequestCode.LOGIN)
    }

    fun requestPermisson() {
        val permissionCheck1 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
        val permissionCheck2 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED
        val permissionCheck3 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[2]) == PackageManager.PERMISSION_GRANTED
        val permissionCheck4 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[3]) == PackageManager.PERMISSION_GRANTED
        if (!(permissionCheck1 && permissionCheck2 && permissionCheck3 && permissionCheck4)) {
            // If permissions are not already granted, request permission from the user.
            ActivityCompat.requestPermissions(this, Constant.REQUEST_PERMISSIONS, Constant.RequestCode.PERMISSION)
        } else {
            preparing()
        }
    }

    private fun preparing() {
        PreparingTask(object : PreparingTask.Response {
            override fun post(output: List<DLayerInfo>?) {
                if (output != null && output.isNotEmpty()) {
                    mApplication!!.layerInfos = output
                    startMain()
                } else if (output == null) {
                    Toast.makeText(this@MainActivity, "Tài khoản của bạn không có quyền truy cập ứng dụng này", Toast.LENGTH_SHORT).show()
                    startSignIn()
                }
                else{
                    Toast.makeText(this@MainActivity, "Không tìm thấy lớp dữ liệu. Vui lòng liên hệ quản trị viên", Toast.LENGTH_SHORT).show()
                    startSignIn()
                }

            }
        }).execute(this@MainActivity, mApplication!!)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        val expected = grantResults.size
        var sum = 0
        for (i in grantResults) if (i == PackageManager.PERMISSION_GRANTED) sum++
        if (sum == expected) {
            preparing()
        } else {
//            Toast.makeText(MainActivity.this, "Vui lòng cho phép ứng dụng truy cập các quyền trên", Toast.LENGTH_LONG).show();
            requestPermisson()
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun startMain() {
        // create an empty map instance
        mListLayerID!!.clear()
        imageLayersChuyenDe = null
        imageLayersHanhChinh = imageLayersChuyenDe
        states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        colors = intArrayOf(R.color.colorTextColor_1, R.color.colorTextColor_1)

        mBinding.appBar.layoutLayer.visibility = View.INVISIBLE
        setLicense()
        mGeocoder = Geocoder(this.applicationContext, Locale.getDefault())
        setSupportActionBar(mBinding.appBar.toolbar)


        //for camera begin
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        //for camera end
        //đưa listview search ra phía sau
        mBinding.appBar.content.llayoutMainSearch.invalidate()
        val toggle = ActionBarDrawerToggle(this@MainActivity,
                mBinding.drawerLayout, mBinding.appBar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        mapViewEvent()
        mBinding.appBar.skbrHanhchinh.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                imageLayersHanhChinh!!.opacity = i.toFloat() / 100
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        mBinding.appBar.skbrChuyende.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                imageLayersChuyenDe!!.opacity = i.toFloat() / 100
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        findViewById()
        setOnClickListener()
        optionSearchFeature()
        setLoginInfos()
    }

    private fun mapViewEvent() {
        mBinding.appBar.content.mapView.map = ArcGISMap(Basemap.Type.OPEN_STREET_MAP, 10.23851, 105.94032, 12)
        mBinding.appBar.content.mapView.map.addDoneLoadingListener { handlingMapViewDoneLoading() }
        mBinding.appBar.content.mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mBinding.appBar.content.mapView) {
            override fun onLongPress(e: MotionEvent) {
                addGraphicsAddFeature(e)
                super.onLongPress(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                try {
                    if (mapViewHandler != null) mapViewHandler!!.onSingleTapMapView(e)
                } catch (ex: ArcGISRuntimeException) {
                    Log.d("", ex.toString())
                }
                return super.onSingleTapConfirmed(e)
            }

            @SuppressLint("SetTextI18n")
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (mapViewHandler != null) {
                    val location = mapViewHandler!!.onScroll()
                    val log = (location!![0] * 100000).roundToInt().toFloat() / 100000
                    val lat = (location[1] * 100000).roundToInt().toFloat() / 100000
                    mBinding.appBar.content.txtToado.text = "$lat, $log"
                }
                mGraphicsOverlay!!.graphics.clear()
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

        }
        mGraphicsOverlay = GraphicsOverlay()
        mBinding.appBar.content.mapView.graphicsOverlays.add(mGraphicsOverlay)
    }

    private fun setLoginInfos() {
        val displayName = mApplication!!.user!!.displayName
        mBinding.navView.setNavigationItemSelectedListener(this)
        mBinding.navView.getHeaderView(0).findViewById<TextView>(R.id.nav_name_nv).text = displayName
    }

    private fun findViewById() {
        mTxtOpenStreetMap = mBinding.appBar.txtLayerOpenStreetMap
        mTxtStreetMap = mBinding.appBar.txtLayerStreetMap
        mTxtImageWithLabel = mBinding.appBar.txtLayerImage
        mImageOpenStreetMap = mBinding.appBar.imgLayerOpenStreetMap
        mImageStreetMap = mBinding.appBar.imgLayerStreetMap
        mImageImageWithLabel = mBinding.appBar.imgLayerImage
        mBinding.appBar.linearDisplayLayerFeature.removeAllViews()
        mBinding.appBar.linearDisplayLayerAdministration.removeAllViews()
    }

    private fun setOnClickListener() {
        mBinding.appBar.layoutLayerOpenStreetMap.setOnClickListener(this)
        mBinding.appBar.layoutLayerStreetMap.setOnClickListener(this)
        mBinding.appBar.layoutLayerImage.setOnClickListener(this)
        mBinding.appBar.btnLayerClose.setOnClickListener(this)
        mBinding.appBar.content.layoutTimSuCo.setOnClickListener(this)
        mBinding.appBar.content.layoutTimDiaChi.setOnClickListener(this)
        mBinding.appBar.floatBtnLayer.setOnClickListener(this)
        mBinding.appBar.floatBtnLocation.setOnClickListener(this)
    }

    fun addFeature() {
        val intentAdd = Intent(this@MainActivity, AddFeatureActivity::class.java)
        startActivityForResult(intentAdd, Constant.RequestCode.ADD)
    }

    private fun handlingAddFeatureSuccess() {
        handlingCancelAdd()
        mapViewHandler!!.query(String.format(Constant.QUERY_BY_OBJECTID, mApplication!!.diemSuCo!!.objectID))
        mApplication!!.diemSuCo!!.clear()
    }

    fun handlingCancelAdd() {
        if (mPopUp!!.callout != null && mPopUp!!.callout!!.isShowing) {
            mPopUp!!.callout!!.dismiss()
        }
        mGraphicsOverlay!!.graphics.clear()
    }

    private fun addGraphicsAddFeature(vararg e: MotionEvent) {
        val center: Point
        if (e.isEmpty()) center = mBinding.appBar.content.mapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center else {
            center = mBinding.appBar.content.mapView.screenToLocation(android.graphics.Point(Math.round(e[0].x), Math.round(e[0].y)))
            mBinding.appBar.content.mapView.setViewpointCenterAsync(center)
        }
        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.YELLOW, 20F)
        val graphic = Graphic(center, symbol)
        mGraphicsOverlay!!.graphics.clear()
        mGraphicsOverlay!!.graphics.add(graphic)
        if (mPopUp != null) {
            mPopUp!!.showPopupAdd(center)
        }
    }

    fun findRoute() {
        val diachi = mApplication!!.selectedArcGISFeature!!.attributes[Constant.FieldSuCo.DIA_CHI]
        var url: String? = null
        if (diachi != null) {
            url = String.format("google.navigation:q=%s", Uri.encode(diachi.toString()))
            val gmmIntentUri = Uri.parse(url)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        } else {
            Snackbar.make(mBinding.appBar.content.mapView, "Đối tượng được chọn chưa có thông tin vị trí", Snackbar.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handlingMapViewDoneLoading() {
        mLocationDisplay = mBinding.appBar.content.mapView.locationDisplay
        mLocationDisplay!!.startAsync()

//        loginWithPortal1();
        setServices()
    }

    private fun setServices() {
        try {
            // config feature layer service
            mDFeatureLayers = ArrayList()
            for (dLayerInfo in mApplication!!.layerInfos!!) {
                if (dLayerInfo.layerId.substring(dLayerInfo.layerId.length - 3) == "TBL" || !dLayerInfo.isView) continue
                var url = dLayerInfo.url
                if (!dLayerInfo.url.startsWith("http")) url = "http:" + dLayerInfo.url
                if (url == null) continue
                if (dLayerInfo.layerId.toUpperCase().contains(Constant.LAYER_ID.BASEMAP) && imageLayersHanhChinh == null) {
                    if (mPopUp != null) {
                        mURL_HanhChinh = "$url/${mApplication!!.appInfo!!.config.hanhChinhID}"
                        mPopUp!!.setmServiceFeatureTableHanhChinh(mURL_HanhChinh)
                    }
                    imageLayersHanhChinh = ArcGISMapImageLayer(url)
                    imageLayersHanhChinh!!.id = dLayerInfo.layerId
                    mBinding.appBar.content.mapView.map.operationalLayers.add(imageLayersHanhChinh)
                    val finalUrl = url
                    mURL_HanhChinh = "$url/${mApplication!!.appInfo!!.config.hanhChinhID}"
                    imageLayersHanhChinh!!.addDoneLoadingListener {
                        //todo không focus đúng khi chọn tài khoản cái bè
                        for (layer in mBinding.appBar.content.mapView.map.operationalLayers) {
                            if (layer is FeatureLayer) continue
                            val fullExtent = layer.fullExtent
                            if (fullExtent != null && fullExtent.xMin != 0.0 && fullExtent.yMin != 0.0
                                    && fullExtent.xMax != 0.0 && fullExtent.yMax != 0.0) {
                                try {
                                    mBinding.appBar.content.mapView.setViewpointGeometryAsync(fullExtent, 50.0)

                                    break
                                } catch (e: Exception) {
                                    DAlertDialog().show(this@MainActivity, "Thông báo", layer.name + ": " + e.toString())
                                }
                            }
                        }
                        if (imageLayersHanhChinh!!.loadStatus == LoadStatus.LOADED) {
                            val sublayerList: ListenableList<ArcGISSublayer> = imageLayersHanhChinh!!.sublayers
                            for (sublayer in sublayerList) {
                                addCheckBox(sublayer as ArcGISMapImageSublayer, states, colors, true)
                            }
                        }
                    }
                    imageLayersHanhChinh!!.loadAsync()
                } else if (dLayerInfo.layerId.contains(Constant.LAYER_ID.SU_CO)) {
                    val serviceFeatureTable = ServiceFeatureTable(url)
                    mFeatureLayer = FeatureLayer(serviceFeatureTable)
                    if (dLayerInfo.definition.toLowerCase() == "null" || dLayerInfo.definition.toLowerCase() == "1=1") {
                        mFeatureLayer!!.definitionExpression = Constant.DEFINITION_HIDE_COMPLETE +
                                String.format(" and  %s = '%s' or %s = '%s'",
                                        Constant.FieldSuCo.NV_XU_LY, mApplication!!.user!!.username,
                                Constant.Field.CREATED_USER, mApplication!!.user!!.username)
                    } else mFeatureLayer!!.definitionExpression = dLayerInfo.definition
                    mFeatureLayer!!.id = dLayerInfo.layerId
                    mFeatureLayer!!.name = dLayerInfo.layerName
                    mFeatureLayer!!.isPopupEnabled = true
//                    mFeatureLayer!!.minScale = 0.0
                    mFeatureLayer!!.addDoneLoadingListener {
//                        setRendererSuCoFeatureLayer(mFeatureLayer!!)
                        mDFeatureLayer = DFeatureLayer(serviceFeatureTable, mFeatureLayer!!, dLayerInfo)
                        mApplication!!.dFeatureLayer = mDFeatureLayer
                        mDFeatureLayers!!.add(mDFeatureLayer!!)
                        val callout = mBinding.appBar.content.mapView.callout
                        mPopUp = Popup(this@MainActivity, mBinding.appBar.content.mapView,
                                serviceFeatureTable, callout, mGeocoder)
                        DFeatureLayerDiemSuCo = mDFeatureLayer
                        mapViewHandler = MapViewHandler(this, mDFeatureLayer!!, callout, mBinding.appBar.content.mapView, mPopUp!!,
                                this@MainActivity, mGeocoder!!)
                        mapViewHandler!!.setFeatureLayerDTGs(mDFeatureLayers)
                        if (mURL_HanhChinh != null) mPopUp!!.setmServiceFeatureTableHanhChinh(mURL_HanhChinh)
                    }
                    mBinding.appBar.content.mapView.map.operationalLayers.add(mFeatureLayer)
                } else if (dLayerInfo.layerId != "diemdanhgiaLYR" && imageLayersChuyenDe == null) {
                    imageLayersChuyenDe = ArcGISMapImageLayer(url.replaceFirst("FeatureServer(.*)".toRegex(), "MapServer"))
                    imageLayersChuyenDe!!.name = dLayerInfo.layerName
                    imageLayersChuyenDe!!.id = dLayerInfo.layerId
                    mBinding.appBar.content.mapView.map.operationalLayers.add(imageLayersChuyenDe)
                    imageLayersChuyenDe!!.addDoneLoadingListener {
                        if (imageLayersChuyenDe!!.loadStatus == LoadStatus.LOADED) {
                            val sublayerList: ListenableList<ArcGISSublayer> = imageLayersChuyenDe!!.sublayers
                            for (sublayer in sublayerList) {
                                if (sublayer.id == 13L) {
                                    sublayer.isVisible = false
                                } else addCheckBox(sublayer as ArcGISMapImageSublayer, states, colors, false)
                            }
                        }
                    }
                    imageLayersChuyenDe!!.loadAsync()
                }
            }
        } catch (e: Exception) {
            Log.e("error", e.toString())
        }
    }

    private fun addCheckBox(layer: ArcGISMapImageSublayer, states: Array<IntArray>, colors: IntArray, isHanhChinh: Boolean) {
        @SuppressLint("InflateParams") val bindingLayoutFeature = LayoutFeatureBinding.inflate(layoutInflater)
        bindingLayoutFeature.txtLayoutFeature.setTextColor(resources.getColor(android.R.color.black))
        bindingLayoutFeature.txtLayoutFeature.text = layer.name
        bindingLayoutFeature.ckbLayoutFeature.isChecked = false
        layer.isVisible = false
        CompoundButtonCompat.setButtonTintList(bindingLayoutFeature.ckbLayoutFeature, ColorStateList(states, colors))
        bindingLayoutFeature.ckbLayoutFeature.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (buttonView.isChecked) {
                if (bindingLayoutFeature.txtLayoutFeature.text == layer.name) layer.isVisible = true
            } else {
                if (bindingLayoutFeature.txtLayoutFeature.text == layer.name) layer.isVisible = false
            }
        }
        if (!mListLayerID!!.contains(layer.name)) {
            if (isHanhChinh) mBinding.appBar.linearDisplayLayerAdministration.addView(bindingLayoutFeature.root)
            else mBinding.appBar.linearDisplayLayerFeature.addView(bindingLayoutFeature.root)
            mListLayerID!!.add(layer.name)
        }
    }

    private fun setLicense() {
        //way 1
        ArcGISRuntimeEnvironment.setLicense(getString(R.string.license))
    }

    private fun setRendererSuCoFeatureLayer(mSuCoTanHoaLayer: FeatureLayer) {
        val uniqueValueRenderer = UniqueValueRenderer()
        uniqueValueRenderer.fieldNames.add(Constant.FieldSuCo.TRANG_THAI)
        //        uniqueValueRenderer.getFieldNames().add(Constant.FieldSuCo.THONG_TIN_PHAN_ANH);
        val batThuongPMS = PictureMarkerSymbol(Constant.URLImage.BAT_THUONG)
        batThuongPMS.height = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        batThuongPMS.width = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        val moiTiepNhanPMS = PictureMarkerSymbol(Constant.URLImage.MOI_TIEP_NHAN)
        moiTiepNhanPMS.height = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        moiTiepNhanPMS.width = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        val dangSuaPMS = PictureMarkerSymbol(Constant.URLImage.DANG_XU_LY)
        dangSuaPMS.height = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        dangSuaPMS.width = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        val hoanThanhPMS = PictureMarkerSymbol(Constant.URLImage.HOAN_THANH)
        hoanThanhPMS.height = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        hoanThanhPMS.width = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        uniqueValueRenderer.defaultSymbol = moiTiepNhanPMS
        uniqueValueRenderer.defaultLabel = "Chưa xác định"
        val batThuongValue1: MutableList<Any> = ArrayList()
        batThuongValue1.add(Constant.TrangThaiSuCo.MOI_TIEP_NHAN)
        batThuongValue1.add(Constant.ThongTinPhanAnh.KHONG_NUOC)
        val batThuongValue2: MutableList<Any> = ArrayList()
        batThuongValue2.add(Constant.TrangThaiSuCo.MOI_TIEP_NHAN)
        batThuongValue2.add(Constant.ThongTinPhanAnh.XI_DHN)
        val batThuongValue3: MutableList<Any> = ArrayList()
        batThuongValue3.add(Constant.TrangThaiSuCo.MOI_TIEP_NHAN)
        batThuongValue3.add(Constant.ThongTinPhanAnh.ONG_BE)
        val moiTiepNhanValue: MutableList<Any> = ArrayList()
        moiTiepNhanValue.add(Constant.TrangThaiSuCo.MOI_TIEP_NHAN)
        val dangSuaValueNull: MutableList<Any> = ArrayList()
        dangSuaValueNull.add(Constant.TrangThaiSuCo.DANG_SUA)
        //        dangSuaValueNull.add(null);
        val dangSuaValue0: MutableList<Any> = ArrayList()
        dangSuaValue0.add(Constant.TrangThaiSuCo.DANG_SUA)
        dangSuaValue0.add(Constant.ThongTinPhanAnh.KHAC)
        val dangSuaValue1: MutableList<Any> = ArrayList()
        dangSuaValue1.add(Constant.TrangThaiSuCo.DANG_SUA)
        dangSuaValue1.add(Constant.ThongTinPhanAnh.KHONG_NUOC)
        val dangSuaValue2: MutableList<Any> = ArrayList()
        dangSuaValue2.add(Constant.TrangThaiSuCo.DANG_SUA)
        dangSuaValue2.add(Constant.ThongTinPhanAnh.NUOC_DUC)
        val dangSuaValue3: MutableList<Any> = ArrayList()
        dangSuaValue3.add(Constant.TrangThaiSuCo.DANG_SUA)
        dangSuaValue3.add(Constant.ThongTinPhanAnh.NUOC_YEU)
        val dangSuaValue4: MutableList<Any> = ArrayList()
        dangSuaValue4.add(Constant.TrangThaiSuCo.DANG_SUA)
        dangSuaValue4.add(Constant.ThongTinPhanAnh.XI_DHN)
        val dangSuaValue5: MutableList<Any> = ArrayList()
        dangSuaValue5.add(Constant.TrangThaiSuCo.DANG_SUA)
        dangSuaValue5.add(Constant.ThongTinPhanAnh.HU_VAN)
        val dangSuaValue6: MutableList<Any> = ArrayList()
        dangSuaValue6.add(Constant.TrangThaiSuCo.DANG_SUA)
        dangSuaValue6.add(Constant.ThongTinPhanAnh.ONG_BE)
        val hoanThanhValueNull: MutableList<Any> = ArrayList()
        hoanThanhValueNull.add(Constant.TrangThaiSuCo.HOAN_THANH)
        //        hoanThanhValueNull.add(null);
        val hoanThanhValue0: MutableList<Any> = ArrayList()
        hoanThanhValue0.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue0.add(Constant.ThongTinPhanAnh.KHAC)
        val hoanThanhValue1: MutableList<Any> = ArrayList()
        hoanThanhValue1.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue1.add(Constant.ThongTinPhanAnh.KHONG_NUOC)
        val hoanThanhValue2: MutableList<Any> = ArrayList()
        hoanThanhValue2.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue2.add(Constant.ThongTinPhanAnh.NUOC_DUC)
        val hoanThanhValue3: MutableList<Any> = ArrayList()
        hoanThanhValue3.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue3.add(Constant.ThongTinPhanAnh.NUOC_YEU)
        val hoanThanhValue4: MutableList<Any> = ArrayList()
        hoanThanhValue4.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue4.add(Constant.ThongTinPhanAnh.XI_DHN)
        val hoanThanhValue5: MutableList<Any> = ArrayList()
        hoanThanhValue5.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue5.add(Constant.ThongTinPhanAnh.HU_VAN)
        val hoanThanhValue6: MutableList<Any> = ArrayList()
        hoanThanhValue6.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue6.add(Constant.ThongTinPhanAnh.ONG_BE)

//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Bất thường", "Bất thường", batThuongPMS, batThuongValue1));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Bất thường", "Bất thường", batThuongPMS, batThuongValue2));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Bất thường", "Bất thường", batThuongPMS, batThuongValue3));
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Mới tiếp nhận", "Mới tiếp nhận", moiTiepNhanPMS, moiTiepNhanValue))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Đang sửa", "Đang sửa", dangSuaPMS, dangSuaValueNull))
        //        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Đang sửa", "Đang sửa", dangSuaPMS, dangSuaValue0));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Đang sửa", "Đang sửa", dangSuaPMS, dangSuaValue1));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Đang sửa", "Đang sửa", dangSuaPMS, dangSuaValue2));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Đang sửa", "Đang sửa", dangSuaPMS, dangSuaValue3));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Đang sửa", "Đang sửa", dangSuaPMS, dangSuaValue4));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Đang sửa", "Đang sửa", dangSuaPMS, dangSuaValue5));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Đang sửa", "Đang sửa", dangSuaPMS, dangSuaValue6));
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanhPMS, hoanThanhValueNull))
        //        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Hoàn thành", "Hoàn thành", hoanThanhPMS, hoanThanhValue0));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Hoàn thành", "Hoàn thành", hoanThanhPMS, hoanThanhValue1));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Hoàn thành", "Hoàn thành", hoanThanhPMS, hoanThanhValue2));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Hoàn thành", "Hoàn thành", hoanThanhPMS, hoanThanhValue3));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Hoàn thành", "Hoàn thành", hoanThanhPMS, hoanThanhValue4));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Hoàn thành", "Hoàn thành", hoanThanhPMS, hoanThanhValue5));
//        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue(
//                "Hoàn thành", "Hoàn thành", hoanThanhPMS, hoanThanhValue6));
        mSuCoTanHoaLayer.renderer = uniqueValueRenderer
        mSuCoTanHoaLayer.loadAsync()
    }

    private fun setViewPointCenterLongLat(position: Point, location: String) {
        if (mPopUp == null) {
            make(mBinding.appBar.content.mapView, getString(R.string.message_unloaded_map), true)
        } else {
            val geometry = GeometryEngine.project(position, SpatialReferences.getWgs84())
            val geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator())
            val point = geometry1.extent.center
            val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.RED, 20F)
            val graphic = Graphic(point, symbol)
            mGraphicsOverlay!!.graphics.add(graphic)
            mBinding.appBar.content.mapView.setViewpointCenterAsync(point, resources.getInteger(R.integer.SCALE_IMAGE_WITH_LABLES).toDouble())
        }
    }

    private fun optionSearchFeature() {
        mIsSearchingFeature = true
        mBinding.appBar.content.layoutTimSuCo.setBackgroundResource(R.drawable.layout_border_bottom)
        mBinding.appBar.content.layoutTimDiaChi.setBackgroundResource(R.drawable.layout_shape_basemap_none)
    }

    private fun optionFindRoute() {
        mIsSearchingFeature = false
        mBinding.appBar.content.layoutTimDiaChi.setBackgroundResource(R.drawable.layout_border_bottom)
        mBinding.appBar.content.layoutTimSuCo.setBackgroundResource(R.drawable.layout_shape_basemap_none)
    }

    private fun deleteSearching() {
        mGraphicsOverlay!!.graphics.clear()
        mBinding.appBar.content.llayoutMainSearch.removeAllViews()
    }


    private fun visibleFloatActionButton() {
        val navigationView = mBinding.navView
        val menuItem = navigationView.menu.findItem(R.id.nav_visible_float_button)
        if (mBinding.appBar.floatBtnLayer.isShown) {
            mBinding.appBar.floatBtnLayer.hide()
            mBinding.appBar.floatBtnLocation.hide()
            menuItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_action_visible)
            menuItem.setTitle(R.string.nav_hien_thi_nut_chuc_nang)
        } else {
            mBinding.appBar.floatBtnLayer.show()
            mBinding.appBar.floatBtnLocation.show()
            menuItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_action_visible_off)
            menuItem.setTitle(R.string.nav_an_nut_chuc_nang)
        }
    }

    override fun onBackPressed() {
        val drawer = mBinding.drawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } //            super.onBackPressed();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.quan_ly_su_co, menu)
        mTxtSearchView = menu.findItem(R.id.action_search).actionView as SearchView
        mTxtSearchView!!.queryHint = getString(R.string.title_search)
        mTxtSearchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            @RequiresApi(api = Build.VERSION_CODES.N)
            override fun onQueryTextSubmit(query: String): Boolean {
                try {
                    mBinding.appBar.content.llayoutMainSearch.removeAllViews()
                    if (mIsSearchingFeature && mapViewHandler != null) {
                        mPopUp!!.callout!!.dismiss()
                        mFeatureLayer!!.clearSelection()
                        val queryParameters = QueryParameters()
                        val builder = StringBuilder()
                        for (field in mFeatureLayer!!.featureTable.fields) {
                            when (field.fieldType) {
                                Field.Type.OID, Field.Type.INTEGER, Field.Type.SHORT -> try {
                                    val search = query.toInt()
                                    builder.append(String.format("%s = %s", field.name, search))
                                    builder.append(" or ")
                                } catch (ignored: Exception) {
                                }
                                Field.Type.FLOAT, Field.Type.DOUBLE -> try {
                                    val search = query.toDouble()
                                    builder.append(String.format("%s = %s", field.name, search))
                                    builder.append(" or ")
                                } catch (ignored: Exception) {
                                }
                                Field.Type.TEXT -> {
                                    builder.append(field.name).append(" like N'%").append(query).append("%'")
                                    builder.append(" or ")
                                }
                            }
                        }
                        builder.append(" 1 = 2 ")
                        queryParameters.whereClause = builder.toString()
                        QueryServiceFeatureTableGetListAsync(this@MainActivity,
                                mApplication!!.dFeatureLayer!!.serviceFeatureTable,
                                object : QueryServiceFeatureTableGetListAsync.AsyncResponse {
                                    override fun processFinish(output: List<Feature>?) {
                                        if (output != null && output.isNotEmpty()) {
                                            val bindingViews = handleFromItems(this@MainActivity, output, mApplication!!.dFeatureLayer!!.serviceFeatureTable)
                                            for (view in bindingViews) {
                                                val txtID = view.txtId
                                                view.root.setOnClickListener {
                                                    if (mapViewHandler != null) {
                                                        mapViewHandler!!.query(String.format(Constant.QUERY_BY_SUCOID, txtID.text.toString()))
                                                        mBinding.appBar.content.llayoutMainSearch.removeAllViews()
                                                    }
                                                }
                                                mBinding.appBar.content.llayoutMainSearch.addView(view.root)
                                            }
                                        } else {
                                            Snackbar.make(mBinding.appBar.content.mapView, "Không tìm thấy sự cố", Snackbar.LENGTH_LONG).show()
                                        }
                                    }
                                }).execute(queryParameters)

                    } else if (query.isNotEmpty()) {
                        deleteSearching()
                        val findLocationAsycn = FindLocationAsycn(this@MainActivity,
                                true,
                                object : FindLocationAsycn.AsyncResponse {
                                    override fun processFinish(output: List<DAddress>?) {
                                        if (output != null) {
                                            if (output.size > 0) {
                                                for (address in output) {
//                                        DFeature item = new DFeature(new HashMap<>());
//                                        item.getAttributes().put(Constant.FieldSuCo.TRANG_THAI, Constant.TrangThaiSuCo.HOAN_THANH);
//                                        item.getAttributes().put(Constant.LocationField.LOCATION, address.getLocation());
//                                        item.getAttributes().put(Constant.LocationField.LONGTITUDE, address.getLongtitude());
//                                        item.getAttributes().put(Constant.LocationField.LATITUDE, address.getLatitude());
                                                    val layout = ItemTracuuBinding.inflate(layoutInflater)
                                                    layout.txtId.visibility = View.GONE
                                                    layout.txtLocation.text = address.location
                                                    layout.txtTime.visibility = View.GONE
                                                    layout.txtInfo.visibility = View.GONE
                                                    layout.txtObjectid.visibility = View.GONE
                                                    layout.root.setOnClickListener { v: View? -> setViewPointCenterLongLat(Point(address.longtitude, address.latitude), address.location) }
                                                    mBinding.appBar.content.llayoutMainSearch.addView(layout.root)
                                                }

                                                //                                    }
                                            }
                                        }
                                    }
                                }).execute(query)

                    }
                } catch (e: Exception) {
                    Log.e("Lỗi tìm kiếm", e.toString())
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.trim { it <= ' ' }.isNotEmpty() && !mIsSearchingFeature) {
                } else {
                    mBinding.appBar.content.llayoutMainSearch.removeAllViews()
                    mGraphicsOverlay!!.graphics.clear()
                }
                return false
            }
        })
        val mLayoutTimKiem = mBinding.appBar.content.layoutTimKiem
        menu.findItem(R.id.action_search).setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                visibleFloatActionButton()
                mLayoutTimKiem.visibility = View.VISIBLE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                mLayoutTimKiem.visibility = View.INVISIBLE
                visibleFloatActionButton()
                return true
            }
        })
        return true
    }

    private fun showHideComplete() {
        if (mApplication!!.dFeatureLayer!!.getdLayerInfo().definition.toLowerCase() == "null") {
            if (mApplication!!.dFeatureLayer!!.layer.definitionExpression.contains(Constant.DEFINITION_HIDE_COMPLETE)) {
                mApplication!!.dFeatureLayer!!.layer.definitionExpression = null
            } else {
                mApplication!!.dFeatureLayer!!.layer.definitionExpression = (mApplication!!.dFeatureLayer!!.getdLayerInfo().definition
                        + " and " + Constant.DEFINITION_HIDE_COMPLETE)
            }
        } else {
            if (mApplication!!.dFeatureLayer!!.layer.definitionExpression.contains(Constant.DEFINITION_HIDE_COMPLETE)) {
                mApplication!!.dFeatureLayer!!.layer.definitionExpression = mApplication!!.dFeatureLayer!!.getdLayerInfo().definition
            } else {
                mApplication!!.dFeatureLayer!!.layer.definitionExpression = (mApplication!!.dFeatureLayer!!.getdLayerInfo().definition
                        + Constant.DEFINITION_HIDE_COMPLETE)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handleFromFeatures clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_search -> {
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_thongke -> {
                val intent = Intent(this, ListTaskActivity::class.java)
                this.startActivityForResult(intent, Constant.RequestCode.LIST_TASK)
            }
            R.id.nav_reload -> if (isOnline(this)) startMain()
            R.id.nav_show_hide_complete -> showHideComplete()
            R.id.nav_logOut -> startSignIn()
            R.id.nav_delete_searching -> deleteSearching()
            R.id.nav_visible_float_button -> visibleFloatActionButton()
            else -> {
            }
        }
        mBinding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun onClickTextView(v: View) {
        when (v.id) {
            R.id.txt_quanlysuco_hanhchinh -> if (mBinding.appBar.linearDisplayLayerAdministration!!.visibility == View.VISIBLE) {
                mBinding.appBar.skbrHanhchinh!!.visibility = View.GONE
                mBinding.appBar.linearDisplayLayerAdministration!!.visibility = View.GONE
            } else {
                mBinding.appBar.skbrHanhchinh!!.visibility = View.VISIBLE
                mBinding.appBar.linearDisplayLayerAdministration!!.visibility = View.VISIBLE
            }
            R.id.txt_quanlysuco_dulieu -> if (mBinding.appBar.linearDisplayLayerFeature!!.visibility == View.VISIBLE) {
                mBinding.appBar.linearDisplayLayerFeature!!.visibility = View.GONE
                mBinding.appBar.skbrChuyende!!.visibility = View.GONE
            } else {
                mBinding.appBar.linearDisplayLayerFeature!!.visibility = View.VISIBLE
                mBinding.appBar.skbrChuyende!!.visibility = View.VISIBLE
            }
        }
    }

    fun onClickCheckBox(v: View) {
        if (v is CheckBox) {
            val checkBox = v
            when (v.getId()) {
                R.id.ckb_quanlysuco_hanhchinh -> {
                    var i = 0
                    while (i < mBinding.appBar.linearDisplayLayerAdministration!!.childCount) {
                        val view = mBinding.appBar.linearDisplayLayerAdministration!!.getChildAt(i)
                        if (view is LinearLayout) {
                            val layoutFeature = view
                            var j = 0
                            while (j < layoutFeature.childCount) {
                                val view1 = layoutFeature.getChildAt(j)
                                if (view1 is LinearLayout) {
                                    val layoutCheckBox = view1
                                    var k = 0
                                    while (k < layoutCheckBox.childCount) {
                                        val view2 = layoutCheckBox.getChildAt(k)
                                        if (view2 is CheckBox) {
                                            val checkBoxK = view2
                                            if (checkBox.isChecked) checkBoxK.isChecked = true else checkBoxK.isChecked = false
                                        }
                                        k++
                                    }
                                }
                                j++
                            }
                        }
                        i++
                    }
                }
                R.id.ckb_quanlysuco_dulieu -> {
                    var i = 0
                    while (i < mBinding.appBar.linearDisplayLayerFeature!!.childCount) {
                        val view = mBinding.appBar.linearDisplayLayerFeature!!.getChildAt(i)
                        if (view is LinearLayout) {
                            val layoutFeature = view
                            var j = 0
                            while (j < layoutFeature.childCount) {
                                val view1 = layoutFeature.getChildAt(j)
                                if (view1 is LinearLayout) {
                                    val layoutCheckBox = view1
                                    var k = 0
                                    while (k < layoutCheckBox.childCount) {
                                        val view2 = layoutCheckBox.getChildAt(k)
                                        if (view2 is CheckBox) {
                                            val checkBoxK = view2
                                            checkBoxK.isChecked = checkBox.isChecked
                                        }
                                        k++
                                    }
                                }
                                j++
                            }
                        }
                        i++
                    }
                }
            }
        }
    }

    private fun handlingLocation() {
        if (mIsFirstLocating) {
            mIsFirstLocating = false
            mLocationDisplay!!.stop()
            enableLocation()
        } else {
            if (mLocationDisplay!!.isStarted) {
                disableLocation()
            } else if (!mLocationDisplay!!.isStarted) {
                enableLocation()
            }
        }
    }

    private fun disableLocation() {
        mLocationDisplay!!.stop()
    }

    private fun enableLocation() {
        mLocationDisplay!!.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
        mLocationDisplay!!.startAsync()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.layout_tim_su_co -> optionSearchFeature()
            R.id.layout_tim_dia_chi -> optionFindRoute()
            R.id.floatBtnLayer -> {
                v.visibility = View.INVISIBLE
                mBinding.appBar.layoutLayer.visibility = View.VISIBLE
            }
            R.id.layout_layer_open_street_map -> {
                mBinding.appBar.content.mapView.map.maxScale = 1128.497175
                mBinding.appBar.content.mapView.map.basemap = Basemap.createOpenStreetMap()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_open_street_map)
                mBinding.appBar.content.mapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)
            }
            R.id.layout_layer_street_map -> {
                mBinding.appBar.content.mapView.map.maxScale = 1128.497176
                mBinding.appBar.content.mapView.map.basemap = Basemap.createStreets()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_street_map)
            }
            R.id.layout_layer_image -> {
                mBinding.appBar.content.mapView.map.maxScale = resources.getInteger(R.integer.MAX_SCALE_IMAGE_WITH_LABLES).toDouble()
                mBinding.appBar.content.mapView.map.basemap = Basemap.createImageryWithLabels()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_image)
            }
            R.id.btn_layer_close -> {
                mBinding.appBar.layoutLayer.visibility = View.INVISIBLE
                mBinding.appBar.floatBtnLayer.visibility = View.VISIBLE
            }
            R.id.floatBtnLocation -> handlingLocation()
            R.id.imgBtn_timkiemdiachi_themdiemsuco -> {
            }
        }
    }

    private fun getBitmap(path: String?): Bitmap? {
        val uri = Uri.fromFile(File(path))
        var `in`: InputStream?
        return try {
            val IMAGE_MAX_SIZE = 1200000 // 1.2MP
            `in` = contentResolver.openInputStream(uri)

            // Decode image size
            var o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`in`, null, o)
            assert(`in` != null)
            `in`!!.close()
            var scale = 1
            while (o.outWidth * o.outHeight * (1 / Math.pow(scale.toDouble(), 2.0)) > IMAGE_MAX_SIZE) {
                scale++
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight)
            var b: Bitmap?
            `in` = contentResolver.openInputStream(uri)
            if (scale > 1) {
                scale--
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = BitmapFactory.Options()
                o.inSampleSize = scale
                b = BitmapFactory.decodeStream(`in`, null, o)

                // resize to desired dimensions
                val height = b!!.height
                val width = b.width
                Log.d("", "1th scale operation dimenions - width: $width, height: $height")
                val y = Math.sqrt(IMAGE_MAX_SIZE / (width.toDouble() / height))
                val x = y / height * width
                val scaledBitmap = Bitmap.createScaledBitmap(b, x.toInt(), y.toInt(), true)
                b.recycle()
                b = scaledBitmap
                System.gc()
            } else {
                b = BitmapFactory.decodeStream(`in`)
            }
            assert(`in` != null)
            `in`!!.close()
            Log.d("", "bitmap size - width: " + b!!.width + ", height: " + b.height)
            b
        } catch (e: IOException) {
            Log.e("", e.message, e)
            null
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun handlingListTaskActivityResult() {
        //query sự cố theo idsuco, lấy objectid
        val objectID = mApplication!!.diemSuCo!!.objectID
        mapViewHandler!!.query(String.format("%s = %d", Constant.Field.OBJECTID, objectID))
    }

    @SuppressLint("ResourceAsColor")
    private fun handlingColorBackgroundLayerSelected(id: Int) {
        when (id) {
            R.id.layout_layer_open_street_map -> {
                mImageOpenStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap)
                mTxtOpenStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                mImageStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
                mImageImageWithLabel!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtImageWithLabel!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
            }
            R.id.layout_layer_street_map -> {
                mImageStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap)
                mTxtStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                mImageOpenStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtOpenStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
                mImageImageWithLabel!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtImageWithLabel!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
            }
            R.id.layout_layer_image -> {
                mImageImageWithLabel!!.setBackgroundResource(R.drawable.layout_shape_basemap)
                mTxtImageWithLabel!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                mImageOpenStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtOpenStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
                mImageStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            when (requestCode) {
                1 -> if (resultCode == Activity.RESULT_OK && mapViewHandler != null) {
                    val objectid = data!!.getIntExtra(getString(R.string.ket_qua_objectid), 1)
                    mapViewHandler!!.query(String.format(Constant.QUERY_BY_OBJECTID, objectid))
                }
                Constant.RequestCode.LOGIN -> if (resultCode == Activity.RESULT_OK) requestPermisson() else finish()
                Constant.RequestCode.LIST_TASK -> if (resultCode == Activity.RESULT_OK) handlingListTaskActivityResult()
                Constant.RequestCode.ADD -> if (resultCode == Activity.RESULT_OK) {
                    handlingAddFeatureSuccess()
                } else {
                    handlingCancelAdd()
                }
                Constant.RequestCode.UPDATE -> mPopUp!!.refreshPopup(mApplication!!.selectedArcGISFeature)
                Constant.RequestCode.REQUEST_ID_UPDATE_ATTACHMENT -> if (resultCode == Activity.RESULT_OK) {
                    if (mUri != null) {
                        val bitmap = getBitmap(mUri!!.path)
                        try {
                            if (bitmap != null) {
                                val matrix = Matrix()
                                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                val outputStream = ByteArrayOutputStream()
                                rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                val image = outputStream.toByteArray()
                                Toast.makeText(this, "Đã lưu ảnh", Toast.LENGTH_SHORT).show()
                                val updateAttachmentAsync = UpdateAttachmentAsync(this, mSelectedArcGISFeature!!, image)
                                updateAttachmentAsync.execute()
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    make(mBinding.appBar.content.mapView, "Hủy chụp ảnh", false)
                } else {
                    make(mBinding.appBar.content.mapView, "Lỗi khi chụp ảnh", false)
                }
                else -> {
                }
            }
        } catch (ignored: Exception) {
        }
    }

    fun setSelectedArcGISFeature(selectedArcGISFeature: ArcGISFeature?) {
        mSelectedArcGISFeature = selectedArcGISFeature
    }

    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var mUri: Uri? = null
    fun setUri(uri: Uri?) {
        mUri = uri
    }

    companion object {
        var DFeatureLayerDiemSuCo: DFeatureLayer? = null
    }
}