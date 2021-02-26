package hcm.ditagis.com.vinhlong.qlsc.entities

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class CustomLinearLayout : LinearLayout {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}