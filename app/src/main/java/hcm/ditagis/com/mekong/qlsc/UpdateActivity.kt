package hcm.ditagis.com.mekong.qlsc

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Field
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hcm.ditagis.com.mekong.qlsc.async.EditAsync
import hcm.ditagis.com.mekong.qlsc.databinding.*
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.utities.Constant
import java.util.*

class UpdateActivity : AppCompatActivity() {
    private lateinit var mApplication: DApplication
    private lateinit var mBinding: ActivityUpdateBinding

    private var mArcGISFeature: ArcGISFeature? = null

    private val attributes: HashMap<String, Any>
        get() {
            val attributes = HashMap<String, Any>()
            var currentFieldName: String
            for (i in 0 until mBinding.llayoutUpdateFeatureField.childCount) {
                val viewI = mBinding.llayoutUpdateFeatureField.getChildAt(i) as LinearLayout
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
                                                if (valueDomain != null) attributes[currentFieldName] = valueDomain.toString()
                                            } else {
                                                attributes[currentFieldName] = viewL.text.toString()
                                            }

                                        }
                                    }
                                } else if (viewK is AppCompatSpinner) {
                                    if (field.domain != null) {
                                        val codedValues = (field.domain as CodedValueDomain).codedValues
                                        val valueDomain = getCodeDomain(codedValues, viewK.selectedItem.toString())
                                        if (valueDomain != null) attributes[currentFieldName] = valueDomain.toString()
                                    }

                                }
                            }

                        }
                    } catch (e: Exception) {

                    }
                }
            }
            return attributes
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mApplication = application as DApplication
        initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun update() {
        mBinding.llayoutUpdateFeatureProgress.visibility = View.VISIBLE
        mBinding.llayoutUpdateFeatureMain.visibility = View.GONE
        mBinding.txtUpdateFeatureProgress.text = "Đang lưu..."
        EditAsync(mBinding.txtUpdateFeatureProgress, this@UpdateActivity, mApplication.dFeatureLayer!!.serviceFeatureTable,
                mApplication.selectedArcGISFeature!!, object : EditAsync.AsyncResponse {
            override fun processFinish(feature: Boolean?) {
                mBinding.llayoutUpdateFeatureProgress.visibility = View.GONE
                mBinding.llayoutUpdateFeatureMain.visibility = View.VISIBLE
                feature?.let {
                    Toast.makeText(this@UpdateActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this@UpdateActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }).execute(attributes)

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

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        mBinding.btnUpdateFeatureUpdate.setOnClickListener { update() }

        mBinding.txtUpdateFeatureProgress.text = "Đang khởi tạo thuộc tính..."
        mBinding.llayoutUpdateFeatureProgress.visibility = View.VISIBLE
        mBinding.llayoutUpdateFeatureMain.visibility = View.GONE
        mArcGISFeature = mApplication.selectedArcGISFeature

        mBinding.swipeUdpateFeature.setOnRefreshListener {
            loadData()
            mBinding.swipeUdpateFeature.isRefreshing = false
        }

        loadData()
    }

    private fun loadData() {

        mBinding.llayoutUpdateFeatureField.removeAllViews()
        mBinding.llayoutUpdateFeatureProgress.visibility = View.VISIBLE
        mBinding.llayoutUpdateFeatureMain.visibility = View.GONE

        for (fieldName in mArcGISFeature!!.attributes.keys) {
            if (Constant.Field.NONE_UPDATE_FIELDS.find { f -> f == fieldName } != null) continue
            val field = mArcGISFeature!!.featureTable.getField(fieldName)
            var value: Any? = null
            if (mArcGISFeature != null) {
                value = mArcGISFeature!!.attributes[field.name]
            }
            if (field.domain != null) {
                val bindingLayoutView = ItemAddFeatureSpinnerBinding.inflate(layoutInflater)
//                val bindingLayoutSpinner = this@UpdateActivity.layoutInflater.inflate(R.layout.item_add_feature_spinner, null, false) as LinearLayout
                val codedValueDomain = field.domain as CodedValueDomain
                val adapter = ArrayAdapter(this@UpdateActivity, android.R.layout.simple_list_item_1, ArrayList<String>())
                bindingLayoutView.spinnerAddSpinnerValue.adapter = adapter
                val values = ArrayList<String>()
                values.add(Constant.EMPTY)
                var selectedValue: String? = null
                for (codedValue in codedValueDomain.codedValues) {
                    values.add(codedValue.name)
                    if (value != null && codedValue.code == value)
                        selectedValue = codedValue.name
                }
                bindingLayoutView.llayoutAddFeatureSpinner.hint = field.alias
                bindingLayoutView.llayoutAddFeatureSpinner.tag = fieldName

                bindingLayoutView.txtSpinTitle.setText(field.alias)
                adapter.addAll(values)
                adapter.notifyDataSetChanged()

                for (i in values.indices) {
                    if (selectedValue != null && values[i] == selectedValue) {
                        bindingLayoutView.spinnerAddSpinnerValue.setSelection(i)
                        break
                    }
                }
                mBinding.llayoutUpdateFeatureField.addView(bindingLayoutView.root)
            } else {
//                val nm = NumberFormat.getCurrencyInstance()
                when (field.fieldType) {
                    Field.Type.INTEGER, Field.Type.SHORT, Field.Type.DOUBLE, Field.Type.FLOAT, Field.Type.TEXT -> {

                        val bindingLayoutView = ItemAddFeatureEdittextBinding.inflate(layoutInflater)
                        bindingLayoutView.llayoutAddFeatureEdittext.hint = field.alias
                        bindingLayoutView.llayoutAddFeatureEdittext.tag = fieldName
                        if (value != null) {
                            bindingLayoutView.etxtNumber.setText(value.toString())
                        }
                        when (field.fieldType) {
                            Field.Type.INTEGER, Field.Type.SHORT -> {
                                bindingLayoutView.etxtNumber.inputType = InputType.TYPE_CLASS_NUMBER
                            }
                            Field.Type.DOUBLE, Field.Type.FLOAT -> {
                                bindingLayoutView.etxtNumber.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                            }
                        }
                        mBinding.llayoutUpdateFeatureField.addView(bindingLayoutView.root)
                    }
                    Field.Type.DATE -> {
                        val bindingLayoutView = ItemAddFeatureDateBinding.inflate(layoutInflater)
                        bindingLayoutView.textInputLayoutAddFeatureDate.hint = field.alias
                        bindingLayoutView.textInputLayoutAddFeatureDate.tag = fieldName
                        if (value != null)
                            bindingLayoutView.editAddDateValue.setText(Constant.DATE_FORMAT.format((value as Calendar).time))
                        bindingLayoutView.btnAddDate.setOnClickListener { selectDate(field, bindingLayoutView) }
                        mBinding.llayoutUpdateFeatureField.addView(bindingLayoutView.root)
                    }

                    else -> {
//                        setViewVisible(layoutView, layoutView.llayout_add_feature_spinner, field)
                    }
                }
            }

        }
        mBinding.llayoutUpdateFeatureProgress.visibility = View.GONE
        mBinding.llayoutUpdateFeatureMain.visibility = View.VISIBLE

    }

    private fun selectDate(field: Field, bindingLayoutView: ItemAddFeatureDateBinding) {
//        mRootView.fab_parent.close(false)
        val dialog = BottomSheetDialog(this@UpdateActivity)
        dialog.setCancelable(true)
        val bindingLayoutSelectTime = LayoutSelectTimeBinding.inflate(layoutInflater)
        val calendar = Calendar.getInstance()

        if (bindingLayoutView.editAddDateValue.text!!.trim().isNotEmpty()) {
            val date = Constant.DATE_FORMAT.parse(bindingLayoutView.editAddDateValue.text!!.trim().toString())

            calendar.time = date

        }
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        bindingLayoutSelectTime.numberPickerYear.value = year
        bindingLayoutSelectTime.numberPickerMonth.value = month
        bindingLayoutSelectTime.numberPickerDay.value = day
        bindingLayoutSelectTime.numberPickerMonth.setOnValueChangedListener { picker, oldVal, newVal ->
            when (newVal) {
                1, 3, 5, 7, 8, 10, 12 -> {
                    bindingLayoutSelectTime.numberPickerDay.maxValue = 31
                }
                4, 6, 9, 11 -> {
                    bindingLayoutSelectTime.numberPickerDay.maxValue = 30
                }
                2 -> {
                    val year = bindingLayoutSelectTime.numberPickerYear.value
                    if (year % 400 == 0 || (year % 4 == 0 && year % 100 > 0)) {
                        //la nam nhuan
                        bindingLayoutSelectTime.numberPickerDay.maxValue = 29
                    } else {
                        bindingLayoutSelectTime.numberPickerDay.maxValue = 28
                    }
                }
            }
        }
        bindingLayoutSelectTime.numberPickerYear.setOnValueChangedListener { picker, oldVal, newVal ->
            val month = bindingLayoutSelectTime.numberPickerMonth.value
            if (month == 2)
                if (newVal % 400 == 0 || (newVal % 4 == 0 && newVal % 100 > 0)) {
                    //la nam nhuan
                    bindingLayoutSelectTime.numberPickerDay.maxValue = 29
                } else {
                    bindingLayoutSelectTime.numberPickerDay.maxValue = 28
                }
        }
        bindingLayoutSelectTime.btnOK.setOnClickListener {
            val year = bindingLayoutSelectTime.numberPickerYear.value
            val month = bindingLayoutSelectTime.numberPickerMonth.value
            val day = bindingLayoutSelectTime.numberPickerDay.value
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, day)
            val date = calendar.time
            bindingLayoutView.editAddDateValue.setText(Constant.DATE_FORMAT.format(date))
            dialog.dismiss()
        }

        dialog.setContentView(bindingLayoutSelectTime.root)

        dialog.show()
    }
}
