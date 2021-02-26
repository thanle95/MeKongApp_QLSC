package hcm.ditagis.com.vinhlong.qlsc.entities

/**
 * Created by NGUYEN HONG on 3/14/2018.
 */
class DLayerInfo(val id: String, val titleLayer: String, val url: String, val isCreate: Boolean, val isDelete: Boolean, val isEdit: Boolean, val isView: Boolean, val definition: String, val outFieldsArr: Array<String>, val updateFieldsArr: Array<String>)