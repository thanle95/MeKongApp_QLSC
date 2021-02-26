package hcm.ditagis.com.vinhlong.qlsc.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import hcm.ditagis.com.vinhlong.qlsc.R

/**
 * Created by ThanLe on 04/10/2017.
 */
class FeatureViewInfoAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<FeatureViewInfoAdapter.Item>(mContext, 0, items) {
    fun getItems(): List<Item> {
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
            convertView = inflater.inflate(R.layout.item_viewinfo, null)
        }
        val item = items[position]
        val txtAlias = convertView!!.findViewById<TextView>(R.id.txt_viewinfo_alias)
        txtAlias.setText(item.alias)
        val txtValue = convertView.findViewById<TextView>(R.id.txt_viewinfo_value)
        txtValue.text = item.value
        if (item.value == null) txtValue.visibility = View.GONE else txtValue.visibility = View.VISIBLE
        return convertView
    }

    class Item {
         var alias: String? = null

        var value: String? = null
        var fieldName: String? = null

    }

}