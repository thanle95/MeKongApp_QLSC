package hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB

import hcm.ditagis.com.vinhlong.qlsc.entities.HoSoVatTuSuCo
import hcm.ditagis.com.vinhlong.qlsc.entities.VatTu
import java.util.*

class ListObjectDB private constructor() {
    var vatTus: List<VatTu>
    var dmas: List<String>
    var lstFeatureLayerDTG: List<LayerInfoDTG>
    private var lstHoSoVatTuSuCoInsert: MutableList<HoSoVatTuSuCo>
    var hoSoVatTuSuCos: List<HoSoVatTuSuCo>

    fun getLstHoSoVatTuSuCoInsert(): List<HoSoVatTuSuCo> {
        return lstHoSoVatTuSuCoInsert
    }

    fun setLstHoSoVatTuSuCoInsert(lstHoSoVatTuSuCoInsert: MutableList<HoSoVatTuSuCo>) {
        this.lstHoSoVatTuSuCoInsert = lstHoSoVatTuSuCoInsert
    }

    fun clearListHoSoVatTuSuCoChange() {
        lstHoSoVatTuSuCoInsert.clear()
    }

    companion object {
        @JvmStatic
        var instance: ListObjectDB? = null
            get() {
                if (field == null) field = ListObjectDB()
                return field
            }
            private set
    }

    init {
        lstHoSoVatTuSuCoInsert = ArrayList()
        hoSoVatTuSuCos = ArrayList()
        vatTus = ArrayList()
        dmas = ArrayList()
        lstFeatureLayerDTG = ArrayList()
    }
}