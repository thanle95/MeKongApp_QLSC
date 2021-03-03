package hcm.ditagis.com.mekong.qlsc.fragment.task

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.mekong.qlsc.R
import hcm.ditagis.com.mekong.qlsc.databinding.ItemTracuuBinding
import hcm.ditagis.com.mekong.qlsc.utities.Constant
import java.util.*

object HandlingSearchHasDone {
    @JvmStatic
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun handleFromItems(context: Context?, items: List<Feature>, serviceFeatureTable: ServiceFeatureTable): List<ItemTracuuBinding> {
        val views: MutableList<ItemTracuuBinding> = ArrayList()
        try {

//            Comparator<Feature> comparator = (Item o1, Item o2) -> {
//                try {
//                    long i = Constant.DateFormat.DATE_FORMAT_VIEW.parse(o2.getNgayThongBao()).getTime() -
//                            Constant.DateFormat.DATE_FORMAT_VIEW.parse(o1.getNgayThongBao()).getTime();
//                    if (i > 0)
//                        return 1;
//                    else if (i == 0)
//                        return 0;
//                    else return -1;
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                return 0;
//            };
//            List<Item> list = items;
//            list.sort(comparator);
            val thongTinPhanAnhDomain = serviceFeatureTable.getField(Constant.FieldSuCo.THONG_TIN_PHAN_ANH).domain as CodedValueDomain
            for (item in items) {
                val bindingLayout = ItemTracuuBinding.inflate(LayoutInflater.from(context))
                val txtThongTinPhanAnh = bindingLayout.txtInfo
                val txtDiaChi = bindingLayout.txtLocation
                val txtID = bindingLayout.txtId
                val txtNgayCapNhat = bindingLayout.txtTime
                val trangThaiObject = item.attributes[Constant.FieldSuCo.TRANG_THAI]
                var trangThai = Constant.TrangThaiSuCo.MOI_TIEP_NHAN
                if (trangThaiObject != null) trangThai = trangThaiObject as Short

                bindingLayout.txtObjectid.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                txtDiaChi.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                txtID.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                txtNgayCapNhat.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                txtThongTinPhanAnh.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                when (trangThai) {
                    Constant.TrangThaiSuCo.MOI_TIEP_NHAN -> {
                        bindingLayout.root.setBackgroundColor(ContextCompat.getColor(context!!, R.color.color_chua_sua_chua))
                    }
                    Constant.TrangThaiSuCo.DANG_SUA -> {
                        bindingLayout.root.setBackgroundColor(ContextCompat.getColor(context!!, R.color.color_dang_sua_chua))
                    }
                    Constant.TrangThaiSuCo.HOAN_THANH -> {
                        bindingLayout.root.setBackgroundColor(ContextCompat.getColor(context!!, R.color.color_da_sua_chua))
                    }
                    else -> {
                    }
                }
                val id = item.attributes[Constant.FieldSuCo.ID_SUCO]
                val diaChi = item.attributes[Constant.FieldSuCo.DIA_CHI]
                val ngayThongBao = item.attributes[Constant.FieldSuCo.TG_PHAN_ANH]
                val thongTinPhanAnh = getValueDomain(thongTinPhanAnhDomain.codedValues, item.attributes[Constant.FieldSuCo.THONG_TIN_PHAN_ANH])
                if (id == null) txtID.visibility = View.GONE else txtID.text = id as String?
                if (diaChi == null) txtDiaChi.visibility = View.GONE else txtDiaChi.text = diaChi as String?
                if (ngayThongBao == null) txtNgayCapNhat.visibility = View.GONE else txtNgayCapNhat.text = Constant.DateFormat.DATE_FORMAT_VIEW.format((ngayThongBao as Calendar).time)
                if (thongTinPhanAnh == null) txtThongTinPhanAnh.visibility = View.GONE else txtThongTinPhanAnh.text = thongTinPhanAnh as String?
                bindingLayout.txtObjectid.text = item.attributes[Constant.Field.OBJECTID].toString()
                views.add(bindingLayout)
            }
        } catch (e: Exception) {
            Log.e("Lỗi lấy ds công việc", e.toString())
        }
        return views
    }

    fun getValueDomain(codedValues: List<CodedValue>, code: Any?): Any? {
        var value: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.code == code) {
                value = codedValue.name
                break
            }
        }
        return value
    }
}