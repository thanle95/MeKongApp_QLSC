package hcm.ditagis.com.vinhlong.qlsc.async

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.entities.HoSoVatTuSuCo
import hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB.ListObjectDB
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException
@SuppressLint("StaticFieldLeak")
class HoSoVatTuSuCoAsync (
 private val mContext: Context, private val mDelegate: AsyncResponse) : AsyncTask<Any?, Any?, Void?>() {
    private val mServiceFeatureTable: ServiceFeatureTable? = null

    interface AsyncResponse {
        fun processFinish(`object`: Any?)
    }

    private fun find(idSuCo: String) {
        val queryParameters = QueryParameters()
        val list: MutableList<HoSoVatTuSuCo> = ArrayList()
        val queryClause = String.format("%s = '%s'", mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo), idSuCo)
        //        String queryClause = "1 = 1";
        queryParameters.whereClause = queryClause
        val queryResultListenableFuture = mServiceFeatureTable!!.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        queryResultListenableFuture.addDoneListener {
            try {
                val result = queryResultListenableFuture.get()
                val it: Iterator<*> = result.iterator()
                while (it.hasNext()) {
                    val feature = it.next() as Feature
                    val attributes = feature.attributes
                    val maVatTu = attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu)].toString()
                    var isFound = false

                    //Lấy tên vật tư và mã vật tư
                    for (vatTu in ListObjectDB.instance!!.vatTus) if (vatTu.maVatTu == maVatTu) {
                        list.add(HoSoVatTuSuCo(attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo)].toString(), attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_SoLuong)].toString().toDouble(),
                                attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu)].toString(),
                                vatTu.tenVatTu, vatTu.donViTinh))
                        isFound = true
                        break
                    }
                    if (!isFound) for (vatTu in ListObjectDB.instance!!.vatTus) if (vatTu.maVatTu == maVatTu) {
                        list.add(HoSoVatTuSuCo(attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo)].toString(), attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_SoLuong)].toString().toDouble(),
                                attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu)].toString(),
                                vatTu.tenVatTu, vatTu.donViTinh))
                        break
                    }
                }
                ListObjectDB.instance!!.hoSoVatTuSuCos = list
                publishProgress(list)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                publishProgress()
            } catch (e: ExecutionException) {
                e.printStackTrace()
                publishProgress()
            }
        }
    }

    private fun delete() {
        val queryParameters = QueryParameters()
        val queryClause = "1 = 1"
        queryParameters.whereClause = queryClause
        val queryResultListenableFuture = mServiceFeatureTable!!.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        queryResultListenableFuture.addDoneListener {
            try {
                val result = queryResultListenableFuture.get()
                mServiceFeatureTable.deleteFeaturesAsync(result).addDoneListener {
                    val listListenableFuture = mServiceFeatureTable.applyEditsAsync()
                    listListenableFuture.addDoneListener {
                        try {
                            if (listListenableFuture.get().size > 0) {
                                //xóa thành công
                                insert(ListObjectDB.instance!!.getLstHoSoVatTuSuCoInsert())
                            } else {
                                publishProgress(false)
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                            publishProgress(false)
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                            publishProgress(false)
                        }
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                publishProgress(false)
            } catch (e: ExecutionException) {
                e.printStackTrace()
                publishProgress(false)
            }
        }
    }

    private fun insert(hoSoVatTuSuCos: List<HoSoVatTuSuCo>) {
        val features: MutableList<Feature> = ArrayList()
        for (hoSoVatTuSuCo in hoSoVatTuSuCos) {
            val attributes: MutableMap<String, Any> = HashMap()
            attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo)] = hoSoVatTuSuCo.idSuCo
            attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu)] = hoSoVatTuSuCo.maVatTu
            attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_SoLuong)] = hoSoVatTuSuCo.soLuong
            val feature = mServiceFeatureTable!!.createFeature()
            feature.attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo)] = hoSoVatTuSuCo.idSuCo
            feature.attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu)] = hoSoVatTuSuCo.maVatTu
            feature.attributes[mContext.getString(R.string.Field_HoSoVatTuSuCo_SoLuong)] = hoSoVatTuSuCo.soLuong
            features.add(feature)
        }
        mServiceFeatureTable!!.addFeaturesAsync(features).addDoneListener {
            val listListenableFuture = mServiceFeatureTable.applyEditsAsync()
            listListenableFuture.addDoneListener {
                try {
                    val featureEditResults = listListenableFuture.get()
                    if (featureEditResults.size > 0) {
                        ListObjectDB.instance!!.clearListHoSoVatTuSuCoChange()
                        publishProgress(true)
                    } else {
                        publishProgress(false)
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    publishProgress(false)
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                    publishProgress(false)
                }
            }
        }
    }

    override fun doInBackground(vararg objects: Any?): Void? {
        mServiceFeatureTable!!.loadAsync()
        mServiceFeatureTable.addLoadStatusChangedListener { loadStatusChangedEvent: LoadStatusChangedEvent ->
            if (loadStatusChangedEvent.newLoadStatus == LoadStatus.LOADED) {
                if (objects.isNotEmpty()) {
                    when (objects[0].toString().toInt()) {
                        Constant.HOSOVATTUSUCO_METHOD.FIND -> if (objects.size > 1 && objects[1] is String) {
                            find(objects[1].toString())
                        }
                        Constant.HOSOVATTUSUCO_METHOD.INSERT -> delete()
                    }
                }
            } else {
                publishProgress()
                Log.e("Load table", "không loaded")
            }
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Any?) {
        super.onProgressUpdate(*values)
        mDelegate.processFinish(values)
    }

}