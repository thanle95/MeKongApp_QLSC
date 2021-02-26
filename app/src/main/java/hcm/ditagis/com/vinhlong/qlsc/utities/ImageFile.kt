package hcm.ditagis.com.vinhlong.qlsc.utities

import android.content.Context
import android.os.Environment
import hcm.ditagis.com.vinhlong.qlsc.R
import java.io.File

/**
 * Created by ThanLe on 12/8/2017.
 */
object ImageFile {
    @JvmStatic
    fun getFile(context: Context): File {
        val path = Environment.getExternalStorageDirectory().path
        val outFile = File(path, context.resources.getString(R.string.path_saveImage))
        if (!outFile.exists()) outFile.mkdir()
        return File(outFile, "xxx.png")
    }
}