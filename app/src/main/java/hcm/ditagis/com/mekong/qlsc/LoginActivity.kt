package hcm.ditagis.com.mekong.qlsc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hcm.ditagis.com.mekong.qlsc.async.NewLoginAsycn
import hcm.ditagis.com.mekong.qlsc.databinding.ActivityLoginBinding
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.utities.CheckConnectInternet.isOnline
import hcm.ditagis.com.mekong.qlsc.utities.Preference.Companion.instance

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var isLastLogin = false
    private var mApplication: DApplication? = null
    private lateinit var mBinding: ActivityLoginBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mApplication = application as DApplication
        mBinding.btnLogin.setOnClickListener(this)
        mBinding.txtLoginChangeAccount.setOnClickListener(this)

        mBinding.txtUsername.setText("tiwamythoxulysuco")
        mBinding.txtPassword.setText("tiwamythoxulysuco")
        mBinding.txtVersion.text = "v" + packageManager.getPackageInfo(packageName, 0).versionName
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
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(this, "Có lỗi xảy ra khi kiểm tra phiên bản", Toast.LENGTH_LONG).show()
        }
    }

    private fun goURLBrowser(input: String) {
        var url = input
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
            mBinding.txtLoginValidation.visibility = View.VISIBLE
            return
        }
        mBinding.txtLoginValidation.visibility = View.GONE
        val userName: String? = if (isLastLogin) instance!!.loadPreference(getString(R.string.preference_username)) else mBinding.txtUsername.text.toString().trim { it <= ' ' }
        val passWord = mBinding.txtPassword.text.toString().trim { it <= ' ' }
        if (userName!!.isEmpty() || passWord.isEmpty()) {
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
        mBinding.txtLoginValidation.setText(R.string.info_login_empty)
        mBinding.txtLoginValidation.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        mBinding.txtLoginValidation.setText(R.string.validate_login_fail)
        mBinding.txtLoginValidation.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess() {
        instance!!.savePreferences(getString(R.string.preference_username), mBinding.txtUsername.text.toString())
        //        Preference.getInstance().savePreferences(getString(R.string.preference_password), khachHang.getPassWord());
        instance!!.savePreferences(getString(R.string.preference_displayname), mApplication!!.user!!.displayName)
        mBinding.txtUsername.setText("")
        mBinding.txtPassword.setText("")
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun changeAccount() {
        mBinding.txtUsername.setText("")
        mBinding.txtPassword.setText("")
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
                if (mBinding.txtPassword.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                    login()
                    return true
                }
                super.onKeyUp(keyCode, event)
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}