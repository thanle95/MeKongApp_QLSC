package hcm.ditagis.com.vinhlong.qlsc.services

import android.content.Context
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB.ListObjectDB.Companion.instance
import java.util.*
import java.util.concurrent.ExecutionException

class GetDMA(private val mContext: Context) {
    //
    val maDMAFromService: Unit
        get() {
            val layerInfoVatTu = mContext.getString(R.string.LayerInfo_DMA)
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
                        val dmaList: MutableList<String> = ArrayList()
                        try {
                            val result = feature.get()
                            val iterator: Iterator<Feature> = result.iterator()
                            var item: Feature
                            while (iterator.hasNext()) {
                                item = iterator.next()
                                val dma = item.attributes[mContext.getString(R.string.field_DMA_maDMA)]
                                if (dma != null) dmaList.add(dma.toString())
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        } finally {
                            instance!!.dmas = dmaList
                        }
                    }
                    break
                }
            }
        }

}