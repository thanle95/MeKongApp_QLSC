package hcm.ditagis.com.mekong.qlsc.fragment.task

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.google.android.material.snackbar.Snackbar
import hcm.ditagis.com.mekong.qlsc.ListTaskActivity
import hcm.ditagis.com.mekong.qlsc.R
import hcm.ditagis.com.mekong.qlsc.async.QueryServiceFeatureTableGetListAsync
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import kotlinx.android.synthetic.main.fragment_list_task_list.view.*
import kotlinx.android.synthetic.main.item_tracuu.view.*

@SuppressLint("ValidFragment")
class ListTaskFragment @RequiresApi(api = Build.VERSION_CODES.N) @SuppressLint("ValidFragment") constructor(private val mActivity: ListTaskActivity, inflater: LayoutInflater) : Fragment() {
    var mLLayoutChuaXuLy: LinearLayout? = null
    var mTxtChuaXuLy: TextView? = null
    private val mRootView: View
    private var mSwipe: SwipeRefreshLayout? = null
    private val mApplication: DApplication

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun init() {
        mSwipe = mRootView.swipe_list_task
        mLLayoutChuaXuLy = mRootView.llayout_list_task_chua_xu_ly
        mTxtChuaXuLy = mRootView.txt_list_task_chua_xu_ly
        mTxtChuaXuLy!!.setOnClickListener { view: View -> onClick(view) }
        mSwipe!!.setOnRefreshListener {
            loadTasks()
            mSwipe!!.isRefreshing = false
        }
        loadTasks()
    }

    private fun loadTasks() {
        mLLayoutChuaXuLy!!.removeAllViews()
        val queryParameters = QueryParameters()
        @SuppressLint("DefaultLocale") val queryClause = mApplication.dFeatureLayer!!.layer.definitionExpression
        queryParameters.whereClause = queryClause
        QueryServiceFeatureTableGetListAsync(mActivity, mApplication.dFeatureLayer!!.serviceFeatureTable,
                object: QueryServiceFeatureTableGetListAsync.AsyncResponse {
                    override fun processFinish(output: List<Feature>?) {

                        if (output != null && output.isNotEmpty()) {
                            var views: List<View?>? = null
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                views = HandlingSearchHasDone.handleFromItems(mRootView.context, output, mApplication.dFeatureLayer!!.serviceFeatureTable)
                                for (view in views) {
                                    val txtID = view!!.txt_id
                                    view!!.setOnClickListener { v: View? -> mActivity.itemClick(txtID.text.toString(), view.txt_objectid.text.toString()) }
                                    mLLayoutChuaXuLy!!.addView(view)
                                }
                            } else {
                                Snackbar.make(mRootView, "Phiên bản điện thoại cần lớn hơn " + Build.VERSION_CODES.N, Snackbar.LENGTH_LONG).show()
                            }
                        }
                        mTxtChuaXuLy!!.text = mActivity.resources.getString(R.string.txt_list_task_chua_xu_ly, mLLayoutChuaXuLy!!.childCount)
                    }
                }).execute(queryParameters)


    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.txt_list_task_chua_xu_ly -> if (mLLayoutChuaXuLy!!.visibility == View.VISIBLE) mLLayoutChuaXuLy!!.visibility = View.GONE else mLLayoutChuaXuLy!!.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mRootView
    }

    init {
        mApplication = mActivity.application as DApplication
        mRootView = inflater.inflate(R.layout.fragment_list_task_list, null)
        init()
    }
}