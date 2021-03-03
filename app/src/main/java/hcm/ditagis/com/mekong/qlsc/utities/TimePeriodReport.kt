package hcm.ditagis.com.mekong.qlsc.utities

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import hcm.ditagis.com.mekong.qlsc.R
import hcm.ditagis.com.mekong.qlsc.adapter.ThongKeAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by NGUYEN HONG on 4/26/2018.
 */
class TimePeriodReport(private val mContext: Context) {
    private val calendar: Calendar
    private val today: Date
    private var items: MutableList<ThongKeAdapter.Item>
    fun getItems(): MutableList<ThongKeAdapter.Item> {
        return items
    }

    fun setItems(items: MutableList<ThongKeAdapter.Item>) {
        this.items = items
    }

    private fun formatTimeToGMT(date: Date): String {
        @SuppressLint("SimpleDateFormat") val dateFormatGmt = SimpleDateFormat(mContext.getString(R.string.format_day_yearfirst))
        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormatGmt.format(date)
    }

    private fun dayToFirstDayString(firstDate: Date, lastDate: Date): String {
        return DateFormat.format(mContext.getString(R.string.format_time_day_month_year), firstDate).toString() + " - " + DateFormat.format(mContext.getString(R.string.format_time_day_month_year), lastDate)
    }

    private val firstDayofMonth: Date
        private get() {
            resetToday()
            calendar[Calendar.DAY_OF_MONTH] = 1
            return calendar.time
        }

    private val lastDayofMonth: Date
        private get() {
            actualMaximumToday
            calendar.add(Calendar.MONTH, 1)
            calendar[Calendar.DAY_OF_MONTH] = 1
            calendar.add(Calendar.DATE, -1)
            return calendar.time
        }

    private val firstDayofLastMonth: Date
        private get() {
            resetToday()
            calendar.add(Calendar.MONTH, -1)
            calendar[Calendar.DAY_OF_MONTH] = 1
            return calendar.time
        }

    private val lastDayofLastMonth: Date
        private get() {
            actualMaximumToday
            calendar[Calendar.DAY_OF_MONTH] = 1
            calendar.add(Calendar.DATE, -1)
            return calendar.time
        }

    private val firstDayofLast3Months: Date
        private get() {
            resetToday()
            calendar.add(Calendar.MONTH, -2)
            calendar[Calendar.DAY_OF_MONTH] = 1
            return calendar.time
        }

    private val lastDayofLast3Months: Date
        private get() {
            actualMaximumToday
            calendar.add(Calendar.MONTH, 1)
            calendar[Calendar.DAY_OF_MONTH] = 1
            calendar.add(Calendar.DATE, -1)
            return calendar.time
        }

    private val firstDayofLast6Months: Date
        private get() {
            resetToday()
            calendar.add(Calendar.MONTH, -5)
            calendar[Calendar.DAY_OF_MONTH] = 1
            return calendar.time
        }

    private val lastDayofLast6Months: Date
        private get() {
            actualMaximumToday
            calendar.add(Calendar.MONTH, 1)
            calendar[Calendar.DAY_OF_MONTH] = 1
            calendar.add(Calendar.DATE, -1)
            return calendar.time
        }

    private val firstDayofYear: Date
        private get() {
            resetToday()
            calendar[Calendar.DAY_OF_YEAR] = 1
            return calendar.time
        }

    private val lastDayofYear: Date
        private get() {
            actualMaximumToday
            calendar[Calendar.DAY_OF_MONTH] = 31
            calendar[Calendar.MONTH] = 11
            return calendar.time
        }

    private val firstDayoflLastYear: Date
        private get() {
            resetToday()
            calendar.add(Calendar.YEAR, -1)
            calendar[Calendar.DAY_OF_YEAR] = 1
            return calendar.time
        }

    private val lastDayofLastYear: Date
        private get() {
            actualMaximumToday
            calendar.add(Calendar.YEAR, -1)
            calendar[Calendar.DAY_OF_MONTH] = 31
            calendar[Calendar.MONTH] = 11
            return calendar.time
        }

    private fun resetToday() {
        calendar.time = today
        calendar[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)
    }

    private val actualMaximumToday: Unit
        private get() {
            calendar.time = today
            calendar[Calendar.HOUR_OF_DAY] = 23
            calendar[Calendar.MINUTE] = 59
            calendar[Calendar.SECOND] = 59
            calendar[Calendar.MILLISECOND] = 999
        }

    init {
        today = Date()
        calendar = Calendar.getInstance()
        items = ArrayList()
        items.add(ThongKeAdapter.Item(1, "Tất cả", null, null, null))
        items.add(ThongKeAdapter.Item(2, "Tháng này", formatTimeToGMT(firstDayofMonth), formatTimeToGMT(lastDayofMonth), dayToFirstDayString(firstDayofMonth, lastDayofMonth)))
        items.add(ThongKeAdapter.Item(3, "Tháng trước", formatTimeToGMT(firstDayofLastMonth), formatTimeToGMT(lastDayofLastMonth), dayToFirstDayString(firstDayofLastMonth, lastDayofLastMonth)))
        items.add(ThongKeAdapter.Item(4, "3 tháng gần nhất", formatTimeToGMT(firstDayofLast3Months), formatTimeToGMT(lastDayofLast3Months), dayToFirstDayString(firstDayofLast3Months, lastDayofLast3Months)))
        items.add(ThongKeAdapter.Item(5, "6 tháng gần nhất", formatTimeToGMT(firstDayofLast6Months), formatTimeToGMT(lastDayofLast6Months), dayToFirstDayString(firstDayofLast6Months, lastDayofLast6Months)))
        items.add(ThongKeAdapter.Item(6, "Năm nay", formatTimeToGMT(firstDayofYear), formatTimeToGMT(lastDayofYear), dayToFirstDayString(firstDayofYear, lastDayofYear)))
        items.add(ThongKeAdapter.Item(7, "Năm trước", formatTimeToGMT(firstDayoflLastYear), formatTimeToGMT(lastDayofLastYear), dayToFirstDayString(firstDayoflLastYear, lastDayofLastYear)))
        items.add(ThongKeAdapter.Item(8, "Tùy chỉnh", null, null, "-- - --"))
    }
}