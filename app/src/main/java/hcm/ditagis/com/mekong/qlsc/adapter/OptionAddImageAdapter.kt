package hcm.ditagis.com.mekong.qlsc.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import hcm.ditagis.com.mekong.qlsc.R

/**
 * Created by ThanLe on 04/10/2017.
 */
class OptionAddImageAdapter(private val mContext: Context, private val items: MutableList<String>) : ArrayAdapter<String>(mContext, 0, items) {
    fun getItems(): List<String> {
        return items
    }

    override fun clear() {
        items.clear()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            convertView = inflater.inflate(R.layout.item_option, null)
        }
        val item = items[position]
        val txtTitle = convertView!!.findViewById<TextView>(R.id.txt_option_title)
        txtTitle.text = item
        return convertView
    }

}