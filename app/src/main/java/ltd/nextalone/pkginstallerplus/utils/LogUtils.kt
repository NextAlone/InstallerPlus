package ltd.nextalone.pkginstallerplus.utils

import android.util.Log

internal const val TAG = "NextAlone"
internal fun logDebug(msg: String) {
    Log.d(TAG, msg)
}

internal fun logError(msg: String) {
    Log.e(TAG, msg)
}

internal fun logThrowable(msg: String, t: Throwable? = null) {
    Log.e(TAG, msg + t?.message, t)
}

internal fun <T : Any> T.logDetail(info: String, vararg msg: Any) {
    logDebug("${this.javaClass.simpleName}: $info, ${msg.joinToString(", ")}")
}

internal fun <T : Any> T.logStart() {
    logDebug("$this: Start")
}

internal fun <T : Any> T.logBefore(msg: String = "") {
    logDebug("$this: Before, $msg")
}

internal fun <T : Any> T.logAfter(msg: String = "") {
    logDebug("$this: After, $msg")
}
