package ltd.nextalone.pkginstallerplus.utils

import android.content.Context

const val MATCH_PARENT = -1
const val WRAP_CONTENT = -2


fun Context.getId(name: String): Int = resources.getIdentifier(name, "id", packageName)

fun Context.dip2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun Context.dip2sp(dpValue: Float): Int {
    val scale = resources.displayMetrics.density / resources.displayMetrics.scaledDensity
    return (dpValue * scale + 0.5f).toInt()
}

fun Context.px2sp(pxValue: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}

