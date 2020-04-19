package me.hufman.mediabrowserunlocker.hooks

import android.app.Service
import android.os.Bundle
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import me.hufman.mediabrowserunlocker.*
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable

class MediaServiceHook(val packageName: String, val serviceClass: Class<*>): XC_MethodHook() {
	companion object {
		val MEDIA_BROWSER_SERVICE_RESULT = "android.service.media.MediaBrowserService.Result"
		val MEDIA_BROWSER_SERVICE_INTENT_ACTION = "android.media.browse.MediaBrowserService"

		fun discoverOnGetRoot(klass: Class<*>): Method? {
			return klass.declaredMethods.filter {
				it.isOverrides() &&
						it.parameterTypes.contentEquals(arrayOf(String::class.java, Int::class.javaPrimitiveType, Bundle::class.java))
			}.firstOrNull()
		}

		fun discoverLoadChildren(klass: Class<*>): Method? {
			return klass.declaredMethods.filter {
				it.isOverrides() &&
						it.parameterTypes.size == 2 &&
						it.parameterTypes[0] == String::class.java &&
						it.genericParameterTypes[1] is ParameterizedType &&
						(it.genericParameterTypes[1] as ParameterizedType).actualTypeArguments[0] is ParameterizedType &&
						((it.genericParameterTypes[1] as ParameterizedType).actualTypeArguments[0] as ParameterizedType).rawType == List::class.java
			}.firstOrNull()
		}
	}

	override fun afterHookedMethod(param: MethodHookParam?) {
		if (param?.method?.name == "onCreate") onCreate(param.thisObject as Service)
	}
	override fun beforeHookedMethod(param: MethodHookParam?) {
		if (param?.method?.name == "onDestroy") onDestroy(param.thisObject as Service)
	}

	var unlockerEngine: UnlockerServiceClient? = null

	fun onCreate(thisObject: Service) {
//		XposedBridge.log("MediaBrowserUnlocker: ${packageName} started")
		val unlockerEngine = UnlockerServiceClient(thisObject)
		val collation = BrowseResultCollation(unlockerEngine)
		this.unlockerEngine = unlockerEngine

		hookMediaBrowserServiceDeferred(packageName, serviceClass, unlockerEngine, collation)
	}
	fun onDestroy(thisObject: Service) {
//		XposedBridge.log("MediaBrowserUnlocker: ${packageName} destroyed")
		unlockerEngine?.disconnect()
	}

	fun hookMediaBrowserServiceDeferred(servicePackage: String, serviceClass: Class<*>, unlockerEngine: UnlockerEngine, collation: BrowseResultCollation) {
		// find the overridden onGetRoot method, might be obfuscated to a different name
		val rootHook = GetRootHook(
			servicePackage,
			unlockerEngine
		)

		discoverOnGetRoot(serviceClass)?.apply {
			XposedBridge.hookMethod(this, rootHook)
		} ?: XposedBridge.log("Could not find onGetRoot method in $servicePackage")

		// find the overridden loadChildren method, might be obfuscated to a different name
		val childrenHook = LoadChildrenHook(
			servicePackage,
			collation
		)

		discoverLoadChildren(serviceClass)?.apply {
			XposedBridge.hookMethod(this, childrenHook)
			hookSendResult(this.parameterTypes[1], collation)
		} ?: XposedBridge.log("$packageName - Could not find onLoadChildren method in $servicePackage")
	}

	fun hookSendResult(resultClass: Class<*>, collation: BrowseResultCollation) {
		val browseResultHook = BrowseResultHook(collation)
		// the system MediaBrowserService can't be obfuscated, I think
		try {
			val osResult = XposedHelpers.findClass(MEDIA_BROWSER_SERVICE_RESULT, resultClass.classLoader)
			osResult.declaredMethods.filter {
				it.name == "sendResult"
			}.forEach {
				XposedBridge.hookMethod(it, browseResultHook)
			}
		} catch (e: XposedHelpers.ClassNotFoundError) {
			XposedBridge.log("$packageName - Could not find system MediaBrowserService.Result")
			// might be using the compat package
		}

		// the MediaBrowserServiceCompat's sendResult is probably obfuscated
		resultClass.declaredMethods.filter {
			it.genericParameterTypes.size == 1 &&
			it.genericParameterTypes[0] is TypeVariable<*>
		}.map {
			XposedBridge.hookMethod(it, browseResultHook)
		}.getOrElse(0) {
			XposedBridge.log("$packageName - Could not find MediaBrowserServiceCompat's sendResult method")
		}
	}
}