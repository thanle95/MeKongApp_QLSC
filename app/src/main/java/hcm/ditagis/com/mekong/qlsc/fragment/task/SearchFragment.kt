package hcm.ditagis.com.mekong.qlsc.fragment.task

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import hcm.ditagis.com.mekong.qlsc.ListTaskActivity
import hcm.ditagis.com.mekong.qlsc.R
import hcm.ditagis.com.mekong.qlsc.async.QueryServiceFeatureTableGetListAsync
import hcm.ditagis.com.mekong.qlsc.databinding.DateTimePickerBinding
import hcm.ditagis.com.mekong.qlsc.databinding.FragmentListTaskSearchBinding
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.utities.Constant
import java.text.ParseException
import java.util.*

@SuppressLint("ValidFragment")
class SearchFragment @SuppressLint("ValidFragment") constructor(activity: ListTaskActivity, inflater: LayoutInflater) : Fragment() {
    private val mActivity: ListTaskActivity = activity
    private var mEtxtAddress: EditText? = null
    private var mSpinTrangThai: Spinner? = null
    private var mTxtThoiGian: TextView? = null
    private var mBtnSearch: Button? = null
    private var mTxtKetQua: TextView? = null
    private var mLLayoutKetQua: LinearLayout? = null
    private lateinit var mApplication: DApplication
    private var mCodeValues: List<CodedValue>? = null
    private var _mBinding: FragmentListTaskSearchBinding? = null
    private val mBinding get() = _mBinding

    private fun initSpinTrangThai() {
        val domain = mApplication.dFeatureLayer!!.layer.featureTable.getField(Constant.FieldSuCo.TRANG_THAI).domain
        if (domain != null) {
            mCodeValues = (domain as CodedValueDomain).codedValues
            if (mCodeValues != null) {
                val codes: MutableList<String> = ArrayList()
                codes.add("Tất cả")
                for (codedValue in mCodeValues!!) {
                    //nếu codedValue là hoàn thành
                    if (codedValue.code as Short == Constant.TrangThaiSuCo.HOAN_THANH) {
                        //và definition chứa nội dung ẩn hoàn thành
                        if (mApplication.dFeatureLayer!!.layer.definitionExpression.contains(Constant.DEFINITION_HIDE_COMPLETE)) //thì continue
                            continue
                    }
                    //ngược lại thì add
                    codes.add(codedValue.name)
                }
                val adapter = ArrayAdapter(mBinding!!.root.context, android.R.layout.simple_list_item_1, codes)
                mSpinTrangThai!!.adapter = adapter
            }
        }
    }

    private fun showDateTimePicker() {
        val bindingView = DateTimePickerBinding.inflate(LayoutInflater.from(mBinding!!.root.context))
        val alertDialog = AlertDialog.Builder(mBinding!!.root.context).create()
        bindingView.dateTimeSet.setOnClickListener { view: View? ->
            val datePicker = bindingView.datePicker
            val calendar: Calendar = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val displaytime = DateFormat.format(Constant.DateFormat.DATE_FORMAT_STRING, calendar.time) as String
            @SuppressLint("SimpleDateFormat") val dateFormatGmt = Constant.DateFormat.DATE_FORMAT_YEAR_FIRST
            //            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            mTxtThoiGian!!.text = displaytime
            alertDialog.dismiss()
        }
        alertDialog.setView(bindingView.root)
        alertDialog.show()
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun traCuu() {
        mLLayoutKetQua!!.removeAllViews()
        var trangThai: Short = -1
        if (mCodeValues != null) for (codedValue in mCodeValues!!) {
            if (codedValue.name == mSpinTrangThai!!.selectedItem.toString()) {
                trangThai = codedValue.code.toString().toShort()
            }
        }
        val queryParameters = QueryParameters()
        @SuppressLint("DefaultLocale") val queryClause = StringBuilder(String.format("( %s like N'%%%s%%' or %s is null)",
                Constant.FieldSuCo.DIA_CHI, mEtxtAddress!!.text.toString(),
                Constant.FieldSuCo.DIA_CHI))
        try {
            val date = Constant.DateFormat.DATE_FORMAT.parse(mTxtThoiGian!!.text.toString())
            queryClause.append(String.format(" and %s > date '%s'", Constant.FieldSuCo.TG_PHAN_ANH, formatTimeToGMT(date)))
        } catch (e: ParseException) {
        }
        if (trangThai.toInt() != -1) {
            queryClause.append(String.format(" and %s = %d",
                    Constant.FieldSuCo.TRANG_THAI, trangThai))
        }
        queryParameters.whereClause = queryClause.toString() + " and " + mApplication.dFeatureLayer!!.layer.definitionExpression
        QueryServiceFeatureTableGetListAsync(mActivity, mApplication.dFeatureLayer!!.serviceFeatureTable,
                object : QueryServiceFeatureTableGetListAsync.AsyncResponse {
                    override fun processFinish(output: List<Feature>?) {
                        if (output != null && output.size > 0) {
                            val bindingViews = HandlingSearchHasDone.handleFromItems(mBinding!!.root.context, output, mApplication.dFeatureLayer!!.serviceFeatureTable)
                            for (bindingView in bindingViews!!) {
                                bindingView.root.setOnClickListener { v: View? -> mActivity.itemClick(bindingView.txtId.text.toString(),
                                        bindingView.txtObjectid.text.toString()) }
                                mLLayoutKetQua!!.addView(bindingView.root)
                            }
                            mTxtKetQua!!.visibility = View.VISIBLE
                            mTxtKetQua!!.text = String.format("Kết quả tra cứu: %d sự cố", mLLayoutKetQua!!.childCount)
                        } else {
                            mTxtKetQua!!.visibility = View.INVISIBLE
                            Toast.makeText(mBinding!!.root.context, "Không có kết quả", Toast.LENGTH_SHORT).show()
                        }
                    }

                }).execute(queryParameters)
    }

    private fun formatTimeToGMT(date: Date): String {
        val dateFormatGmt = Constant.DateFormat.DATE_FORMAT_YEAR_FIRST
        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormatGmt.format(date)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _mBinding = FragmentListTaskSearchBinding.inflate(inflater, container, false)
        mEtxtAddress = mBinding!!.etxtListTaskSearchAddress
        mSpinTrangThai = mBinding!!.spinListTaskSearchTrangThai
        mTxtThoiGian = mBinding!!.txtListTaskSearchThoiGian
        mBtnSearch = mBinding!!.btnListTaskSearch
        mLLayoutKetQua = mBinding!!.llayoutListTaskSearchKetQua
        mTxtKetQua = mBinding!!.txtListTaskKetQua
        mBtnSearch!!.setOnClickListener { view: View -> onClick(view) }
        mTxtThoiGian!!.setOnClickListener { view: View -> onClick(view) }

        mApplication = mActivity.application as DApplication

        initSpinTrangThai()
        return mBinding!!.root
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_list_task_search -> traCuu()
            R.id.txt_list_task_search_thoi_gian -> showDateTimePicker()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }
}