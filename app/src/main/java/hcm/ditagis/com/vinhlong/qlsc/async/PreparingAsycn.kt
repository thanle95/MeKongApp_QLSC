package hcm.ditagis.com.vinhlong.qlsc.async

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.entities.DLayerInfo
import hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB.User
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.zip.GZIPInputStream

class PreparingAsycn(@field:SuppressLint("StaticFieldLeak") private val mContext: Context, private val mDApplication: DApplication, private val mDelegate: AsyncResponse) : AsyncTask<Void?, ListenableFuture<FeatureQueryResult?>?, List<DLayerInfo>?>() {
    private var mDialog: ProgressDialog? = null

    interface AsyncResponse {
        fun processFinish(output: List<DLayerInfo>?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog = ProgressDialog(mContext, android.R.style.Theme_Material_Dialog_Alert)
        mDialog!!.setMessage(mContext.getString(R.string.preparing))
        mDialog!!.setCancelable(false)
        mDialog!!.show()
    }

     override fun doInBackground(vararg params: Void?): List<DLayerInfo>? {
        try {
            return layerInfoAPI
        } catch (e: Exception) {
            Log.e("Lỗi lấy danh sách DMA", e.toString())
        }
        return null
    }

    @SafeVarargs
    override fun onProgressUpdate(vararg values: ListenableFuture<FeatureQueryResult?>?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(value: List<DLayerInfo>?) {
//        if (khachHang != null) {
        mDialog!!.dismiss()
        mDelegate.processFinish(value)
        //        }
    }

    private val layerInfoAPI: List<DLayerInfo>?
        private get() {
            try {
                val API_URL = Constant.URL_API.LAYER_INFO
                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                try {
                    conn.doOutput = false
                    conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                    conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, "Bearer " + mDApplication.user!!.accessToken)
                    conn.connect()

                    val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                    val builder = StringBuilder()
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        builder.append(line)
                    }
                    return pajsonRouteeJSon(builder.toString())
                } catch (e: Exception) {
                    Log.e("error", e.toString())
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                Log.e("Lỗi lấy LayerInfo", e.toString())
            }
            return null
        }

    @Throws(JSONException::class)
    private fun pajsonRouteeJSon(data: String?): List<DLayerInfo>? {
        val outputType = object : TypeToken<List<DLayerInfo>>() {}.type
        val gson = Gson()
        val list: List<DLayerInfo> = gson.fromJson(data, outputType)
        return list
    }

}