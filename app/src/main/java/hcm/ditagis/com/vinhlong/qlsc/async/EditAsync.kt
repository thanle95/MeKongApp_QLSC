package hcm.ditagis.com.vinhlong.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.esri.arcgisruntime.data.*
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
class EditAsync(private val mActivity: Activity, private val mIsComplete: Boolean,
                selectedArcGISFeature: ArcGISFeature, private val mLLayoutField: LinearLayout, image: ByteArray?,
                delegate: AsyncResponse) : AsyncTask<Void?, ArcGISFeature?, Void?>() {
    @SuppressLint("StaticFieldLeak")
    private val mServiceFeatureTable: ServiceFeatureTable
    private val mFeature: ArcGISFeature
    private val mImage: ByteArray?
    private val mDelegate: AsyncResponse
    private val mApplication: DApplication
    private var mAttributes: HashMap<String, Any?>? = null

    interface AsyncResponse {
        fun processFinish(feature: ArcGISFeature?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        if (mImage == null) mAttributes = attributes
    }

    //        try {

    //        } catch (Exception e) {
//            Log.e("Lỗi lấy attributes", e.toString());
//        }
    private val attributes: HashMap<String, Any?>
        private get() {
            val attributes = HashMap<String, Any?>()
            //        try {
            var currentAlias = ""
            for (i in 0 until mLLayoutField.childCount) {
                val itemAddFeature = mLLayoutField.getChildAt(i) as LinearLayout
                for (j in 0 until itemAddFeature.childCount) {
                    val typeInput_itemAddFeature = itemAddFeature.getChildAt(j) as LinearLayout
                    for (k in 0 until typeInput_itemAddFeature.childCount) {
                        val view = typeInput_itemAddFeature.getChildAt(k)
                        if (view.visibility == View.VISIBLE) {
                            if (view is EditText && !currentAlias.isEmpty()) {
                                for (field in mServiceFeatureTable.fields) {
                                    if (field.alias == currentAlias) {
                                        if (field.domain != null) {
                                            val codedValues = (field.domain as CodedValueDomain).codedValues
                                            val valueDomain = getCodeDomain(codedValues, view.text.toString())
                                            if (valueDomain != null) attributes[currentAlias] = valueDomain.toString()
                                        } else {
                                            attributes[currentAlias] = view.text.toString()
                                        }
                                        break
                                    }
                                }
                            } else if (view is Spinner && !currentAlias.isEmpty()) {
                                for (field in mServiceFeatureTable.fields) {
                                    if (field.alias == currentAlias) {
                                        if (field.domain != null) {
                                            val codedValues = (field.domain as CodedValueDomain).codedValues
                                            val valueDomain = getCodeDomain(codedValues, view.selectedItem.toString())
                                            if (valueDomain != null) attributes[currentAlias] = valueDomain.toString()
                                        } else {
                                        }
                                        break
                                    }
                                }
                            } else if (view is TextView) {
                                currentAlias = view.text.toString()
                                attributes[currentAlias] = null
                            }
                        }
                    }
                }
            }

//        } catch (Exception e) {
//            Log.e("Lỗi lấy attributes", e.toString());
//        }
            return attributes
        }

     override fun doInBackground(vararg params: Void?): Void? {
        if (mImage == null) {
            for (alias in mAttributes!!.keys) {
                for (field in mServiceFeatureTable.fields) {
                    if (field.alias == alias) {
                        try {
                            val value = mAttributes!![alias].toString().trim { it <= ' ' }
                            when (field.fieldType) {
                                Field.Type.TEXT -> mFeature.attributes[field.name] = value
                                Field.Type.DOUBLE -> mFeature.attributes[field.name] = value.toDouble()
                                Field.Type.FLOAT -> mFeature.attributes[field.name] = value.toFloat()
                                Field.Type.INTEGER -> mFeature.attributes[field.name] = value.toInt()
                                Field.Type.SHORT -> mFeature.attributes[field.name] = value.toShort()
                            }
                        } catch (e: Exception) {
                            mFeature.attributes[field.name] = null
                            Log.e("Lỗi thêm điểm", e.toString())
                        }
                        break
                    }
                    when(field.name){
                        Constant.Field.CREATED_DATE, Constant.Field.LAST_EDITED_DATE,
                        Constant.FieldSuCo.TG_PHAN_ANH -> mFeature.attributes[field.name] = Calendar.getInstance()
                        Constant.Field.CREATED_USER, Constant.Field.LAST_EDITED_USER,
                        -> mFeature.attributes[field.name] = mApplication.user!!.userName

                    }
                }
            }
        }
        if (mIsComplete) mFeature.attributes[Constant.FieldSuCo.TRANG_THAI] = Constant.TrangThaiSuCo.HOAN_THANH
        mServiceFeatureTable.loadAsync()
        mServiceFeatureTable.addDoneLoadingListener {
            // update feature in the feature table
            mServiceFeatureTable.updateFeatureAsync(mFeature).addDoneListener {
                mServiceFeatureTable.applyEditsAsync().addDoneListener {
                    if (mImage != null) {
                        if (mFeature.canEditAttachments()) addAttachment() else applyEdit()
                    } else {
                        applyEdit()
                    }
                }
            }
        }
        return null
    }

    private fun addAttachment() {
        val attachmentName = String.format(Constant.AttachmentName.UPDATE,
                mApplication.user!!.userName, System.currentTimeMillis())
        val addResult = mFeature.addAttachmentAsync(mImage, Constant.FileType.PNG, attachmentName)
        addResult.addDoneListener {
            try {
                val attachment = addResult.get()
                if (attachment.size > 0) {
                    val tableResult = mServiceFeatureTable.updateFeatureAsync(mFeature)
                    tableResult.addDoneListener { applyEdit() }
                }
            } catch (ignored: Exception) {
                publishProgress()
            }
        }
    }

    private fun applyEdit() {
        val updatedServerResult = mServiceFeatureTable.applyEditsAsync()
        updatedServerResult.addDoneListener {
            try {
                updatedServerResult.get()
                publishProgress(mFeature)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                publishProgress()
            } catch (e: ExecutionException) {
                e.printStackTrace()
                publishProgress()
            }
        }
    }

    private fun getIdFeatureTypes(featureTypes: List<FeatureType>, value: String): Any? {
        var code: Any? = null
        for (featureType in featureTypes) {
            if (featureType.name == value) {
                code = featureType.id
                break
            }
        }
        return code
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

    override fun onProgressUpdate(vararg values: ArcGISFeature?) {
        super.onProgressUpdate(*values)
        if (values.isNotEmpty()) mDelegate.processFinish(values[0]) else mDelegate.processFinish(null)
    }

    init {
        mApplication = mActivity.application as DApplication
        mServiceFeatureTable = selectedArcGISFeature.featureTable as ServiceFeatureTable
        mDelegate = delegate
        mFeature = selectedArcGISFeature
        mImage = image
    }
}