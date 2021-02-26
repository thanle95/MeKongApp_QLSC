package hcm.ditagis.com.vinhlong.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.utities.Constant
import java.text.ParseException
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
class QueryFeatureAsync(activity: Activity, trangThai: Int, diaChi: String, thoiGianPhanAnh: String
                        , delegate: AsyncResponse) : AsyncTask<Void?, List<Feature>?, Void?>() {
    @SuppressLint("StaticFieldLeak")
    private val mDelegate: AsyncResponse
    private val mApplication: DApplication
    private val mServiceFeatureTable: ServiceFeatureTable
    private val mTrangThai: Int
    private val mDiaChi: String
    private var mThoiGian: String
    private var mHasTime = false

    interface AsyncResponse {
        fun processFinish(output: List<Feature>?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    private fun formatTimeToGMT(date: Date): String {
        val dateFormatGmt = Constant.DateFormat.DATE_FORMAT_YEAR_FIRST
        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormatGmt.format(date)
    }

    @SuppressLint("DefaultLocale")
    override fun doInBackground(vararg aVoids: Void?): Void? {
        try {
            val queryParameters = QueryParameters()
            @SuppressLint("DefaultLocale") val queryClause = StringBuilder(String.format("( %s like N'%%%s%%' or %s is null)",
                    Constant.FieldSuCo.DIA_CHI, mDiaChi,
                    Constant.FieldSuCo.DIA_CHI))
            if (mHasTime) queryClause.append(String.format(" and %s > date '%s'", Constant.FieldSuCo.TG_PHAN_ANH, mThoiGian))
            if (mTrangThai != -1) {
                queryClause.append(String.format(" and %s = %d",
                        Constant.FieldSuCo.TRANG_THAI, mTrangThai))
            }
            queryParameters.whereClause = queryClause.toString() + " and " + mApplication.dFeatureLayer!!.layer.definitionExpression
            val featureQueryResultListenableFuture = mServiceFeatureTable.queryFeaturesAsync(queryParameters,
                    ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
            featureQueryResultListenableFuture.addDoneListener {
                try {
                    val result = featureQueryResultListenableFuture.get()
                    val iterator: Iterator<Feature> = result.iterator()
                    var item: Feature
                    val features: MutableList<Feature> = ArrayList()
                    while (iterator.hasNext()) {
                        item = iterator.next()
                        features.add(item)
                    }
                    publishProgress(features)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    publishProgress()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                    publishProgress()
                }
            }
        } catch (e: Exception) {
            publishProgress()
        }
        return null
    }

    override fun onProgressUpdate(vararg values: List<Feature>?) {
        if (values.isNotEmpty()) mDelegate.processFinish(values[0])
        else mDelegate.processFinish(null)
    }

    override fun onPostExecute(result: Void?) {}

    init {
        mApplication = activity.application as DApplication
        mServiceFeatureTable = mApplication.dFeatureLayer!!.serviceFeatureTable
        mDelegate = delegate
        mTrangThai = trangThai
        mDiaChi = diaChi
        mThoiGian = thoiGianPhanAnh
        try {
            val date = Constant.DateFormat.DATE_FORMAT.parse(thoiGianPhanAnh)
            mThoiGian = formatTimeToGMT(date)
            mHasTime = true
        } catch (e: ParseException) {
            mHasTime = false
            e.printStackTrace()
        }
    }
}