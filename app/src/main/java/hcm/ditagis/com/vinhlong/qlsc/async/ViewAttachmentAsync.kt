package hcm.ditagis.com.vinhlong.qlsc.async

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.FeatureEditResult
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.vinhlong.qlsc.MainActivity
import hcm.ditagis.com.vinhlong.qlsc.R
import hcm.ditagis.com.vinhlong.qlsc.adapter.FeatureViewMoreInfoAttachmentsAdapter
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import kotlinx.android.synthetic.main.layout_viewmoreinfo_feature_attachment.view.*
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.util.*

/**
 * Created by ThanLe on 4/16/2018.
 */
class ViewAttachmentAsync(private val mMainActivity: MainActivity, selectedArcGISFeature: ArcGISFeature?) : AsyncTask<Void?, Int?, Void?>() {
    private val mDialog: ProgressDialog?
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var builder: AlertDialog.Builder? = null
    private var layout: View? = null
    private val mDApplication: DApplication
    private var mAttachments: List<Attachment>? = null
    override fun onPreExecute() {
        super.onPreExecute()
        mDialog!!.setMessage(mMainActivity.getString(R.string.async_dang_lay_hinh_anh_dinh_kem))
        mDialog.setCancelable(false)
        mDialog.setButton("Hủy") { dialogInterface, i -> publishProgress(0) }
        mDialog.show()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        builder = AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val layoutInflater = LayoutInflater.from(mMainActivity)
        layout = layoutInflater.inflate(R.layout.layout_viewmoreinfo_feature_attachment, null)
        if (layout == null) {
            return null
        } else {
            val lstViewAttachment = layout!!.lstView_alertdialog_attachments
            val attachmentsAdapter = FeatureViewMoreInfoAttachmentsAdapter(mMainActivity, ArrayList())
            lstViewAttachment.adapter = attachmentsAdapter
            lstViewAttachment.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, view, i, l ->
                val item = adapterView.getItemAtPosition(i) as FeatureViewMoreInfoAttachmentsAdapter.Item
                val name = item.name!!.split("_").toTypedArray()
                if (name.size > 1 && (name[1] == mDApplication.user!!.userName)) {
                    val builder = android.app.AlertDialog.Builder(layout!!.getContext(), R.style.DDialogBuilder)
                    builder.setTitle("Bạn có chắc muốn xóa ảnh này?")
                            .setPositiveButton("Xoá") { dialogInterface: DialogInterface, i1: Int ->
                                mDApplication.selectedArcGISFeature!!.deleteAttachmentAsync(mAttachments!!.get(i)).addDoneListener {
                                    val listListenableFuture: ListenableFuture<List<FeatureEditResult>> = (mDApplication.selectedArcGISFeature!!.featureTable as ServiceFeatureTable).applyEditsAsync()
                                    listListenableFuture.addDoneListener {
                                        try {
                                            listListenableFuture.get()
                                            attachmentsAdapter.remove(item)
                                            attachmentsAdapter.notifyDataSetChanged()
                                            Toast.makeText(layout!!.context, "Xóa thành công", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(layout!!.context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                dialogInterface.dismiss()
                            }.setNegativeButton("Hủy", { dialogInterface: DialogInterface, i12: Int -> dialogInterface.dismiss() })
                    val dialog = builder.create()
                    dialog.show()
                    true
                } else {
                    Toast.makeText(layout!!.getContext(), "Bạn không có quyền xóa ảnh này", Toast.LENGTH_SHORT).show()
                    false
                }
            }
            val attachmentResults = mSelectedArcGISFeature!!.fetchAttachmentsAsync()
            attachmentResults.addDoneListener {
                try {
                    mAttachments = attachmentResults.get()
                    val size: IntArray = intArrayOf(mAttachments!!.size)
                    // if selected feature has attachments, display them in a list fashion
                    if (mAttachments!!.isNotEmpty()) {
                        //
                        for (attachment: Attachment in mAttachments!!) {
                            if (attachment.contentType.toLowerCase(Locale.ROOT).trim { it <= ' ' }.contains("png")) {
                                val item1: FeatureViewMoreInfoAttachmentsAdapter.Item = FeatureViewMoreInfoAttachmentsAdapter.Item()
                                item1.name = attachment.name
                                val inputStreamListenableFuture: ListenableFuture<InputStream> = attachment.fetchDataAsync()
                                inputStreamListenableFuture.addDoneListener {
                                    try {
                                        val inputStream: InputStream = inputStreamListenableFuture.get()
                                        item1.img = IOUtils.toByteArray(inputStream)
                                        attachmentsAdapter.add(item1)
                                        attachmentsAdapter.notifyDataSetChanged()
                                        size[0]--
                                        publishProgress(size.get(0))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    } else {
                        publishProgress(0)
                    }
                } catch (e: Exception) {
                    Log.e("ERROR", e.message)
                }
            }
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        if (values[0] == 0) {
            if (mDialog != null && mDialog.isShowing) {
                mDialog.dismiss()
                builder!!.setView(layout)
                builder!!.setCancelable(false)
                builder!!.setPositiveButton("Thoát") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                val dialog = builder!!.create()
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.show()
            }
        }
        super.onProgressUpdate(*values)
    }

    init {
        mSelectedArcGISFeature = selectedArcGISFeature
        mDialog = ProgressDialog(mMainActivity, android.R.style.Theme_Material_Dialog_Alert)
        mDApplication = mMainActivity.application as DApplication
    }
}