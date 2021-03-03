package hcm.ditagis.com.mekong.qlsc

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import hcm.ditagis.com.mekong.qlsc.databinding.ActivityShowCaptureBinding
import hcm.ditagis.com.mekong.qlsc.entities.DApplication

class ShowCaptureActivity : AppCompatActivity() {
    private var mApplication: DApplication? = null
    private lateinit var mBinding: ActivityShowCaptureBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        mBinding = ActivityShowCaptureBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mApplication = application as DApplication
        mBinding.showCaptureImageView.setImageBitmap(mApplication!!.bitmaps!!.first())
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.show_capture_cancel -> goHomeCancel()
            R.id.show_capture_ok -> goHome()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        goHomeCancel()
    }


    private fun goHome() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun goHomeCancel() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }
}
