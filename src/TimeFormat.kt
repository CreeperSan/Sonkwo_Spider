
import java.text.SimpleDateFormat
import java.util.*

fun getStrTime(cc_time: String): String {
    var re_StrTime: String? = null

    val sdf = SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒")
    val lcc_time = java.lang.Long.valueOf(cc_time)!!
    re_StrTime = sdf.format(Date(lcc_time * 1000L))

    return re_StrTime
}
