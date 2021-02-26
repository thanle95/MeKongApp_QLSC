package hcm.ditagis.com.vinhlong.qlsc.utities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import hcm.ditagis.com.vinhlong.qlsc.R

class DAlertDialog {
    private var mDialog: Dialog? = null
    fun show(activity: Activity?, title: String?, vararg message: String?) {
        val builder = AlertDialog.Builder(activity, R.style.DDialogBuilder)
        builder.setTitle(title)
        if (message.isNotEmpty()) builder.setMessage(message[0])
        builder.setPositiveButton("OK") { dialog, which -> dialog.dismiss() }.setCancelable(false)
        if (message.isNotEmpty()) builder.setMessage(message[0])
        mDialog = builder.create()
        mDialog!!.show()
    }

    fun show(activity: Activity?, e: Exception) {
        val builder = AlertDialog.Builder(activity, R.style.DDialogBuilder)
        builder.setTitle("Có lỗi xảy ra")
                .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }.setCancelable(false)
        if (e.message != null) builder.setMessage(e.message) else builder.setMessage(e.toString())
        mDialog = builder.create()
        mDialog!!.show()
    }
}