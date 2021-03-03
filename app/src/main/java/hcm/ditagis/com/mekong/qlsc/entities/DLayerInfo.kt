package hcm.ditagis.com.mekong.qlsc.entities


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