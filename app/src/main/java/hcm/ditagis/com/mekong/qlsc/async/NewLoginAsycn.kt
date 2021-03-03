package hcm.ditagis.com.mekong.qlsc.async

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hcm.ditagis.com.mekong.qlsc.R
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.entities.entitiesDB.User
import hcm.ditagis.com.mekong.qlsc.utities.Constant
import hcm.ditagis.com.mekong.qlsc.utities.Preference
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
            cred.put("username", userName)
            cred.put("password", pin)
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.useCaches = false
            val wr = OutputStreamWriter(conn.outputStream)
            wr.write(cred.toString())
            wr.flush()
            conn.connect()
            val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (true) {
                line = bufferedReader.readLine()
                if (line == null)
                    break
                stringBuilder.append(line)

            }

            bufferedReader.close()
            conn.disconnect()
            val user = parseUser(stringBuilder.toString())
            Preference.instance!!.savePreferences(mActivity.getString(R.string.preference_login_api),
                    user.accessToken!!.replace("\"", ""))

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

    private fun parseUser(data: String?): User {
        val userType = object : TypeToken<User>() {}.type
        val gson = Gson()
        val user: User = gson.fromJson(data, userType)

        return user
    }

    init {
        mDApplication = mActivity.application as DApplication
        mDelegate = delegate
        API_URL = mActivity.getString(R.string.URL_API) + "/Login"
    }
}