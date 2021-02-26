package hcm.ditagis.com.vinhlong.qlsc.entities

import com.esri.arcgisruntime.geometry.Point
import java.util.*

class DiemSuCo {
    var objectID: Long = 0
    var idSuCo: String? = null
    var vitri: String? = null
    var ngayPhanAnh: Date? = null
    var nguoiPhanAnh: String? = null
    var sdtPhanAnh: String? = null
    var emailPhanAnh: String? = null
    var quan: String? = null
    var phuong: String? = null
    var ghiChu: String? = null
    var nguoiCapNhat: String? = null
    var ngayCapNhat: Date? = null
    var nguyenNhan: String? = null
    var point: Point? = null
    var image: ByteArray?= null
    var phuiDaoDai: Double? = null
    var phuiDaoRong: Double? = null
    var phuiDaoSau: Double? = null

    fun clear() {
        objectID = -1
        idSuCo = null
        vitri = null
        ngayPhanAnh = null
        nguoiPhanAnh = null
        sdtPhanAnh = null
        emailPhanAnh = null
        quan = null
        phuong = null
        ghiChu = null
        nguoiCapNhat = null
        ngayCapNhat = null
        nguyenNhan = null
        point = null
        image = null
        phuiDaoDai = null
        phuiDaoRong = null
        phuiDaoSau = null
    }

}