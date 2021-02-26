package hcm.ditagis.com.vinhlong.qlsc

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.android.material.tabs.TabLayout.ViewPagerOnTabSelectedListener
import hcm.ditagis.com.vinhlong.qlsc.entities.DApplication
import hcm.ditagis.com.vinhlong.qlsc.fragment.task.ListTaskFragment
import hcm.ditagis.com.vinhlong.qlsc.fragment.task.SearchFragment
import java.util.*

class ListTaskActivity : AppCompatActivity() {
    private var mListTaskFragment: ListTaskFragment? = null
    private var mSearchFragment: SearchFragment? = null
    private var mApplication: DApplication? = null

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_task)
        mApplication = application as DApplication
        Objects.requireNonNull(supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        Objects.requireNonNull(supportActionBar)?.setDisplayShowHomeEnabled(true)
        val sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        val viewPager = findViewById<ViewPager>(R.id.container_basemap)
        viewPager.adapter = sectionsPagerAdapter
        val tabLayout = findViewById<TabLayout>(R.id.tabs_basemap)
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(ViewPagerOnTabSelectedListener(viewPager))
        mListTaskFragment = ListTaskFragment(this@ListTaskActivity, layoutInflater)
        mSearchFragment = SearchFragment(this@ListTaskActivity, layoutInflater)
        viewPager.setCurrentItem(0, true)
    }

    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> mSearchFragment!!
                1 -> mListTaskFragment!!
                else -> null
            }!!
        }

        override fun getCount(): Int {
            return 2
        }
    }

    //    public void itemClick(AdapterView<?> adapter, int position) {
    //        HandlingSearchHasDone.Item item = (HandlingSearchHasDone.Item) adapter.getItemAtPosition(position);
    //        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_dialog, null);
    //        TextView txtTitle = layout.findViewById(R.id.txt_dialog_title);
    //        TextView txtMessage = layout.findViewById(R.id.txt_dialog_message);
    //        txtTitle.setText(getString(R.string.message_title_confirm));
    //        txtMessage.setText(getString(R.string.message_click_list_task, item.getId()));
    //
    //        AlertDialog.Builder builder = new AlertDialog.Builder(ListTaskActivity.this);
    //        builder.setView(layout);
    //        builder.setCancelable(false)
    //                .setPositiveButton(R.string.message_btn_ok, (dialog, i) -> {
    //                    mApplication.getDiemSuCo().setIdSuCo(item.getId());
    //                    goHome();
    //                }).setNegativeButton(R.string.message_btn_cancel, (dialog, i) -> {
    //        });
    //
    //        AlertDialog dialog = builder.create();
    //        dialog.show();
    //    }
    fun itemClick(id: String?, objectID: String?) {
        val layout = layoutInflater.inflate(R.layout.layout_dialog, null) as LinearLayout
        val txtTitle = layout.findViewById<TextView>(R.id.txt_dialog_title)
        val txtMessage = layout.findViewById<TextView>(R.id.txt_dialog_message)
        txtTitle.text = getString(R.string.message_title_confirm)
        var xID = id
        if(id.isNullOrEmpty())
            xID = objectID
        txtMessage.text = getString(R.string.message_click_list_task, xID)
        val builder = AlertDialog.Builder(this@ListTaskActivity)
        builder.setView(layout)
        builder.setCancelable(false)
                .setPositiveButton(R.string.message_btn_ok) { dialog: DialogInterface?, i: Int ->
                    mApplication!!.diemSuCo!!.objectID = objectID!!.toLong()
                    goHome()
                }.setNegativeButton(R.string.message_btn_cancel) { dialog: DialogInterface?, i: Int -> }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        goHome()
    }

    fun goHome() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}