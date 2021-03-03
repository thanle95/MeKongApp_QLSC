package hcm.ditagis.com.mekong.qlsc

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hcm.ditagis.com.mekong.qlsc.async.NewLoginAsycn
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.utities.CheckConnectInternet.isOnline
import hcm.ditagis.com.mekong.qlsc.utities.Preference.Companion.instance
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var isLastLogin = false
    private var mApplication: DApplication? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mApplication = application as DApplication
        btnLogin.setOnClickListener(this)
        txt_login_changeAccount.setOnClickListener(this)

//        txtUsername!!.setText("tiwamythoxulysuco");
//        txtPassword!!.setText("tiwamythoxulysuco");
        txt_version.setText("v" + packageManager.getPackageInfo(packageName, 0).versionName)
        create()
    }

    private fun create() {
        instance!!.setContext(this)
        val preference_userName = instance!!.loadPreference(getString(R.string.preference_username))

        //nếu chưa từng đăng nhập thành công trước đó
        //nhập username và password bình thường
        isLastLogin = !(preference_userName == null || preference_userName.isEmpty())
        try {
            if (!mApplication!!.isCheckedVersion) {
                mApplication!!.isCheckedVersion = true
//                CheckVersionAsycn(this, CheckVersionAsycn.AsyncResponse { output: VersionInfo? ->
//                    if (output != null) {
//                        val builder = AlertDialog.Builder(this@LoginActivity, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
//                        builder.setCancelable(true)
//                                .setPositiveButton("CẬP NHẬT") { dialogInterface: DialogInterface?, i: Int -> goURLBrowser(output.link) }.setTitle("Có phiên bản mới")
//                        var isDeveloper = false
//                        if (output.type != "RELEASE") {
//                            val anInt = Settings.Secure.getInt(this.contentResolver,
//                                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
//                            if (anInt != 0) isDeveloper = true
//                        }
//                        if (isDeveloper) builder.setMessage("Bạn là người phát triển ứng dụng! Bạn có muốn cập nhật lên phiên bản " + output.versionCode + "?") else builder.setMessage("Bạn có muốn cập nhật lên phiên bản " + output.versionCode + "?")
//                        val dialog = builder.create()
//                        dialog.show()
//                    } else {
//                        Toast.makeText(this@LoginActivity, "Phiên bản hiện tại là mới nhất", Toast.LENGTH_LONG).show()
//                    }
//                }).execute(packageManager.getPackageInfo(packageName, 0).versionName)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(this, "Có lỗi xảy ra khi kiểm tra phiên bản", Toast.LENGTH_LONG).show()
        }
    }

    private fun goURLBrowser(url: String) {
        var url = url
        var result = false
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://$url"
        val webpage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        try {
            startActivity(intent)
            result = true
        } catch (ignored: Exception) {
        }
    }

    private fun login() {
        if (!isOnline(this)) {
            txt_login_validation!!.setText(R.string.validate_no_connect)
            txt_login_validation!!.visibility = View.VISIBLE
            return
        }
        txt_login_validation!!.visibility = View.GONE
        val userName: String?
        userName = if (isLastLogin) instance!!.loadPreference(getString(R.string.preference_username)) else txtUsername!!.text.toString().trim { it <= ' ' }
        val passWord = txtPassword!!.text.toString().trim { it <= ' ' }
        if (userName!!.length == 0 || passWord.length == 0) {
            handleInfoLoginEmpty()
            return
        }
        NewLoginAsycn(this,
                object: NewLoginAsycn.AsyncResponse {
                    override fun processFinish(output: Void?) {
                        if (mApplication!!.user != null) handleLoginSuccess() else handleLoginFail()
                    }
                }).execute(userName,passWord)
    }

    private fun handleInfoLoginEmpty() {
        txt_login_validation!!.setText(R.string.info_login_empty)
        txt_login_validation!!.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        txt_login_validation!!.setText(R.string.validate_login_fail)
        txt_login_validation!!.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess() {
        instance!!.savePreferences(getString(R.string.preference_username), txtUsername!!.text.toString())
        //        Preference.getInstance().savePreferences(getString(R.string.preference_password), khachHang.getPassWord());
        instance!!.savePreferences(getString(R.string.preference_displayname), mApplication!!.user!!.displayName)
        txtUsername!!.setText("")
        txtPassword!!.setText("")
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun changeAccount() {
        txtUsername!!.setText("")
        txtPassword!!.setText("")
        instance!!.savePreferences(getString(R.string.preference_username), "")
        create()
    }

    override fun onPostResume() {
        super.onPostResume()
        create()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> login()
            R.id.txt_login_changeAccount -> changeAccount()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (txtPassword!!.text.toString().trim { it <= ' ' }.length > 0) {
                    login()
                    return true
                }
                super.onKeyUp(keyCode, event)
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}