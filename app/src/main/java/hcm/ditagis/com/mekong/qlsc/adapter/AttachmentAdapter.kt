package hcm.ditagis.com.mekong.qlsc.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import hcm.ditagis.com.mekong.qlsc.R
import hcm.ditagis.com.mekong.qlsc.entities.DAttachment
import kotlinx.android.synthetic.main.item_add_attachment.view.*


class AttachmentAdapter(private val mContext: Context, private var items: MutableList<DAttachment>) : ArrayAdapter<DAttachment>(mContext, 0, items) {

    fun getItems(): MutableList<DAttachment> {
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

    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var tmpView = convertView
        if (tmpView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            tmpView = inflater.inflate(R.layout.item_add_attachment, null)
        }
        if (tmpView != null) {
            val item = items[position]
            if (item.image != null)
                {
                    tmpView.img_add_attachment.visibility = View.INVISIBLE
                    val background = BitmapDrawable(mContext.resources, item.image)
                    tmpView.layout_add_attachment.background = background
                } else {
                tmpView.img_add_attachment.visibility = View.VISIBLE
                tmpView.layout_add_attachment.background = mContext.getDrawable(R.drawable.layout_border_dashed)
            }
            tmpView.txt_add_attachment__title.text = item.title
            return tmpView
        } else {
                    val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    return inflater.inflate(R.layout.item_add_attachment, null)
                }

    }

}