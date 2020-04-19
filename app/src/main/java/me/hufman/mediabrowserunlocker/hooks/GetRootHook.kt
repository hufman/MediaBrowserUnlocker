package me.hufman.mediabrowserunlocker.hooks

import android.os.Bundle
import de.robv.android.xposed.XC_MethodHook
import me.hufman.mediabrowserunlocker.UnlockerEngine
import java.lang.reflect.Method


/**
 * After onGetRoot, take some actions:
 *   - log the returned root name, if any, for the client app
 *   - optionally alter the returned root
 */
class GetRootHook(val servicePackage: String, val unlockerEngine: UnlockerEngine): XC_MethodHook() {
	override fun beforeHookedMethod(param: MethodHookParam?) {
		param ?: return
		param.args[0] = "com.google.android.projection.gearhead"    // audible does setup with this
		param.args[1] = 1000    // magic UID
	}

	override fun afterHookedMethod(param: MethodHookParam?) {
		val clientPackage = param?.args?.getOrNull(0) as? String
		if (clientPackage == null) {
			// Unexpected, onGetRoot should always have a String clientPackageName
			return
		}

		// unpack the original result, which might be an obfuscated type, so we have to use reflection
		val originalResult = param.result
		val originalRoot = if (originalResult == null) { null } else {
			val originalRootField = originalResult.javaClass.declaredFields.first {it.type == String::class.java}
			originalRootField.isAccessible = true
			originalRootField.get(originalResult) as? String
		}
		val originalExtras = if (originalResult == null) { null } else {
			val originalExtrasField = originalResult.javaClass.declaredFields.first {it.type == Bundle::class.java}
			originalExtrasField.isAccessible = true
			originalExtrasField.get(originalResult) as? Bundle
		}

		val newRoot = unlockerEngine.getRoot(servicePackage, clientPackage, originalRoot)

		// update the newRoot if needed
		if (newRoot != originalRoot) {
			if (newRoot == null) {
				param.result = null
			} else {
				// Replacing the BrowserRoot seems to make MediaBrowserServiceCompat complain
				try {
					val constructor = (param.method as Method).returnType.getDeclaredConstructor(
						String::class.java,
						Bundle::class.java
					)
					param.result = constructor.newInstance(
						newRoot,
						originalExtras
					)
				} catch (e: NoSuchMethodException) {
					// weird BrowserRoot type that doesn't have a string and a bundle constructor
					// don't try to change the result, then
				}
			}
		}
	}

}