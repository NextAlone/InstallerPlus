package ltd.nextalone.pkginstallerplus.utils

import java.lang.reflect.Field

fun findField(clazz: Class<*>?, type: Class<*>?, name: String?): Field? {
    if (clazz != null && name?.length!! > 0) {
        var clz: Class<*> = clazz
        do {
            for (field in clz.declaredFields) {
                if ((type == null || field.type == type) && (field.name == name)
                ) {
                    field.isAccessible = true
                    return field
                }
            }
        } while (clz.superclass.also { clz = it } != null)
    }
    return null
}

fun iGetObjectOrNull(obj: Any, name: String?): Any? {
    return iGetObjectOrNull<Any>(obj, name, null)
}

fun <T> iGetObjectOrNull(obj: Any, name: String?, type: Class<T>?): T? {
    val clazz: Class<*> = obj.javaClass
    try {
        val f: Field = findField(clazz, type, name) as Field
        f.isAccessible = true
        return f[obj] as T
    } catch (e: Exception) {
    }
    return null
}

fun iPutObject(obj: Any, name: String?, value: Any?) {
    iPutObject(obj, name, null, value)
}

fun iPutObject(obj: Any, name: String?, type: Class<*>?, value: Any?) {
    val clazz: Class<*> = obj.javaClass
    try {
        val f: Field = findField(clazz, type, name) as Field
        f.isAccessible = true
        f[obj] = value
    } catch (e: java.lang.Exception) {
    }
}

internal fun Any.get(objName: String): Any? = this.get(objName, null)

internal fun <T> Any.get(objName: String, clz: Class<T>? = null): T? = iGetObjectOrNull(this, objName, clz)

internal fun Any.set(name: String, value: Any): Any = iPutObject(this, name, value)

internal fun Any.set(name: String, clz: Class<*>?, value: Any): Any = iPutObject(this, name, clz, value)

