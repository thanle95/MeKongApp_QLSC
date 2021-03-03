package hcm.ditagis.com.mekong.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Field
import hcm.ditagis.com.mekong.qlsc.R
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.utities.Constant
import hcm.ditagis.com.mekong.qlsc.utities.DAlertDialog
import kotlinx.android.synthetic.main.item_add_feature.view.*
import java.util.*

open class LoadingDataFeatureAsync constructor(context: Activity, fields: List<Field>, delegate: AsyncResponse, vararg arcGISFeatures: ArcGISFeature?) : AsyncTask<Boolean?, Boolean?, Void?>() {
    private val mDelegate: AsyncResponse

    @SuppressLint("StaticFieldLeak")
    private val mActivity: Activity
    private val mFields: List<Field>
    private var mArcGISFeature: ArcGISFeature? = null
    private val mApplication: DApplication = context.application as DApplication

    interface AsyncResponse {
        fun processFinish(views: List<View?>?)
    }

    override fun doInBackground(vararg params: Boolean?): Void? {
        if (params.isNotEmpty()) publishProgress(params[0])
        return null
    }

    override fun onProgressUpdate(vararg values: Boolean?) {
        mDelegate.processFinish(values[0]?.let { loadDataToAdd(it) })
    }

    private fun loadDataToAdd(isAdd: Boolean): List<View?> {
        val views: MutableList<View?> = ArrayList()
        val layoutManager = LinearLayoutManager(mActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val addFields: Array<String> = mActivity.resources.getStringArray(R.array.add_Fields)
//        val updateFields: Array<String> = mApplication.dFeatureLayer!!.getdLayerInfo().updateFieldsArr
        if (isAdd) {
            for (fieldName: String in addFields) {
                if (fieldName.trim { it <= ' ' }.isNotEmpty()) for (field: Field in mFields) if ((field.name == fieldName)) views.add(getView(field, isAdd))
            }
        } else {
            for (field: Field in mArcGISFeature!!.featureTable.fields) {
                views.add(getView(field, isAdd))
            }
        }
        return views
    }

    private fun getView(field: Field, isAdd: Boolean): View {
        val layout: LinearLayout = mActivity.layoutInflater.inflate(R.layout.item_add_feature, null) as LinearLayout
        val spin = layout.llayout_add_feature_spinner.spinner_add_spinner_value
        val adapter: ArrayAdapter<String> = ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, ArrayList())
        spin.adapter = adapter
        var value: Any? = null
        if (mArcGISFeature != null) {
            value = mArcGISFeature!!.attributes[field.name]
        }
        if (field.domain != null) {
            val codedValueDomain: CodedValueDomain = field.domain as CodedValueDomain
            val values: MutableList<String> = ArrayList()
            values.add(Constant.NULL)
            var selectedValue: String? = null
            for (codedValue: CodedValue in codedValueDomain.codedValues) {
                values.add(codedValue.name)
                if (value != null && (codedValue.code == value)) selectedValue = codedValue.name
            }
            layout.llayout_add_feature_number_decimal.visibility = View.GONE
            layout.llayout_add_feature_spinner.visibility = View.VISIBLE
            layout.llayout_add_feature_edittext.visibility = View.GONE
            layout.llayout_add_feature_number.visibility = View.GONE
            layout.llayout_add_feature_spinner.txt_add_spiner_title.text = field.alias
            adapter.addAll(values)
            adapter.notifyDataSetChanged()
            for (i in values.indices) {
                if (selectedValue != null && (values.get(i) == selectedValue)) {
                    spin.setSelection(i)
                    break
                }
            }
        } else when (field.fieldType) {
            Field.Type.INTEGER, Field.Type.SHORT -> {
                layout.llayout_add_feature_number_decimal.visibility = View.GONE
                layout.llayout_add_feature_spinner.visibility = View.GONE
                layout.llayout_add_feature_edittext.visibility = View.GONE
                layout.llayout_add_feature_number.visibility = View.VISIBLE
                layout.llayout_add_feature_number.txt_add_edit_number_title.text = field.alias
                if (value != null) {
                    try {
                        when (field.fieldType) {
                            Field.Type.INTEGER, Field.Type.SHORT -> layout.etxt_add_edit_number_value.setText(value.toString())
                        }
                    } catch (e: Exception) {
                        DAlertDialog().show(mActivity, e)
                    }
                }
            }
            Field.Type.DOUBLE, Field.Type.FLOAT -> {
                layout.llayout_add_feature_number_decimal.visibility = View.VISIBLE
                layout.llayout_add_feature_spinner.visibility = View.GONE
                layout.llayout_add_feature_edittext.visibility = View.GONE
                layout.llayout_add_feature_number.visibility = View.GONE
                layout.llayout_add_feature_number_decimal.txt_add_edit_number_decimal_title.text = field.alias
                if (value != null) {
                    try {
                        when (field.fieldType) {
                            Field.Type.DOUBLE, Field.Type.FLOAT -> layout.etxt_add_edit_number_decimal_value.setText(value.toString())
                        }
                    } catch (e: Exception) {
                        DAlertDialog().show(mActivity, e)
                    }
                }
            }
            Field.Type.TEXT -> {
                layout.llayout_add_feature_number_decimal.visibility = View.GONE
                layout.llayout_add_feature_spinner.visibility = View.GONE
                layout.llayout_add_feature_number.visibility = View.GONE
                layout.llayout_add_feature_edittext.visibility = View.VISIBLE
                layout.llayout_add_feature_edittext.txt_add_edit_text_title.text = field.alias
                if ((field.name == Constant.FieldSuCo.DIA_CHI)) if (isAdd) {
                    value = mApplication.diemSuCo!!.vitri
                }
                if (value != null) {
                    try {
                        layout.edit_add_edittext_value.setText(value.toString())
                    } catch (e: Exception) {
                        DAlertDialog().show(mActivity, e)
                    }
                }
            }
            else -> {
                layout.llayout_add_feature_number_decimal.visibility = View.GONE
                layout.llayout_add_feature_spinner.visibility = View.GONE
                layout.llayout_add_feature_number.visibility = View.GONE
                layout.llayout_add_feature_edittext.visibility = View.GONE
            }
        }
        return layout
    }

    init {
        mActivity = context
        mFields = fields
        mDelegate = delegate
        if (arcGISFeatures != null && arcGISFeatures.isNotEmpty()) mArcGISFeature = arcGISFeatures.get(0)
    }
}