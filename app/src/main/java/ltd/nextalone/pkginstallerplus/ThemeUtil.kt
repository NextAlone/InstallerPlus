package ltd.nextalone.pkginstallerplus

import android.R
import android.content.Context
import android.util.TypedValue


object ThemeUtil {

    var colorPrimary: Int = 0xFF000000.toInt()
    var colorAccent: Int = 0xFF000000.toInt()
    var colorRed: Int = 0xFFDA3633.toInt()
    var colorGreen: Int = 0xFF238636.toInt()

    fun init(ctx: Context) {
        val value = TypedValue()
        if (ctx.theme?.resolveAttribute(R.attr.colorPrimary, value, true) == true) {
            colorPrimary = value.data
        }
        if (ctx.theme?.resolveAttribute(R.attr.colorAccent, value, true) == true) {
            colorAccent = value.data
        }
//        if (isColorDark(colorAccent)) {
//            //daylight
//        } else {
//            //black
//        }
    }

    private fun isColorDark(c: Int): Boolean {
        val r: Int = (c shr 16) and 0xFF;
        val g: Int = (c shr 8) and 0xFF;
        val b: Int = (c shr 0) and 0xFF;
        return (r + g + b) / 3 < 128;
    }
}