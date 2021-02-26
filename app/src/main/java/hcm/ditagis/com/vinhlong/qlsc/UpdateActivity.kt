package hcm.ditagis.com.vinhlong.qlsc

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.esri.arcgisruntime.data.ArcGISFeature
import hcm.ditagis.com.vinhlong.qlsc.async.EditAsync
import hcm.ditagis.com.vinhlong.qlsc.async.LoadingDataFeatureAsync
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import java.util.*

class UpdateActivity : AppCompatActivity() {
    private var mApplication: DApplication? = null
    var mBtnUpdate: Button? = null
    var mBtnComplete: Button? = null
    var mLLayoutMain: LinearLayout? = null
    var mLLayoutField: LinearLayout? = null
    var mLLayoutProgress: LinearLayout? = null
    var mTxtProgress: TextView? = null
    private var mmSwipe: SwipeRefreshLayout? = null
    private val mActivity: UpdateActivity? = null
    private var mArcGISFeature: ArcGISFeature? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_update_feature)
        mApplication = application as DApplication
        Objects.requireNonNull(supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        Objects.requireNonNull(supportActionBar)?.setDisplayShowHomeEnabled(true)
        initViews()
    }

    private fun initViews() {
        mBtnComplete = findViewById(R.id.btn_update_feature_complete)
        mBtnUpdate = findViewById(R.id.btn_update_feature_update)
        mLLayoutMain = findViewById(R.id.llayout_update_feature_main)
        mLLayoutField = findViewById(R.id.llayout_update_feature_field)
        mLLayoutProgress = findViewById(R.id.llayout_update_feature_progress)
        mTxtProgress = findViewById(R.id.txt_update_feature_progress)
        mBtnComplete!!.setOnClickListener { view: View -> onClick(view) }
        mBtnUpdate!!.setOnClickListener { view: View -> onClick(view) }
        mmSwipe = findViewById(R.id.swipe_udpate_feature)
        if (mApplication!!.images != null)
            mApplication!!.images = arrayListOf()
        mTxtProgress!!.text = "Đang khởi tạo thuộc tính..."
        mLLayoutProgress!!.visibility = View.VISIBLE
        mLLayoutMain!!.visibility = View.GONE
        mArcGISFeature = mApplication!!.selectedArcGISFeature
        mmSwipe!!.setOnRefreshListener {
            loadData()
            mmSwipe!!.isRefreshing = false
        }
        loadData()
    }

    private fun loadData() {
        mLLayoutField!!.removeAllViews()
        mLLayoutProgress!!.visibility = View.VISIBLE
        mLLayoutMain!!.visibility = View.GONE
        LoadingDataFeatureAsync(this@UpdateActivity, mArcGISFeature!!.featureTable.fields,
                object: LoadingDataFeatureAsync.AsyncResponse {
                    override fun processFinish(views: List<View?>?) {
                        if (views != null) for (view1 in views) {
                            mLLayoutField!!.addView(view1)
                        }
                        mLLayoutProgress!!.visibility = View.GONE
                        mLLayoutMain!!.visibility = View.VISIBLE
                    }
                }, mArcGISFeature).execute(false)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_update_feature_complete -> {
                val layout = layoutInflater.inflate(R.layout.layout_dialog, null) as LinearLayout
                val txtTitle = layout.findViewById<TextView>(R.id.txt_dialog_title)
                val txtMessage = layout.findViewById<TextView>(R.id.txt_dialog_message)
                txtTitle.text = getString(R.string.message_title_confirm)
                txtMessage.text = "Bạn có muốn hoàn thành sự cố?"
                val builder = AlertDialog.Builder(this@UpdateActivity)
                builder.setView(layout)
                builder.setCancelable(false)
                        .setPositiveButton(R.string.message_btn_ok) { dialog: DialogInterface?, i: Int ->
                            mLLayoutProgress!!.visibility = View.VISIBLE
                            mLLayoutMain!!.visibility = View.GONE
                            mTxtProgress!!.text = "Đang lưu..."
                            EditAsync(this@UpdateActivity, true,
                                    mArcGISFeature!!, mLLayoutField!!, null,
                                  object:  EditAsync.AsyncResponse {
                                      override fun processFinish(feature: ArcGISFeature?) {
                                          mLLayoutProgress!!.visibility = View.GONE
                                          mLLayoutMain!!.visibility = View.VISIBLE
                                          if (feature != null) {
                                              Toast.makeText(this@UpdateActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                              //                                mActivity.goHome();
                                          } else Toast.makeText(this@UpdateActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                                      }
                                  }).execute()

                        }.setNegativeButton(R.string.message_btn_cancel) { dialog: DialogInterface?, i: Int -> }
                val dialog = builder.create()
                dialog.show()
            }
            R.id.btn_update_feature_update -> {
                mLLayoutProgress!!.visibility = View.VISIBLE
                mLLayoutMain!!.visibility = View.GONE
                mTxtProgress!!.text = "Đang lưu..."
                EditAsync(this@UpdateActivity, false,
                        mArcGISFeature!!, mLLayoutField!!, null,
                      object:  EditAsync.AsyncResponse {
                          override fun processFinish(feature: ArcGISFeature?) {
                              mLLayoutProgress!!.visibility = View.GONE
                              mLLayoutMain!!.visibility = View.VISIBLE
                              if (feature != null) {
                                  Toast.makeText(mBtnUpdate!!.context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                  //                                mActivity.goHome();
                              } else Toast.makeText(mBtnUpdate!!.context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                          }
                      }).execute()

            }
        }
    }

    override fun onBackPressed() {
        goHome()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun goHome() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}