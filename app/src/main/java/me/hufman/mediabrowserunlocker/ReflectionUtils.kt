package me.hufman.mediabrowserunlocker

import java.lang.reflect.Method

fun Method.isOverrides(): Boolean {
	// look for a parent class with the same name and signature
	var parent: Class<*>? = this.declaringClass.superclass
	while (parent != null) {
		try {
			parent.getDeclaredMethod(this.name, *this.parameterTypes)
			return true
		} catch (e: NoSuchMethodException) {
			// not found on this parent, try the next
			parent = parent.superclass
		}
	}
	return false
}