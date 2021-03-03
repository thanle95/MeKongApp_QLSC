package hcm.ditagis.com.mekong.qlsc

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.Window
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import hcm.ditagis.com.mekong.qlsc.adapter.ThongKeAdapter
import hcm.ditagis.com.mekong.qlsc.utities.TimePeriodReport
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException

class ThongKeActivity : AppCompatActivity() {
    private var mTxtTongSuCo: TextView? = null
    private var mTxtChuaSua: TextView? = null
    private var mTxtDangSua: TextView? = null
    private var mTxtHoanThanh: TextView? = null
    private var mTxtPhanTramChuaSua: TextView? = null
    private var mTxtPhanTramDangSua: TextView? = null
    private var mTxtPhanTramHoanThanh: TextView? = null
    private val mServiceFeatureTable: ServiceFeatureTable? = null
    private var mThongKeAdapter: ThongKeAdapter? = null
    private var mChuaSuaChua = 0
    private var mDangSuaChua = 0
    private var mHoanThanh = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thong_ke)
        val timePeriodReport = TimePeriodReport(this)
        val items = timePeriodReport.getItems()
        mThongKeAdapter = ThongKeAdapter(this, items)
        mTxtTongSuCo = findViewById(R.id.txtTongSuCo)
        mTxtChuaSua = findViewById(R.id.txtChuaSua)
        mTxtDangSua = findViewById(R.id.txtDangSua)
        mTxtHoanThanh = findViewById(R.id.txtHoanThanh)
        mTxtPhanTramChuaSua = findViewById(R.id.txtPhanTramChuaSua)
        mTxtPhanTramDangSua = findViewById(R.id.txtPhanTramDangSua)
        mTxtPhanTramHoanThanh = findViewById(R.id.txtPhanTramHoanThanh)
        findViewById<View>(R.id.layout_thongke_thoigian).setOnClickListener { v: View? -> showDialogSelectTime() }
        query(items[0])
    }

    private fun showDialogSelectTime() {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        @SuppressLint("InflateParams") val layout = layoutInflater.inflate(R.layout.layout_listview_thongketheothoigian, null)
        val listView = layout.findViewById<ListView>(R.id.lstView_thongketheothoigian)
        listView.adapter = mThongKeAdapter
        builder.setView(layout)
        val selectTimeDialog = builder.create()
        selectTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        selectTimeDialog.show()
        val finalItems = mThongKeAdapter!!.getItems()
        listView.onItemClickListener = OnItemClickListener { parent: AdapterView<*>, view: View?, position: Int, id: Long ->
            val itemAtPosition = parent.getItemAtPosition(position) as ThongKeAdapter.Item
            selectTimeDialog.dismiss()
            if (itemAtPosition.id == finalItems.size) {
                val builder1 = AlertDialog.Builder(this@ThongKeActivity, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
                @SuppressLint("InflateParams") val layout1 = layoutInflater.inflate(R.layout.layout_thongke_thoigiantuychinh, null)
                builder1.setView(layout1)
                val tuychinhDateDialog = builder1.create()
                tuychinhDateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                tuychinhDateDialog.show()
                val edit_thongke_tuychinh_ngaybatdau = layout1.findViewById<EditText>(R.id.edit_thongke_tuychinh_ngaybatdau)
                val edit_thongke_tuychinh_ngayketthuc = layout1.findViewById<EditText>(R.id.edit_thongke_tuychinh_ngayketthuc)
                if (itemAtPosition.thoigianbatdau != null) edit_thongke_tuychinh_ngaybatdau.setText(itemAtPosition.thoigianbatdau)
                if (itemAtPosition.thoigianketthuc != null) edit_thongke_tuychinh_ngayketthuc.setText(itemAtPosition.thoigianketthuc)
                val finalThoigianbatdau = StringBuilder()
                finalThoigianbatdau.append(itemAtPosition.thoigianbatdau)
                edit_thongke_tuychinh_ngaybatdau.setOnClickListener { v: View? -> showDateTimePicker(edit_thongke_tuychinh_ngaybatdau, finalThoigianbatdau, "START") }
                val finalThoigianketthuc = StringBuilder()
                finalThoigianketthuc.append(itemAtPosition.thoigianketthuc)
                edit_thongke_tuychinh_ngayketthuc.setOnClickListener { v: View? -> showDateTimePicker(edit_thongke_tuychinh_ngayketthuc, finalThoigianketthuc, "FINISH") }
                layout1.findViewById<View>(R.id.btn_layngaythongke).setOnClickListener { v: View? ->
                    if (kiemTraThoiGianNhapVao(finalThoigianbatdau.toString(), finalThoigianketthuc.toString())) {
                        tuychinhDateDialog.dismiss()
                        itemAtPosition.thoigianbatdau = finalThoigianbatdau.toString()
                        itemAtPosition.thoigianketthuc = finalThoigianketthuc.toString()
                        itemAtPosition.thoigianhienthi = edit_thongke_tuychinh_ngaybatdau.text.toString() + " - " + edit_thongke_tuychinh_ngayketthuc.text
                        mThongKeAdapter!!.notifyDataSetChanged()
                        query(itemAtPosition)
                    }
                }
            } else {
                query(itemAtPosition)
            }
        }
    }

    private fun kiemTraThoiGianNhapVao(startDate: String, endDate: String): Boolean {
        if (startDate == "" || endDate == "") return false
        @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        try {
            val date1 = dateFormat.parse(startDate)
            val date2 = dateFormat.parse(endDate)
            return !date1.after(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun showDateTimePicker(editText: EditText, output: StringBuilder, typeInput: String) {
        output.delete(0, output.length)
        val dialogView = View.inflate(this, R.layout.date_time_picker, null)
        val alertDialog = android.app.AlertDialog.Builder(this).create()
        dialogView.findViewById<View>(R.id.date_time_set).setOnClickListener { view: View? ->
            val datePicker = dialogView.findViewById<DatePicker>(R.id.date_picker)
            val calendar: Calendar = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val displaytime = DateFormat.format(getString(R.string.format_time_day_month_year), calendar.time) as String
            val format: String
            if (typeInput == "START") {
                calendar[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !
                calendar.clear(Calendar.MINUTE)
                calendar.clear(Calendar.SECOND)
                calendar.clear(Calendar.MILLISECOND)
            } else if (typeInput == "FINISH") {
                calendar[Calendar.HOUR_OF_DAY] = 23
                calendar[Calendar.MINUTE] = 59
                calendar[Calendar.SECOND] = 59
                calendar[Calendar.MILLISECOND] = 999
            }
            @SuppressLint("SimpleDateFormat") val dateFormatGmt = SimpleDateFormat(getString(R.string.format_day_yearfirst))
            dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
            format = dateFormatGmt.format(calendar.time)
            editText.setText(displaytime)
            output.append(format)
            alertDialog.dismiss()
        }
        alertDialog.setView(dialogView)
        alertDialog.show()
    }

    private fun query(item: ThongKeAdapter.Item) {
        mHoanThanh = 0
        mDangSuaChua = mHoanThanh
        mChuaSuaChua = mDangSuaChua
        (findViewById<View>(R.id.txt_thongke_mota) as TextView).text = item.mota
        val txtThoiGian = findViewById<TextView>(R.id.txt_thongke_thoigian)
        if (item.thoigianhienthi == null) txtThoiGian.visibility = View.GONE else {
            txtThoiGian.text = item.thoigianhienthi
            txtThoiGian.visibility = View.VISIBLE
        }
        var whereClause = ""
        if (item.thoigianbatdau == null || item.thoigianketthuc == null) {
            whereClause += String.format("%s = '%s' or ", getString(R.string.Field_SuCo_MaQuan), getString(R.string.Quan5Code))
            whereClause += String.format("%s = '%s' or ", getString(R.string.Field_SuCo_MaQuan), getString(R.string.Quan6Code))
            whereClause += String.format("%s = '%s' or ", getString(R.string.Field_SuCo_MaQuan), getString(R.string.Quan8Code))
            whereClause += String.format("%s = '%s' or ", getString(R.string.Field_SuCo_MaQuan), getString(R.string.QuanBinhTanCode))
            whereClause += " 1 = 1"
        } else {
            whereClause = String.format("(%s >= date '%s' and %s <= date '%s') and (",
                    getString(R.string.Field_SuCo_NgayThongBao), item.thoigianbatdau,
                    getString(R.string.Field_SuCo_NgayThongBao), item.thoigianketthuc)
            whereClause += String.format("%s = '%s' or ", getString(R.string.Field_SuCo_MaQuan), getString(R.string.Quan5Code))
            whereClause += String.format("%s = '%s' or ", getString(R.string.Field_SuCo_MaQuan), getString(R.string.Quan6Code))
            whereClause += String.format("%s = '%s' or ", getString(R.string.Field_SuCo_MaQuan), getString(R.string.Quan8Code))
            whereClause += String.format("%s = '%s' or ", getString(R.string.Field_SuCo_MaQuan), getString(R.string.QuanBinhTanCode))
            whereClause += " 1 = 1)"
        }
        val queryParameters = QueryParameters()
        queryParameters.whereClause = whereClause


//        final ListenableFuture<FeatureQueryResult> feature =
//                mServiceFeatureTable.populateFromServiceAsync(queryParameters, true, outFields);
        val feature = mServiceFeatureTable!!.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        feature.addDoneListener {
            try {
                val result = feature.get()
                val iterator: Iterator<Feature> = result.iterator()
                var item1: Feature
                while (iterator.hasNext()) {
                    item1 = iterator.next()
                    //                    for (Object i : result) {
//                        Feature item = (Feature) i;
                    val value = item1.attributes[getString(R.string.trangthai)]
                    var trangThai = resources.getInteger(R.integer.trang_thai_chua_sua_chua)
                    if (value != null) {
                        trangThai = value.toString().toInt()
                    }
                    if (trangThai == resources.getInteger(R.integer.trang_thai_chua_sua_chua)) mChuaSuaChua++ else if (trangThai == resources.getInteger(R.integer.trang_thai_dang_sua_chua)) mDangSuaChua++ else if (trangThai == resources.getInteger(R.integer.trang_thai_hoan_thanh)) mHoanThanh++
                }
                displayReport()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayReport() {
        val tongloaitrangthai = mChuaSuaChua + mDangSuaChua + mHoanThanh
        mTxtTongSuCo!!.text = getString(R.string.nav_thong_ke_tong_su_co) + tongloaitrangthai
        mTxtChuaSua!!.text = mChuaSuaChua.toString() + ""
        mTxtDangSua!!.text = mDangSuaChua.toString() + ""
        mTxtHoanThanh!!.text = mHoanThanh.toString() + ""
        var percentChuaSua: Double
        var percentDangSua: Double
        var percentHoanThanh: Double
        percentHoanThanh = 0.0
        percentDangSua = percentHoanThanh
        percentChuaSua = percentDangSua
        if (tongloaitrangthai > 0) {
            percentChuaSua = mChuaSuaChua * 100 / tongloaitrangthai.toDouble()
            percentDangSua = mDangSuaChua * 100 / tongloaitrangthai.toDouble()
            percentHoanThanh = mHoanThanh * 100 / tongloaitrangthai.toDouble()
        }
        mTxtPhanTramChuaSua!!.text = "$percentChuaSua%"
        mTxtPhanTramDangSua!!.text = "$percentDangSua%"
        mTxtPhanTramHoanThanh!!.text = "$percentHoanThanh%"
        var mChart = findViewById<PieChart>(R.id.piechart)
        mChart = configureChart(mChart)
        mChart = setData(mChart)
        mChart.animateXY(1500, 1500)
    }

    fun configureChart(chart: PieChart): PieChart {
        chart.setHoleColor(resources.getColor(android.R.color.background_dark))
        chart.holeRadius = 60f
        chart.setDescription("")
        chart.transparentCircleRadius = 5f
        chart.setDrawCenterText(true)
        chart.isDrawHoleEnabled = false
        chart.rotationAngle = 0f
        chart.isRotationEnabled = true
        chart.setUsePercentValues(false)
        val legend = chart.legend
        legend.position = Legend.LegendPosition.LEFT_OF_CHART
        return chart
    }

    private fun setData(chart: PieChart): PieChart {
        val yVals1 = ArrayList<Entry>()
        yVals1.add(Entry(mChuaSuaChua.toFloat(), 0))
        yVals1.add(Entry(mDangSuaChua.toFloat(), 1))
        yVals1.add(Entry(mHoanThanh.toFloat(), 2))
        val xVals = ArrayList<String>()
        xVals.add(getString(R.string.SuCo_TrangThai_ChuaSuaChua))
        xVals.add(getString(R.string.SuCo_TrangThai_DangSuaChua))
        xVals.add(getString(R.string.SuCo_TrangThai_HoanThanh))
        val set1 = PieDataSet(yVals1, "")
        set1.sliceSpace = 0f
        val colors = ArrayList<Int>()
        colors.add(resources.getColor(android.R.color.holo_red_light))
        colors.add(resources.getColor(android.R.color.holo_orange_light))
        colors.add(resources.getColor(android.R.color.holo_green_light))
        set1.colors = colors
        val data = PieData(xVals, set1)
        data.setValueTextSize(15f)
        set1.valueTextSize = 0f
        chart.data = data
        chart.highlightValues(null)
        //        chart.invalidate();
        return chart
    }
}