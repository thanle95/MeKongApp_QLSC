package hcm.ditagis.com.vinhlong.qlsc.entities

/**
 * Created by NGUYEN HONG on 3/14/2018.
 */
class DLayerInfo(
        val layerId: String,
        val layerName: String,
        val isView: Boolean,
        val isCreate: Boolean,
        val isDelete: Boolean,
        val isEdit: Boolean,
        val definition: String,
        val url: String,
        val isVisible: String)