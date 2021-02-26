package hcm.ditagis.com.vinhlong.qlsc.async

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.entities.entitiesDB.User
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import hcm.ditagis.com.vinhlong.qlsc.utities.Preference
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NewLoginAsycn(private val mActivity: Activity, delegate: AsyncResponse) : AsyncTask<String?, Void?, Void?>() {
    private val exception: Exception? = null
    private var mDialog: ProgressDialog? = null
    private val mDelegate: AsyncResponse
    private val mDApplication: DApplication
    var API_URL: String

    interface AsyncResponse {
        fun processFinish(output: Void?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog = ProgressDialog(mActivity, android.R.style.Theme_Material_Dialog_Alert)
        mDialog!!.setMessage(mActivity.getString(R.string.connect_message))
        mDialog!!.setCancelable(false)
        mDialog!!.show()
    }

    override fun doInBackground(vararg params: String?): Void? {
        val userName = params[0]
        val pin = params[1]
        var conn: HttpURLConnection? = null
        try {
            val API_URL = Constant.URL_API.LOGIN
            val url = URL(API_URL)
            conn = url.openConnection() as HttpURLConnection
            conn!!.doOutput = true
            conn.instanceFollowRedirects = false
            conn.requestMethod = Constant.HTTPRequest.POST_METHOD
            val cred = JSONObject()
            cred.put("Username", userName)
            cred.put("Password", pin)
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.useCaches = false
            val wr = OutputStreamWriter(conn.outputStream)
            wr.write(cred.toString())
            wr.flush()
            conn.connect()
            val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
            val builder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                builder.append(line)
            }
            Preference.instance!!.savePreferences(mActivity.getString(R.string.preference_login_api), builder.toString().replace("\"", ""))
            val user = User()
            user.displayName = displayName
            user.userName = userName
            user.token = builder.toString().replace("\"", "")
            mDApplication.user = user
        } catch (e: Exception) {
            Log.e("Lỗi đăng nhập", e.toString())
        } finally {
            conn?.disconnect()
        }
        return null
    }

    override fun onPostExecute(user: Void?) {
//        if (user != null) {
        mDialog!!.dismiss()
        mDelegate.processFinish(user)
        //        }
    }

    private val displayName: String
        private get() {
            val API_URL = mActivity.getString(R.string.URL_API) + "/Account/Profile"
            var displayName = ""
            try {
                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                try {
                    conn.doOutput = false
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Authorization", Preference.instance!!.loadPreference(mActivity.getString(R.string.preference_login_api)))
                    conn.connect()
                    val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        displayName = pajsonRouteeJSon(line)
                        break
                    }
                } catch (e: Exception) {
                    Log.e("error", e.toString())
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                return displayName
            }
        }

    private fun pajsonRouteeJSon(data: String?): String {
        if (data == null) return ""
        var displayName = ""
        val myData = "{ \"account\": [$data]}"
        val jsonData = JSONObject(myData)
        val jsonRoutes = jsonData.getJSONArray("account")
        //        jsonData.getJSONArray("account");
        for (i in 0 until jsonRoutes.length()) {
            val jsonRoute = jsonRoutes.getJSONObject(i)
            displayName = jsonRoute.getString(mActivity.getString(R.string.sql_coloumn_login_displayname))
        }
        return displayName
    }

    init {
        mDApplication = mActivity.application as DApplication
        mDelegate = delegate
        API_URL = mActivity.getString(R.string.URL_API) + "/Login"
    }
}