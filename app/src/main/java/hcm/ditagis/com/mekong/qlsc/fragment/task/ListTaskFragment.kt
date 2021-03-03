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
import hcm.ditagis.com.mekong.qlsc.databinding.FragmentListTaskListBinding
import hcm.ditagis.com.mekong.qlsc.entities.DApplication

@SuppressLint("ValidFragment")
class ListTaskFragment @RequiresApi(api = Build.VERSION_CODES.N) @SuppressLint("ValidFragment") constructor(private val mActivity: ListTaskActivity, inflater: LayoutInflater) : Fragment() {
    var mLLayoutChuaXuLy: LinearLayout? = null
    var mTxtChuaXuLy: TextView? = null
    private var mSwipe: SwipeRefreshLayout? = null
    private val mApplication: DApplication = mActivity.application as DApplication
    private var _mBinding: FragmentListTaskListBinding? = null
    private val mBinding get() = _mBinding
    private fun loadTasks() {
        mLLayoutChuaXuLy!!.removeAllViews()
        val queryParameters = QueryParameters()
        @SuppressLint("DefaultLocale") val queryClause = mApplication.dFeatureLayer!!.layer.definitionExpression
        queryParameters.whereClause = queryClause
        QueryServiceFeatureTableGetListAsync(mActivity, mApplication.dFeatureLayer!!.serviceFeatureTable,
                object: QueryServiceFeatureTableGetListAsync.AsyncResponse {
                    override fun processFinish(output: List<Feature>?) {

                        if (output != null && output.isNotEmpty()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                               val bindingViews = HandlingSearchHasDone.handleFromItems(mBinding!!.root.context, output, mApplication.dFeatureLayer!!.serviceFeatureTable)
                                for (bindingView in bindingViews) {
                                    bindingView.root.setOnClickListener { v: View? -> mActivity.itemClick(bindingView.txtId.text.toString(),
                                            bindingView.txtObjectid.text.toString()) }
                                    mLLayoutChuaXuLy!!.addView(bindingView.root)
                                }
                            } else {
                                Snackbar.make(mBinding!!.root, "Phiên bản điện thoại cần lớn hơn " + Build.VERSION_CODES.N, Snackbar.LENGTH_LONG).show()
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
        _mBinding = FragmentListTaskListBinding.inflate(inflater, container, false)
        mSwipe = mBinding!!.swipeListTask
        mLLayoutChuaXuLy = mBinding!!.llayoutListTaskChuaXuLy
        mTxtChuaXuLy = mBinding!!.txtListTaskChuaXuLy
        mTxtChuaXuLy!!.setOnClickListener { view: View -> onClick(view) }
        mSwipe!!.setOnRefreshListener {
            loadTasks()
            mSwipe!!.isRefreshing = false
        }
        loadTasks()
        return mBinding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }
}