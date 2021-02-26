package hcm.ditagis.com.vinhlong.qlsc.services

import android.content.Context
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.entities.VatTu
import hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB.ListObjectDB.Companion.instance
import java.util.*
import java.util.concurrent.ExecutionException

class GetVatTu(private val mContext: Context) {
    //
    val vatTuFromService: Unit
        get() {
            val layerInfoVatTu = mContext.getString(R.string.LayerInfo_vatTu)
            for (layerInfoDTG in instance!!.lstFeatureLayerDTG) {
                if (layerInfoDTG.id == layerInfoVatTu) {
                    val queryParameters = QueryParameters()
                    queryParameters.whereClause = "1=1"
                    var url = layerInfoDTG.url
                    if (!url.startsWith("http")) url = "http:" + layerInfoDTG.url
                    val serviceFeatureTable = ServiceFeatureTable(url)
                    //
                    val feature = serviceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                    feature.addDoneListener {
                        val vatTuList: MutableList<VatTu> = ArrayList()
                        try {
                            val result = feature.get()
                            val iterator: Iterator<Feature> = result.iterator()
                            var item: Feature
                            while (iterator.hasNext()) {
                                item = iterator.next()
                                val maVatTu = item.attributes[mContext.getString(R.string.field_VatTu_maVatTu)] as String?
                                val tenVatTu = item.attributes[mContext.getString(R.string.field_VatTu_tenVatTu)] as String?
                                val donViTinh = item.attributes[mContext.getString(R.string.field_VatTu_donViTinh)] as String?
                                val vatTu = VatTu(maVatTu!!, tenVatTu!!, donViTinh!!)
                                vatTuList.add(vatTu)
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        } finally {
                            instance!!.vatTus = vatTuList
                        }
                    }
                    break
                }
            }
        }

}