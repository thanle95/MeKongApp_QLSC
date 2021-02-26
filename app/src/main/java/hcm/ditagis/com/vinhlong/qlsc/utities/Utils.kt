package hcm.ditagis.com.vinhlong.qlsc.utities

object Utils {
    const val CHANGE_PASSWORD_OLD_PASSWORD_WRONG = -1
    const val CHANGE_PASSWORD_FAILURE = 0
    const val CHANGE_PASSWORD_SUCCESS = 1
    var instance: Utils? = null
        get() {
            if (field == null) {
                field = Utils
            }
            return field
        }
        private set
}