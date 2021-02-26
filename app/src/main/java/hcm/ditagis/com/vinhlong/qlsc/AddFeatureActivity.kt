package hcm.ditagis.com.vinhlong.qlsc

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.layers.FeatureLayer
import hcm.ditagis.com.vinhlong.qlsc.async.AddFeatureAsync
import hcm.ditagis.com.vinhlong.qlsc.async.LoadingDataFeatureAsync
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import hcm.ditagis.com.vinhlong.qlsc.utities.ImageFile.getFile
import kotlinx.android.synthetic.main.activity_add_feature.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*


class AddFeatureActivity : AppCompatActivity(), View.OnClickListener {





    private var mApplication: DApplication? = null
    private var mImages: MutableList<ByteArray>? = null
    private var mUri: Uri? = null
    private val mAdapterLayer: ArrayAdapter<String>? = null
    private var mFeatureLayer: FeatureLayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_feature)
        mApplication = application as DApplication
        initViews()
    }

    private fun initViews() {
        mImages = ArrayList()
        btn_add_feature_capture!!.setOnClickListener { view: View -> onClick(view) }
        btn_add_feature_add!!.setOnClickListener { view: View -> onClick(view) }
        btn_add_feature_pick_photo!!.setOnClickListener { view: View -> onClick(view) }
        Objects.requireNonNull(supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        Objects.requireNonNull(supportActionBar)?.setDisplayShowHomeEnabled(true)
        txt_add_feature_progress!!.text = "Đang khởi tạo thuộc tính..."
        llayout_add_feature_progress!!.visibility = View.VISIBLE
        llayout_add_feature_main!!.visibility = View.GONE
        mFeatureLayer = mApplication!!.dFeatureLayer!!.layer
        LoadingDataFeatureAsync(this@AddFeatureActivity, mFeatureLayer!!.featureTable.fields,
                object : LoadingDataFeatureAsync.AsyncResponse {
                    override fun processFinish(views: List<View?>?) {
                        if (views != null) for (view1 in views) {
                            llayout_add_feature_field!!.addView(view1)
                        }
                        llayout_add_feature_progress!!.visibility = View.GONE
                        llayout_add_feature_main!!.visibility = View.VISIBLE
                    }

                }).execute(true)

    }

    private fun hadPoint(): Boolean {
        return mApplication!!.addFeaturePoint != null
    }

    fun capture() {
        val cameraIntent = Intent(this@AddFeatureActivity, CameraActivity::class.java)
        this.startActivityForResult(cameraIntent, Constant.RequestCode.ADD_FEATURE_ATTACHMENT)

    }

    private fun pickPhoto() {
        val pickPhoto = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, Constant.RequestCode.PICK_PHOTO)
    }

    private fun getByteArrayFromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Constant.CompressFormat.TYPE_COMPRESS, 100, outputStream)
        val image = outputStream.toByteArray()
        try {
            outputStream.close()
        } catch (e: IOException) {

        }

        return image
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_add_feature_add -> if (!hadPoint()) {
                Toast.makeText(this@AddFeatureActivity, R.string.message_add_feature_had_not_point, Toast.LENGTH_LONG).show()
            } else if (mFeatureLayer == null) {
                Toast.makeText(this@AddFeatureActivity, R.string.message_add_feature_had_not_feature, Toast.LENGTH_LONG).show()
            } else {
                llayout_add_feature_progress!!.visibility = View.VISIBLE
                llayout_add_feature_main!!.visibility = View.GONE
                txt_add_feature_progress!!.text = "Đang lưu..."
                AddFeatureAsync(this@AddFeatureActivity, mApplication!!.dFeatureLayer!!.serviceFeatureTable, llayout_add_feature_field!!,
                        object : AddFeatureAsync.AsyncResponse {
                            override fun processFinish(output: Feature?) {

                                if (output != null) {
                                    goHome()
                                }
                                llayout_add_feature_progress!!.visibility = View.GONE
                                llayout_add_feature_main!!.visibility = View.VISIBLE
                            }

                        }).execute()


            }
            R.id.btn_add_feature_capture -> capture()
            R.id.btn_add_feature_pick_photo -> pickPhoto()
        }
    }


    private fun handlingImage(bitmap: Bitmap?, isFromCamera: Boolean) {
        try {
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                val imageView = ImageView(llayout_add_feature_image!!.context)
                imageView.setPadding(0, 0, 0, 10)
                if (isFromCamera) {
                 imageView.setImageBitmap(bitmap)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    imageView.setImageBitmap(bitmap)
                }
                val image = getByteArrayFromBitmap(bitmap)
                Toast.makeText(this, "Đã lưu ảnh", Toast.LENGTH_SHORT).show()
                llayout_add_feature_image!!.addView(imageView)
                mImages!!.add(image)
                mApplication!!.images = mImages
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constant.RequestCode.ADD_FEATURE_ATTACHMENT -> if (resultCode == Activity.RESULT_OK) {

                val bitmaps = mApplication!!.bitmaps!!
                handlingImage(bitmaps.first(), true)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Hủy chụp ảnh", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(this, "Lỗi khi chụp ảnh", Toast.LENGTH_SHORT)
            }
            Constant.RequestCode.PICK_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        handlingImage(bitmap, false)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddFeatureActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        goHomeCancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun goHome() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun goHomeCancel() {
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }
}