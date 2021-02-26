package hcm.ditagis.com.vinhlong.qlsc.utities

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by ThanLe on 27/10/2017.
 */
object CheckConnectInternet {
    @JvmStatic
    fun isOnline(activity: Activity): Boolean {
        val cm = (activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}