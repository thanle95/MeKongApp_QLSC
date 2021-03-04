package hcm.ditagis.com.mekong.qlsc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import hcm.ditagis.com.mekong.qlsc.async.LoginTask
import hcm.ditagis.com.mekong.qlsc.databinding.ActivityLoginBinding
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.entities.entitiesDB.User
import hcm.ditagis.com.mekong.qlsc.utities.CheckConnectInternet.isOnline
import hcm.ditagis.com.mekong.qlsc.utities.Constant
import hcm.ditagis.com.mekong.qlsc.utities.DPreference
import hcm.ditagis.com.mekong.qlsc.utities.DPreference.Companion.preference


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

//        mBinding.txtUsername.setText("tiwacaibexulysuco")
//        mBinding.txtPassword.setText("tiwacaibexulysuco")
        mBinding.txtVersion.text = "v" + packageManager.getPackageInfo(packageName, 0).versionName
        create()
    }

    private fun create() {
        preference!!.setContext(this)
        val username = preference!!.loadPreference(Constant.PreferenceKey.USERNAME)
        val password = preference!!.loadPreference(Constant.PreferenceKey.PASSWORD)
        if (username != null && password != null) {
            mBinding.txtUsername.setText(username)
            mBinding.txtPassword.setText(password)
        }

    }



    private fun login() {
        if (!isOnline(this)) {
            mBinding.txtLoginValidation.visibility = View.VISIBLE
            return
        }
        mBinding.txtLoginValidation.visibility = View.GONE
        val username = mBinding.txtUsername.text.toString()
        val password = mBinding.txtPassword.text.toString()
        if (username!!.isEmpty() || password.isEmpty()) {
            handleInfoLoginEmpty()
            return
        }
        LoginTask(object : LoginTask.Response {
            override fun post(user: User?) {
                if (user != null) {
                    mApplication!!.user = user
                    handleLoginSuccess(username, password)
                } else handleLoginFail()
            }
        }).execute(this@LoginActivity, username, password)
    }

    private fun handleInfoLoginEmpty() {
        mBinding.txtLoginValidation.setText(R.string.info_login_empty)
        mBinding.txtLoginValidation.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        mBinding.txtLoginValidation.setText(R.string.validate_login_fail)
        mBinding.txtLoginValidation.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess(username: String, password: String) {
        preference!!.savePreferences(Constant.PreferenceKey.USERNAME, username)
        preference!!.savePreferences(Constant.PreferenceKey.PASSWORD, password)
        mBinding.txtUsername.setText("")
        mBinding.txtPassword.setText("")
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun changeAccount() {
        mBinding.txtUsername.setText("")
        mBinding.txtPassword.setText("")
        preference!!.deletePreferences()
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