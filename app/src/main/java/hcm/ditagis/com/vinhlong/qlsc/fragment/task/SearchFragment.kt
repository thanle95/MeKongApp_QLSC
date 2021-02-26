package hcm.ditagis.com.vinhlong.qlsc.fragment.task

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
import hcm.ditagis.com.vinhlong.qlsc.ListTaskActivity
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.async.QueryServiceFeatureTableGetListAsync
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import kotlinx.android.synthetic.main.fragment_list_task_search.view.*
import kotlinx.android.synthetic.main.item_tracuu.view.*
import java.text.ParseException
import java.util.*

@SuppressLint("ValidFragment")
class SearchFragment @SuppressLint("ValidFragment") constructor(activity: ListTaskActivity, inflater: LayoutInflater) : Fragment() {
    private val mRootView: View
    private val mActivity: ListTaskActivity
    private var mEtxtAddress: EditText? = null
    private var mSpinTrangThai: Spinner? = null
    private var mTxtThoiGian: TextView? = null
    private var mBtnSearch: Button? = null
    private var mTxtKetQua: TextView? = null
    private var mLLayoutKetQua: LinearLayout? = null
    private val mApplication: DApplication
    private var mCodeValues: List<CodedValue>? = null
    private fun init() {
        mEtxtAddress = mRootView.findViewById(R.id.etxt_list_task_search_address)
        mSpinTrangThai = mRootView.findViewById(R.id.spin_list_task_search_trang_thai)
        mTxtThoiGian = mRootView.findViewById(R.id.txt_list_task_search_thoi_gian)
        mBtnSearch = mRootView.btn_list_task_search
        mLLayoutKetQua = mRootView.findViewById(R.id.llayout_list_task_search_ket_qua)
        mTxtKetQua = mRootView.findViewById(R.id.txt_list_task_ket_qua)
        mBtnSearch!!.setOnClickListener(View.OnClickListener { view: View -> onClick(view) })
        mTxtThoiGian!!.setOnClickListener(View.OnClickListener { view: View -> onClick(view) })
        initSpinTrangThai()
    }

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
                val adapter = ArrayAdapter(mRootView.context, android.R.layout.simple_list_item_1, codes)
                mSpinTrangThai!!.adapter = adapter
            }
        }
    }

    private fun showDateTimePicker() {
        val dialogView = View.inflate(mRootView.context, R.layout.date_time_picker, null)
        val alertDialog = AlertDialog.Builder(mRootView.context).create()
        dialogView.findViewById<View>(R.id.date_time_set).setOnClickListener { view: View? ->
            val datePicker = dialogView.findViewById<DatePicker>(R.id.date_picker)
            val calendar: Calendar = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val displaytime = DateFormat.format(Constant.DateFormat.DATE_FORMAT_STRING, calendar.time) as String
            @SuppressLint("SimpleDateFormat") val dateFormatGmt = Constant.DateFormat.DATE_FORMAT_YEAR_FIRST
            //            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            mTxtThoiGian!!.text = displaytime
            alertDialog.dismiss()
        }
        alertDialog.setView(dialogView)
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
                            val views = HandlingSearchHasDone.handleFromItems(mRootView.context, output, mApplication.dFeatureLayer!!.serviceFeatureTable)
                            for (view in views!!) {
                                val txtID = view!!.findViewById<TextView>(R.id.txt_id)
                                view.setOnClickListener { v: View? -> mActivity.itemClick(txtID.text.toString(), view.txt_objectid.text.toString()) }
                                mLLayoutKetQua!!.addView(view)
                            }
                            mTxtKetQua!!.visibility = View.VISIBLE
                            mTxtKetQua!!.text = String.format("Kết quả tra cứu: %d sự cố", mLLayoutKetQua!!.childCount)
                        } else {
                            mTxtKetQua!!.visibility = View.INVISIBLE
                            Toast.makeText(mRootView.context, "Không có kết quả", Toast.LENGTH_SHORT).show()
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
        return mRootView
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_list_task_search -> traCuu()
            R.id.txt_list_task_search_thoi_gian -> showDateTimePicker()
        }
    }

    init {
        mRootView = inflater.inflate(R.layout.fragment_list_task_search, null)
        mActivity = activity
        mApplication = activity.application as DApplication
        init()
    }
}