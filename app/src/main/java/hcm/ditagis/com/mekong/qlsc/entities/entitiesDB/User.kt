package hcm.ditagis.com.mekong.qlsc.entities.entitiesDB

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
        val username: String?,
        val displayName: String?,
        var roleId: String?,
        var accessToken: String?,
        var capability: String?
) : Parcelable {
}