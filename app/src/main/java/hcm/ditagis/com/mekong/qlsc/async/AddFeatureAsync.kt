package hcm.ditagis.com.mekong.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import com.esri.arcgisruntime.data.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Created by ThanLe on 4/16/2018.
 */
@SuppressLint("StaticFieldLeak")
class AddFeatureAsync(private val mActivity: Activity,
                      private val mServiceFeatureTable: ServiceFeatureTable, layout: LinearLayout,
                      delegate: AsyncResponse) : AsyncTask<Void?, Feature?, Void?>() {

    @SuppressLint("StaticFieldLeak")
    private val mDelegate: AsyncResponse
    private val mApplication: DApplication
    private val mLLayoutField: LinearLayout
    private var mAttributes: HashMap<String, Any?>? = null
    private var mThongTinPhanAnh: Any? = null
    private var mGhiChu: String? = null

    interface AsyncResponse {
        fun processFinish(output: Feature?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mAttributes = attributes
    }

     override fun doInBackground(vararg params: Void?): Void? {
         val executor: ExecutorService = Executors.newSingleThreadExecutor()
         val handler = Handler(Looper.getMainLooper())

         executor.execute {
             //Background work here
             handler.post {

             }
         }
         val feature: Feature
        try {
            feature = mServiceFeatureTable.createFeature()
            feature.geometry = mApplication.addFeaturePoint
            for (field in mServiceFeatureTable.fields) {
                for (alias in mAttributes!!.keys) {
                    if (field.alias == alias) {
                        try {
                            val value = mAttributes!![alias].toString().trim { it <= ' ' }
                            if (value.isEmpty()) continue
                            when (field.fieldType) {
                                Field.Type.TEXT -> feature.attributes[field.name] = value
                                Field.Type.DOUBLE -> feature.attributes[field.name] = value.toDouble()
                                Field.Type.FLOAT -> feature.attributes[field.name] = value.toFloat()
                                Field.Type.INTEGER -> feature.attributes[field.name] = value.toInt()
                                Field.Type.SHORT -> feature.attributes[field.name] = value.toShort()
                            }
                        } catch (e: Exception) {
                            Log.e("Lỗi thêm điểm", e.toString())
                        }
                        break
                    }
                }
                when(field.name){
                    Constant.Field.CREATED_DATE, Constant.Field.LAST_EDITED_DATE,
                    Constant.FieldSuCo.TG_PHAN_ANH -> feature.attributes[field.name] = Calendar.getInstance()
                    Constant.Field.CREATED_USER, Constant.Field.LAST_EDITED_USER -> feature.attributes[field.name] = mApplication.user!!.username
                    Constant.FieldSuCo.TRANG_THAI -> feature.attributes[field.name] = Constant.TrangThaiSuCo.MOI_TIEP_NHAN
                }
            }
            var queryParameters = QueryParameters()
            queryParameters.geometry = mApplication.addFeaturePoint
//                    queryParameters.whereClause = "1 = 1"
            var listenableFuture = mApplication.mSFTAdministrator!!.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
            listenableFuture.addDoneListener {
                try {
                    var featureQueryResult = listenableFuture.get()
                    val iterator = featureQueryResult.iterator()

                    while (iterator.hasNext()) {
                        val featureHanhChinh = iterator.next() as Feature
                        feature.attributes[Constant.FieldSuCo.MA_QUAN] = featureHanhChinh.attributes[
                                mApplication.appInfo!!.config.MaHuyen]
                        feature.attributes[Constant.FieldSuCo.MA_PHUONG] = featureHanhChinh.attributes[
                                mApplication.appInfo!!.config.IDHanhChinh]
                    }
                    addFeatureAsync(feature)
                }
                catch (e: Exception){
//                            publishProgress(e.toString())
                    addFeatureAsync(feature)
                }
            }
//            addFeatureAsync(feature)
        } catch (e: Exception) {
            publishProgress()
        }
        return null
    }

    private fun getCodeDomain(codedValues: List<CodedValue>, value: String): Any? {
        var code: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.name == value) {
                code = codedValue.code
                break
            }
        }
        return code
    }

    private fun addFeatureAsync(feature: Feature) {
        val mapViewResult = mServiceFeatureTable.addFeatureAsync(feature)
        mapViewResult.addDoneListener {
            val listListenableEditAsync = mServiceFeatureTable.applyEditsAsync()
            listListenableEditAsync.addDoneListener {
                try {
                    val edits = listListenableEditAsync.get()
                    if (edits != null && edits.size > 0) {
                        if (!edits[0].hasCompletedWithErrors()) {
                            val objectId = edits[0].objectId
                            val queryParameters = QueryParameters()
                            val query = String.format("%s = %d", Constant.Field.OBJECTID, objectId)
                            queryParameters.whereClause = query
                            val featuresAsync = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                            featuresAsync.addDoneListener {
                                try {
                                    val result = featuresAsync.get()
                                    if (result.iterator().hasNext()) {
                                        val item = result.iterator().next()
                                        mApplication.diemSuCo!!.objectID = objectId
                                        if (mApplication.images != null && mApplication.images!!.size > 0) addAttachment(item as ArcGISFeature, item) else publishProgress(item)
                                    } else publishProgress()
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                    publishProgress()
                                } catch (e: ExecutionException) {
                                    e.printStackTrace()
                                    publishProgress()
                                }
                            }
                        } else {
                            publishProgress()
                        }
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    publishProgress()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                    publishProgress()
                }
            }
        }
    }

    //        try {
    //        } catch (Exception e) {
//            Log.e("Lỗi lấy attributes", e.toString());
//        }

    private val attributes: HashMap<String, Any?>
        get() {
            val attributes = HashMap<String, Any?>()
            var currentFieldName: String
            var hasValue = false
            for (i in 0 until mLLayoutField.childCount) {
                val viewI = mLLayoutField.getChildAt(i) as LinearLayout
                for (j in 0 until viewI.childCount) {
                    try {
                        val viewJ = viewI.getChildAt(j) as TextInputLayout
                        if (viewJ.visibility == View.VISIBLE
                                && viewJ.hint != null) {
                            val fieldName = viewJ.tag.toString()
                            val field = mApplication.dFeatureLayer!!.serviceFeatureTable.getField(fieldName)
                            currentFieldName = fieldName
                            if (currentFieldName.isEmpty()) continue
                            for (k in 0 until viewJ.childCount) {
                                val viewK = viewJ.getChildAt(k)
                                if (viewK is FrameLayout) {
                                    for (l in 0 until viewK.childCount) {
                                        val viewL = viewK.getChildAt(l)
                                        if (viewL is TextInputEditText) {
                                            if (field.domain != null) {
                                                val codedValues = (field.domain as CodedValueDomain).codedValues

                                                val valueDomain = getCodeDomain(codedValues, viewL.text.toString())
                                                if (valueDomain != null) {
                                                    attributes[currentFieldName] = valueDomain.toString()
                                                    hasValue = true
                                                }
                                            } else {
                                                attributes[currentFieldName] = viewL.text.toString()
                                                hasValue = true
                                            }

                                        }
                                    }
                                } else if (viewK is AppCompatSpinner) {
                                    if (field.domain != null) {
                                        val codedValues = (field.domain as CodedValueDomain).codedValues
                                        val valueDomain = getCodeDomain(codedValues, viewK.selectedItem.toString())
                                        if (valueDomain != null) {
                                            attributes[currentFieldName] = valueDomain.toString()
                                            hasValue = true
                                        }
                                    }

                                }
                            }

                        }
                    } catch (e: Exception) {

                    }
                }
            }
            if (!hasValue) publishProgress()
            return attributes
        }


    private fun addAttachment(arcGISFeature: ArcGISFeature, feature: Feature) {
        for (image in mApplication.images!!) {
            val attachmentName = String.format(Constant.AttachmentName.UPDATE,
                    mApplication.user!!.username, System.currentTimeMillis())
            val addResult = arcGISFeature.addAttachmentAsync(
                    image, Constant.FileType.PNG, attachmentName)
        }
        val tableResult = mServiceFeatureTable.updateFeatureAsync(arcGISFeature)
        //            tableResult.addDoneListener(() -> {
        val updatedServerResult = mServiceFeatureTable.applyEditsAsync()
        updatedServerResult.addDoneListener {
            try {
                val edits = updatedServerResult.get()
                if (edits.size > 0) {
                    if (!edits[0].hasCompletedWithErrors()) {
                        publishProgress(feature)
                    } else publishProgress()
                } else publishProgress()
            } catch (e: InterruptedException) {
                publishProgress()
                e.printStackTrace()
            } catch (e: ExecutionException) {
                publishProgress()
                e.printStackTrace()
            }
        }
    }

     override fun onProgressUpdate(vararg values: Feature?) {
        if (values == null || values.size == 0) {
            Toast.makeText(mActivity.applicationContext, "Nhập thiếu dữ liệu hoặc có lỗi xảy ra", Toast.LENGTH_SHORT).show()
            mDelegate.processFinish(null)
        } else if (values.size > 0) mDelegate.processFinish(values[0])
    }

    override fun onPostExecute(result: Void?) {}

    init {
        mApplication = mActivity.application as DApplication
        mDelegate = delegate
        mLLayoutField = layout
    }
}