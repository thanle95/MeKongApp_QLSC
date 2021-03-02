package hcm.ditagis.com.vinhlong.qlsc.async

import android.app.Activity
import android.os.AsyncTask
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Field
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import hcm.ditagis.com.vinhlong.qlsc.utities.DAlertDialog
import java.util.*

open class LoadingDataFeatureAsync constructor(context: Activity, fields: List<Field>, delegate: AsyncResponse, vararg arcGISFeatures: ArcGISFeature?) : AsyncTask<Boolean?, Boolean?, Void?>() {
    private val mDelegate: AsyncResponse
    private val mActivity: Activity
    private val mFields: List<Field>
    private var mArcGISFeature: ArcGISFeature? = null
    private val mApplication: DApplication

    open interface AsyncResponse {
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
        val addFields: Array<String> = mActivity.getResources().getStringArray(R.array.add_Fields)
//        val updateFields: Array<String> = mApplication.dFeatureLayer!!.getdLayerInfo().updateFieldsArr
        if (isAdd) {
            for (fieldName: String in addFields) {
                if (fieldName.trim { it <= ' ' }.isNotEmpty()) for (field: Field in mFields) if ((field.name == fieldName)) views.add(getView(field, isAdd))
            }
        }else {
            for (field: Field in mArcGISFeature!!.featureTable.fields) {
                views.add(getView(field, isAdd))
            }
        }
        return views
    }

    private fun getView(field: Field, isAdd: Boolean): View {
        val layoutView: LinearLayout = mActivity.layoutInflater.inflate(R.layout.item_add_feature, null) as LinearLayout
        val layoutEditNumber: LinearLayout = layoutView.findViewById(R.id.llayout_add_feature_number)
        val layoutEditNumberDecimal: LinearLayout = layoutView.findViewById(R.id.llayout_add_feature_number_decimal)
        val layoutEditSpinner: LinearLayout = layoutView.findViewById(R.id.llayout_add_feature_spinner)
        val layoutEditText: LinearLayout = layoutView.findViewById(R.id.llayout_add_feature_edittext)
        val spin: Spinner = layoutEditSpinner.findViewById(R.id.spinner_add_spinner_value)
        val adapter: ArrayAdapter<String> = ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, ArrayList())
        spin.setAdapter(adapter)
        var value: Any? = null
        if (mArcGISFeature != null) {
            value = mArcGISFeature!!.attributes[field.name]
        }
        if (field.getDomain() != null) {
            val codedValueDomain: CodedValueDomain = field.getDomain() as CodedValueDomain
            val values: MutableList<String> = ArrayList()
            values.add(Constant.NULL)
            var selectedValue: String? = null
            for (codedValue: CodedValue in codedValueDomain.getCodedValues()) {
                values.add(codedValue.getName())
                if (value != null && (codedValue.getCode() == value)) selectedValue = codedValue.getName()
            }
            layoutEditNumberDecimal.setVisibility(View.GONE)
            layoutEditSpinner.setVisibility(View.VISIBLE)
            layoutEditText.setVisibility(View.GONE)
            layoutEditNumber.setVisibility(View.GONE)
            val textViewSpin: TextView = layoutEditSpinner.findViewById(R.id.txt_add_spiner_title)
            textViewSpin.setText(field.getAlias())
            adapter.addAll(values)
            adapter.notifyDataSetChanged()
            for (i in values.indices) {
                if (selectedValue != null && (values.get(i) == selectedValue)) {
                    spin.setSelection(i)
                    break
                }
            }
        } else when (field.getFieldType()) {
            Field.Type.INTEGER, Field.Type.SHORT -> {
                layoutEditNumberDecimal.setVisibility(View.GONE)
                layoutEditSpinner.setVisibility(View.GONE)
                layoutEditText.setVisibility(View.GONE)
                layoutEditNumber.setVisibility(View.VISIBLE)
                val textViewNumber: TextView = layoutEditNumber.findViewById(R.id.txt_add_edit_number_title)
                textViewNumber.setText(field.getAlias())
                if (value != null) {
                    val editTextNumber: EditText = layoutView.findViewById(R.id.etxt_add_edit_number_value)
                    try {
                        when (field.getFieldType()) {
                            Field.Type.INTEGER, Field.Type.SHORT -> editTextNumber.setText(value.toString())
                        }
                    } catch (e: Exception) {
                        DAlertDialog().show(mActivity, e)
                    }
                }
            }
            Field.Type.DOUBLE, Field.Type.FLOAT -> {
                layoutEditNumberDecimal.setVisibility(View.VISIBLE)
                layoutEditSpinner.setVisibility(View.GONE)
                layoutEditText.setVisibility(View.GONE)
                layoutEditNumber.setVisibility(View.GONE)
                val textViewNumberDecimal: TextView = layoutEditNumberDecimal.findViewById(R.id.txt_add_edit_number_decimal_title)
                textViewNumberDecimal.setText(field.getAlias())
                if (value != null) {
                    val editTextNumberDecimal: EditText = layoutView.findViewById(R.id.etxt_add_edit_number_decimal_value)
                    try {
                        when (field.getFieldType()) {
                            Field.Type.DOUBLE, Field.Type.FLOAT -> editTextNumberDecimal.setText(value.toString())
                        }
                    } catch (e: Exception) {
                        DAlertDialog().show(mActivity, e)
                    }
                }
            }
            Field.Type.TEXT -> {
                layoutEditNumberDecimal.setVisibility(View.GONE)
                layoutEditSpinner.setVisibility(View.GONE)
                layoutEditNumber.setVisibility(View.GONE)
                layoutEditText.setVisibility(View.VISIBLE)
                val textViewEditText: TextView = layoutEditText.findViewById(R.id.txt_add_edit_text_title)
                textViewEditText.setText(field.getAlias())
                if ((field.getName() == Constant.FieldSuCo.DIA_CHI)) if (isAdd) {
                    value = mApplication.diemSuCo!!.vitri
                }
                if (value != null) {
                    val editText: EditText = layoutView.findViewById(R.id.edit_add_edittext_value)
                    try {
                        editText.setText(value.toString())
                    } catch (e: Exception) {
                        DAlertDialog().show(mActivity, e)
                    }
                }
            }
            else -> {
                layoutEditNumberDecimal.setVisibility(View.GONE)
                layoutEditSpinner.setVisibility(View.GONE)
                layoutEditNumber.setVisibility(View.GONE)
                layoutEditText.setVisibility(View.GONE)
            }
        }
        return layoutView
    }

    init {
        mApplication = context.getApplication() as DApplication
        mActivity = context
        mFields = fields
        mDelegate = delegate
        if (arcGISFeatures != null && arcGISFeatures.size > 0) mArcGISFeature = arcGISFeatures.get(0)
    }
}