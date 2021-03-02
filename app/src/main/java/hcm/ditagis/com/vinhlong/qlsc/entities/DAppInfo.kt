package hcm.ditagis.com.vinhlong.qlsc.entities

/**
 * Created by NGUYEN HONG on 3/14/2018.
 */
class DAppInfo(
        val config: DConfig)
class DConfig(
        val hanhChinhID: Int,
        val IDHanhChinh: String,
        val TenHanhChinh: String,
        val MaHuyen: String,
        val TenHuyen: String
)