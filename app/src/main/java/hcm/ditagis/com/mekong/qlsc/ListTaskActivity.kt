package hcm.ditagis.com.mekong.qlsc

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.android.material.tabs.TabLayout.ViewPagerOnTabSelectedListener
import hcm.ditagis.com.mekong.qlsc.databinding.ActivityListTaskBinding
import hcm.ditagis.com.mekong.qlsc.databinding.LayoutDialogBinding
import hcm.ditagis.com.mekong.qlsc.entities.DApplication
import hcm.ditagis.com.mekong.qlsc.fragment.task.ListTaskFragment
import hcm.ditagis.com.mekong.qlsc.fragment.task.SearchFragment
import java.util.*

class ListTaskActivity : AppCompatActivity() {
    private var mListTaskFragment: ListTaskFragment? = null
    private var mSearchFragment: SearchFragment? = null
    private var mApplication: DApplication? = null
    private lateinit var mBinding: ActivityListTaskBinding

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityListTaskBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mApplication = application as DApplication
        Objects.requireNonNull(supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        Objects.requireNonNull(supportActionBar)?.setDisplayShowHomeEnabled(true)
        val sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mBinding.containerBasemap
        mBinding.containerBasemap.adapter = sectionsPagerAdapter
        mBinding.containerBasemap.addOnPageChangeListener(TabLayoutOnPageChangeListener(mBinding.tabsBasemap))
        mBinding.tabsBasemap.addOnTabSelectedListener(ViewPagerOnTabSelectedListener(mBinding.containerBasemap))
        mListTaskFragment = ListTaskFragment(this@ListTaskActivity, layoutInflater)
        mSearchFragment = SearchFragment(this@ListTaskActivity, layoutInflater)
        mBinding.containerBasemap.setCurrentItem(0, true)
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

    fun itemClick(id: String?, objectID: String?) {
        val layout =LayoutDialogBinding.inflate(layoutInflater)
        layout.txtDialogTitle.text = getString(R.string.message_title_confirm)
        var xID = id
        if(id.isNullOrEmpty())
            xID = objectID
        layout.txtDialogMessage.text = getString(R.string.message_click_list_task, xID)
        val builder = AlertDialog.Builder(this@ListTaskActivity)
        builder.setView(layout.root)
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