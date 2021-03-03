package hcm.ditagis.com.mekong.qlsc.utities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.location.Geocoder
import android.view.MotionEvent
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.MapView
import hcm.ditagis.com.mekong.qlsc.MainActivity
import hcm.ditagis.com.mekong.qlsc.async.SingleTapMapViewAsync
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.entities.entitiesDB.DFeatureLayer

/**
 * Created by ThanLe on 2/2/2018.
 */
@SuppressLint("Registered")
class MapViewHandler(private val mActivity: MainActivity, DFeatureLayer: DFeatureLayer, mCallout: Callout, mapView: MapView,
                     popupInfos: Popup, mContext: Context, geocoder: Geocoder) : Activity() {
    private val suCoLayer: FeatureLayer?
    private val mCallout: Callout
    private var mClickPoint: Point? = null
    private val mSelectedArcGISFeature: ArcGISFeature? = null
    private val mMapView: MapView
    private var isClickBtnAdd = false
    private val mServiceFeatureTable: ServiceFeatureTable
    private val mPopUp: Popup
    private val mContext: Context
    private val mGeocoder: Geocoder
    private val mApplication: DApplication
    fun setFeatureLayerDTGs(mDFeatureLayers: List<DFeatureLayer>?) {
        this.mDFeatureLayers = mDFeatureLayers
    }

    private var mDFeatureLayers: List<DFeatureLayer>? = null
    fun setClickBtnAdd(clickBtnAdd: Boolean) {
        isClickBtnAdd = clickBtnAdd
    }

    fun onScroll(): DoubleArray? {
        val targetGeometry = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry
        if (targetGeometry != null) {
            val center = targetGeometry.extent.center
            val project = GeometryEngine.project(center, SpatialReferences.getWgs84())
            return doubleArrayOf(project.extent.center.x, project.extent.center.y)
        }
        return null
    }

    fun onSingleTapMapView(e: MotionEvent) {
        val clickPoint = mMapView.screenToLocation(Point(Math.round(e.x), Math.round(e.y)))
        mClickPoint = Point(e.x.toInt(), e.y.toInt())
        if (isClickBtnAdd) {
            mMapView.setViewpointCenterAsync(clickPoint, 10.0)
        } else {
            mDFeatureLayers?.let { SingleTapMapViewAsync(mActivity, it, mPopUp, mClickPoint!!, mMapView) }?.execute(clickPoint)
        }
    }

    fun query(query: String?) {
        val queryParameters = QueryParameters()
        queryParameters.whereClause = query
        val feature: ListenableFuture<FeatureQueryResult>
        feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        feature.addDoneListener {
            try {
                val result = feature.get()
                if (result.iterator().hasNext()) {
                    val item = result.iterator().next()
                    if (item != null) {
                        if (item.geometry != null) {
                            val extent = item.geometry.extent
                            mApplication.geometry = item.geometry
                            mMapView.setViewpointGeometryAsync(extent)
                        }
//                        if (suCoLayer != null) {
//                            suCoLayer.selectFeature(item)
//                            val queryClause = String.format("%s = '%s' ",
//                                    FieldSuCo.ID_SUCO, item.attributes[FieldSuCo.ID_SUCO].toString())
//                            val queryParameters1 = QueryParameters()
//                            queryParameters1.whereClause = queryClause
//                            QueryServiceFeatureTableAsync(mActivity,object: QueryServiceFeatureTableAsync.AsyncResponse {
//                                override fun processFinish(output: Feature?) {
//                                    if (output != null) {
                                        mApplication.selectedArcGISFeature = item as ArcGISFeature?
                                        mPopUp.showPopup()
//                                    }
//                                }
//                            }).execute(queryParameters1)
//                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        mApplication = mActivity.application as DApplication
        this.mCallout = mCallout
        mMapView = mapView
        mServiceFeatureTable = DFeatureLayer.layer.featureTable as ServiceFeatureTable
        mPopUp = popupInfos
        this.mContext = mContext
        suCoLayer = DFeatureLayer.layer
        mGeocoder = geocoder
    }
}