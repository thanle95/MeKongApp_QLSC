package hcm.ditagis.com.vinhlong.qlsc

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
import hcm.ditagis.com.vinhlong.qlsc.async.NewLoginAsycn
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.utities.CheckConnectInternet.isOnline
import hcm.ditagis.com.vinhlong.qlsc.utities.Preference.Companion.instance
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var mTxtUsername: TextView? = null
    private var mTxtPassword: TextView? = null
    private var isLastLogin = false
    private var mTxtValidation: TextView? = null
    private var mApplication: DApplication? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mApplication = application as DApplication
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener(this)
        findViewById<View>(R.id.txt_login_changeAccount).setOnClickListener(this)
        mTxtUsername = findViewById(R.id.txtUsername)
        mTxtPassword = findViewById(R.id.txtPassword)

//        mTxtUsername!!.setText("tiwamythoxulysuco");
//        mTxtPassword!!.setText("tiwamythoxulysuco");
        mTxtValidation = findViewById(R.id.txt_login_validation)
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
            mTxtValidation!!.setText(R.string.validate_no_connect)
            mTxtValidation!!.visibility = View.VISIBLE
            return
        }
        mTxtValidation!!.visibility = View.GONE
        val userName: String?
        userName = if (isLastLogin) instance!!.loadPreference(getString(R.string.preference_username)) else mTxtUsername!!.text.toString().trim { it <= ' ' }
        val passWord = mTxtPassword!!.text.toString().trim { it <= ' ' }
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
        mTxtValidation!!.setText(R.string.info_login_empty)
        mTxtValidation!!.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        mTxtValidation!!.setText(R.string.validate_login_fail)
        mTxtValidation!!.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess() {
        instance!!.savePreferences(getString(R.string.preference_username), mTxtUsername!!.text.toString())
        //        Preference.getInstance().savePreferences(getString(R.string.preference_password), khachHang.getPassWord());
        instance!!.savePreferences(getString(R.string.preference_displayname), mApplication!!.user!!.displayName)
        mTxtUsername!!.text = ""
        mTxtPassword!!.text = ""
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun changeAccount() {
        mTxtUsername!!.text = ""
        mTxtPassword!!.text = ""
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
                if (mTxtPassword!!.text.toString().trim { it <= ' ' }.length > 0) {
                    login()
                    return true
                }
                super.onKeyUp(keyCode, event)
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}