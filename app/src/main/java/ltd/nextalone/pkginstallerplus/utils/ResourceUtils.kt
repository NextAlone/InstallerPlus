package ltd.nextalone.pkginstallerplus.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View

internal fun Context.hostId(name: String): Int = this.resources.getIdentifier(name, "id", "com.android.packageinstaller")
internal fun Context.hostString(name: String): String = this.getString(this.resources.getIdentifier(name, "String", "com.android.packageinstaller"))

internal fun <T : View?> Any.findHostView(name: String): T? {
    return when (this) {
        is View -> this.context.hostId(name).let { this.findViewById<T>(it) }
        is Activity -> this.hostId(name).let { this.findViewById<T>(it) }
        is Dialog -> this.context.hostId(name).let { this.findViewById<T>(it) }
        else -> null
    }
}
