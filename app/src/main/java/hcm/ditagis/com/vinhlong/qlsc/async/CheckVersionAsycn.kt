package hcm.ditagis.com.vinhlong.qlsc.async

import android.R
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import hcm.ditagis.com.vinhlong.qlsc.entities.VersionInfo
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat

class CheckVersionAsycn( private val mContext: Context, private val mDelegate: AsyncResponse) : AsyncTask<String?, Void?, VersionInfo?>() {
    private var mDialog: ProgressDialog? = null

    interface AsyncResponse {
        fun processFinish(versionInfo: VersionInfo?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog = ProgressDialog(mContext, R.style.Theme_Material_Dialog_Alert)
        mDialog!!.setMessage("Đang kiểm tra phiên bản...")
        mDialog!!.setCancelable(false)
        mDialog!!.show()
    }

     override fun doInBackground(vararg params: String?): VersionInfo? {
        var versionInfo: VersionInfo? = null
        if (params.isNotEmpty()) try {
            val url = URL(String.format(Constant.URL_API.CHECK_VERSION, params[0]))
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.connect()
                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                    break
                }
                bufferedReader.close()
                versionInfo = parse(stringBuilder.toString())
            } catch (e1: Exception) {
                Log.e("Lỗi check version", e1.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message, e)
        } finally {
            return versionInfo
        }
        return versionInfo
    }

    @Throws(JSONException::class)
    private fun parse(data: String?): VersionInfo? {
        if (data == null) return null
        var versionInfo: VersionInfo? = null
        val myData = "{ \"version\": [$data]}"
        val jsonData = JSONObject(myData)
        val jsonRoutes = jsonData.getJSONArray("version")
        for (i in 0 until jsonRoutes.length()) {
            val jsonRoute = jsonRoutes.getJSONObject(i)
            val versionCode = jsonRoute.getString("VersionCode")
            val type = jsonRoute.getString("Type")
            val link = jsonRoute.getString("Link")
            val date = jsonRoute.getString("Date")
            try {
                versionInfo = VersionInfo(versionCode, type, link, SimpleDateFormat("yyyy-MM-dd").parse(date))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return versionInfo
    }

    override fun onPostExecute(versionInfo: VersionInfo?) {
//        if (user != null) {
        mDialog!!.dismiss()
        mDelegate.processFinish(versionInfo)
        //        }
    }

}