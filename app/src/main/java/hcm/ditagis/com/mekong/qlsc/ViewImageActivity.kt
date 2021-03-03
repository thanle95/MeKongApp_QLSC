package hcm.ditagis.com.mekong.qlsc

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import kotlinx.android.synthetic.main.activity_view_image.*

class ViewImageActivity : AppCompatActivity() {
    private lateinit var mApplication: DApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        mApplication = application as DApplication
        var bitmap = mApplication.selectedBitmap
        title = mApplication.selectedAttachment!!.name
        img_view_attachment.setImageBitmap(bitmap)


        btn_rorate_left.setOnClickListener {
            runOnUiThread {
                val matrix = Matrix()

                matrix.postRotate(-90F)
                val bitmap = (img_view_attachment.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

                val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                img_view_attachment.setImageBitmap(rotatedBitmap)
            }
        }
        btn_rorate_right.setOnClickListener {
            runOnUiThread {
                val matrix = Matrix()

                matrix.postRotate(90F)
                val bitmap = (img_view_attachment.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

                val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                img_view_attachment.setImageBitmap(rotatedBitmap)
            }
        }


    }
}
