package hcm.ditagis.com.mekong.qlsc.utities

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.esri.arcgisruntime.geometry.SpatialReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ThanLe on 3/1/2018.
 */
class Constant {
    object DateFormat {
        const val DATE_FORMAT_STRING = "dd/MM/yyyy"
        @JvmField
        val DATE_FORMAT_YEAR_FIRST = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        @JvmField
        val DATE_FORMAT = SimpleDateFormat(DATE_FORMAT_STRING)
        val DATE_FORMAT_VIEW = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")
    }
    object Message {
        const val UNDEFINED = "Lỗi chưa xác định"
    }
    object URLImage {
        const val IMAGES_FOLDER = "$SERVER/images/map/suco"
        const val MAC_DINH = "$IMAGES_FOLDER/marker.png"
        const val BAT_THUONG = "$IMAGES_FOLDER/-1.png"
        const val MOI_TIEP_NHAN = "$IMAGES_FOLDER/0.png"
        const val DANG_XU_LY = "$IMAGES_FOLDER/dxl.png"
        const val HOAN_THANH = "$IMAGES_FOLDER/3.png"
    }

    object FileType {
        const val VIDEO = "video/quicktime"
        const val PNG = "image/png"
        const val JPEG = "image/jpeg"
        const val PDF = "application/pdf"
        const val DOC = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }

    object AttachmentName {
        const val ADD = "img_%s_%d.png"
        const val UPDATE = "img_%s_%d.png"
    }

    object HTTPRequest {
        const val GET_METHOD = "GET"
        const val POST_METHOD = "POST"
        const val AUTHORIZATION = "Authorization"
    }

    object RequestCode {
        const val LOGIN = 0
        const val CAMERA = 1
        const val SHOW_CAPTURE = 2
        const val PERMISSION = 3
        const val SEARCH = 4
        const val BASEMAP = 5
        const val RLAYER = 6
        const val ADD = 7
        const val ADD_FEATURE_ATTACHMENT = 8
        const val LIST_TASK = 9
        const val UPDATE = 10
        const val UPDATE_ATTACHMENT = 11
        const val PICK_PHOTO = 12
        const val NOTIFICATION = 100
        const val REQUEST_ID_UPDATE_ATTACHMENT = 50
    }

    object Field {
        const val OBJECTID = "OBJECTID"
        const val CREATED_USER = "created_user"
        const val CREATED_DATE = "created_date"
        const val LAST_EDITED_USER = "last_edited_user"
        const val LAST_EDITED_DATE = "last_edited_date"
        val NONE_UPDATE_FIELDS = arrayOf(CREATED_DATE, CREATED_USER, LAST_EDITED_DATE, LAST_EDITED_USER, OBJECTID,
        FieldSuCo.TG_PHAN_ANH)
    }

    object OptionAddImage {
        const val CAPTURE = "Chụp ảnh"
        const val PICK = "Chọn ảnh"
    }
    object FieldSuCo {
        const val ID_SUCO = "IDSuCo"
        const val NGUOI_PHAN_ANH = "NguoiPhanAnh"
        const val SDT_PHAN_ANH = "SDTPhanAnh"
        const val TG_PHAN_ANH = "TGPhanAnh"
        const val DOI_QUAN_LY = "DoiQuanLy"
        const val HINH_THUC_PHAT_HIEN = "HinhThucPhatHien"
        const val TRANG_THAI = "TrangThai"
        const val THONG_TIN_PHAN_ANH = "ThongTinPhanAnh"
        const val TG_KHAC_PHUC = "TGKhacPhuc"
        const val NHOM_KHAC_PHUC = "NhomKhacPhuc"
        const val PHAN_LOAI_SU_CO = "PhanLoaiSuCo"
        const val DIA_CHI = "DiaChi"
        const val MA_DUONG = "MaDuong"
        const val MA_QUAN = "MaQuan"
        const val MA_PHUONG = "MaPhuong"
        const val MA_DMA = "MaDMA"
        const val LOAI_SU_CO = "LoaiSuCo"
        const val VAT_LIEU = "VatLieu"
        const val NGUYEN_NHAN = "NguyenNhan"
        const val DUONG_KINH_ONG = "DuongKinhOng"
        const val AP_LUC = "ApLuc"
        const val DO_SAU_LUNG_ONG = "DoSauLungOng"
        const val GHI_CHU = "GhiChu"
        const val NV_XU_LY = "NVXuLy"
    }
    object CompressFormat {
        val JPEG = Bitmap.CompressFormat.JPEG
        val PNG = Bitmap.CompressFormat.PNG

        val TYPE_UPDATE = "png"
        val TYPE_COMPRESS = PNG

    }
    object TrangThaiSuCo {
        const val MOI_TIEP_NHAN = 0.toShort()
        const val DANG_SUA = 2.toShort()
        const val HOAN_THANH = 3.toShort()
    }

    object TinhTrangChuyenChinhThuc {
        const val CHUA_CHUYEN = 0.toShort()
        const val DA_CHUYEN = 1.toShort()
    }

    object LocationField {
        const val LONGTITUDE = "longtitude"
        const val LATITUDE = "latitude"
        const val LOCATION = "location"
    }

    object ThongTinPhanAnh {
        const val KHAC = 0.toShort()
        const val KHONG_NUOC = 1.toShort()
        const val NUOC_DUC = 2.toShort()
        const val NUOC_YEU = 3.toShort()
        const val XI_DHN = 4.toShort()
        const val HU_VAN = 5.toShort()
        const val ONG_BE = 6.toShort()
    }

    object URL_API {
        const val LOGIN = "$SERVER_API/Auth/Login"
        const val LAYER_INFO = "$SERVER_API/auth/layerinfos"
        const val CAPABILITIES = "$SERVER_API/auth/capabilities"
        const val APP_INFO = "$SERVER_API/auth/appinfo/"
    }


    object HOSOVATTUSUCO_METHOD {
        const val FIND = 0
        const val INSERT = 2
    }

    object ACCOUNT_ROLE {
        const val QLCN1 = "qlcn1"
        const val QLCN2 = "qlcn2"
    }
    object LAYER_ID{
        const val SU_CO = "DiemSuCo"
        const val BASEMAP = "BASEMAP"
    }
    companion object {
        const val EMPTY = ""
        @SuppressLint("SimpleDateFormat")
        val DATE_FORMAT = SimpleDateFormat("dd_MM_yyyy")
        @SuppressLint("SimpleDateFormat")
        val DATE_FORMAT_VIEW = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")

        const val SERVER = "http://171.244.32.245:100"
        const val SERVER_API = "http://171.244.32.245:100"
//        const val SERVER_API = "http://vwaco.vn:9092/api"
        const val NULL = ""
        val OPTION_IMAGE_LIST: List<String> = object : ArrayList<String>() {
            init {
                add(OptionAddImage.CAPTURE)
                add(OptionAddImage.PICK)
            }
        }
        const val QUERY_BY_OBJECTID = Field.OBJECTID + " = %d"
        const val QUERY_BY_SUCOID = FieldSuCo.ID_SUCO + " = '%s'"
        val SPATIAL_REFERENCE_VN2000 = SpatialReference.create("PROJCS[\"TPHCM_VN2000\",GEOGCS[\"GCS_VN_2000\",DATUM[\"D_Vietnam_2000\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",105.75],PARAMETER[\"Scale_Factor\",0.9999],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]")
        @JvmField
        val REQUEST_PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        const val MAX_SCALE_IMAGE_WITH_LABLES = 18000.0
        const val DEFINITION_HIDE_COMPLETE = " TrangThai <> 3"
        private var mInstance: Constant? = null
        @JvmStatic
        val instance: Constant?
            get() {
                if (mInstance == null) mInstance = Constant()
                return mInstance
            }
    }


}