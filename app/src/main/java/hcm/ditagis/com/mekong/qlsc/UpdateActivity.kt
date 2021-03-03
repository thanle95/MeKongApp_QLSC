package hcm.ditagis.com.mekong.qlsc

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ArcGISFeature
import hcm.ditagis.com.mekong.qlsc.async.EditAsync
import hcm.ditagis.com.mekong.qlsc.async.LoadingDataFeatureAsync
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import kotlinx.android.synthetic.main.fragment_update_feature.*
import kotlinx.android.synthetic.main.layout_dialog.view.*
import java.util.*

class UpdateActivity : AppCompatActivity() {
    private var mApplication: DApplication? = null
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
        btn_update_feature_complete!!.setOnClickListener { view: View -> onClick(view) }
        btn_update_feature_update!!.setOnClickListener { view: View -> onClick(view) }
        if (mApplication!!.images != null)
            mApplication!!.images = arrayListOf()
        txt_update_feature_progress!!.text = "Đang khởi tạo thuộc tính..."
        llayout_update_feature_progress!!.visibility = View.VISIBLE
        llayout_update_feature_main!!.visibility = View.GONE
        mArcGISFeature = mApplication!!.selectedArcGISFeature
        swipe_udpate_feature!!.setOnRefreshListener {
            loadData()
            swipe_udpate_feature!!.isRefreshing = false
        }
        loadData()
    }

    private fun loadData() {
        llayout_update_feature_field!!.removeAllViews()
        llayout_update_feature_progress!!.visibility = View.VISIBLE
        llayout_update_feature_main!!.visibility = View.GONE
        LoadingDataFeatureAsync(this@UpdateActivity, mArcGISFeature!!.featureTable.fields,
                object: LoadingDataFeatureAsync.AsyncResponse {
                    override fun processFinish(views: List<View?>?) {
                        if (views != null) for (view1 in views) {
                            llayout_update_feature_field!!.addView(view1)
                        }
                        llayout_update_feature_progress!!.visibility = View.GONE
                        llayout_update_feature_main!!.visibility = View.VISIBLE
                    }
                }, mArcGISFeature).execute(false)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_update_feature_complete -> {
                val layout = layoutInflater.inflate(R.layout.layout_dialog, null) as LinearLayout
                layout.txt_dialog_title.text = getString(R.string.message_title_confirm)
                layout.txt_dialog_message.text = "Bạn có muốn hoàn thành sự cố?"
                val builder = AlertDialog.Builder(this@UpdateActivity)
                builder.setView(layout)
                builder.setCancelable(false)
                        .setPositiveButton(R.string.message_btn_ok) { dialog: DialogInterface?, i: Int ->
                            llayout_update_feature_progress!!.visibility = View.VISIBLE
                            llayout_update_feature_main!!.visibility = View.GONE
                            txt_update_feature_progress!!.text = "Đang lưu..."
                            EditAsync(this@UpdateActivity, true,
                                    mArcGISFeature!!, llayout_update_feature_field!!, null,
                                  object:  EditAsync.AsyncResponse {
                                      override fun processFinish(feature: ArcGISFeature?) {
                                          llayout_update_feature_progress!!.visibility = View.GONE
                                          llayout_update_feature_main!!.visibility = View.VISIBLE
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
                llayout_update_feature_progress!!.visibility = View.VISIBLE
                llayout_update_feature_main!!.visibility = View.GONE
                txt_update_feature_progress!!.text = "Đang lưu..."
                EditAsync(this@UpdateActivity, false,
                        mArcGISFeature!!, llayout_update_feature_field!!, null,
                      object:  EditAsync.AsyncResponse {
                          override fun processFinish(feature: ArcGISFeature?) {
                              llayout_update_feature_progress!!.visibility = View.GONE
                              llayout_update_feature_main!!.visibility = View.VISIBLE
                              if (feature != null) {
                                  Toast.makeText(btn_update_feature_update!!.context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                  //                                mActivity.goHome();
                              } else Toast.makeText(btn_update_feature_update!!.context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
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