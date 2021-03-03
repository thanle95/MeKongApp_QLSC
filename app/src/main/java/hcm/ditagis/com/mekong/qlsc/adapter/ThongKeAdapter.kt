package hcm.ditagis.com.mekong.qlsc.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import hcm.ditagis.com.mekong.qlsc.R
import hcm.ditagis.com.mekong.qlsc.databinding.ItemThoigianThongkeBinding

/**
 * Created by ThanLe on 04/10/2017.
 */
class ThongKeAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<ThongKeAdapter.Item>(mContext, 0, items) {
    private class DHolder(var binding: ItemThoigianThongkeBinding){
        var view: View = binding.root
    }
    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: DHolder
        if (convertView == null) {
            val inflater = (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            holder = DHolder(ItemThoigianThongkeBinding.inflate(inflater))
            holder.view.tag = holder
        }
        else{
            holder = convertView.tag as DHolder
        }
        val item = items[position]
        holder.binding.txtThongkeMota.text = item.mota
        if (item.thoigianhienthi != null) {
            holder.binding.txtThongkeThoigian.text = item.thoigianhienthi
        }
        val imageView = holder.binding.imgSelectTime
        if (item.isChecked) {
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.INVISIBLE
        }
        return holder.view
    }

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

    class Item {
        var id = 0
        var mota: String? = null
        var thoigianbatdau: String? = null
        var thoigianketthuc: String? = null
        var thoigianhienthi: String? = null
        var isChecked = false

        constructor(id: Int, mota: String?, thoigianbatdau: String?, thoigianketthuc: String?, thoigianhienthi: String?) {
            this.id = id
            this.mota = mota
            this.thoigianbatdau = thoigianbatdau
            this.thoigianketthuc = thoigianketthuc
            this.thoigianhienthi = thoigianhienthi
        }

    }

}