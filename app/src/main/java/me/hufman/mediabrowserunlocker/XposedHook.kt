package me.hufman.mediabrowserunlocker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.hufman.mediabrowserunlocker.hooks.BrowseResultHook
import me.hufman.mediabrowserunlocker.hooks.GetRootHook
import me.hufman.mediabrowserunlocker.hooks.LoadChildrenHook
import me.hufman.mediabrowserunlocker.logger.MediaAccessLoggerLogcat
import me.hufman.mediabrowserunlocker.policy.MediaAccessPolicyConnectable
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType


class XposedHook : IXposedHookLoadPackage {
	companion object {
		val MEDIA_BROWSER_SERVICE_RESULT = "android.service.media.MediaBrowserService.Result"
		val MEDIA_BROWSER_SERVICE_COMPAT_RESULT = "androidx.media.MediaBrowserServiceCompat.Result"
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

	val mediaAccessPolicy = MediaAccessPolicyConnectable()
	val mediaAccessLogger = MediaAccessLoggerLogcat()
	val browseResultCollation = BrowseResultCollation(mediaAccessLogger)
	val unlockerEngine = UnlockerEngine(browseResultCollation, mediaAccessPolicy, mediaAccessLogger)
	val browseResultHook =
		BrowseResultHook(unlockerEngine)

	override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
		lpparam ?: return

		val serviceClassName = locateMediaBrowserService(lpparam)
		if (serviceClassName != null) {
			// hook the service's onGetRoot functions
			hookMediaBrowserService(lpparam, serviceClassName)

			// hook the Result.sendResult() function
			hookSendResult(lpparam)
		}
	}

	fun getContext(classLoader: ClassLoader): Context {
		val activityThread = XposedHelpers.callStaticMethod(
			XposedHelpers.findClass(
				"android.app.ActivityThread",
				classLoader
			), "currentActivityThread"
		)
		return (XposedHelpers.callMethod(
			activityThread, "getSystemContext"
		) as Context)
	}

	fun locateMediaBrowserService(lpparam: XC_LoadPackage.LoadPackageParam): String? {
		val intent = Intent(MEDIA_BROWSER_SERVICE_INTENT_ACTION)
			.setPackage(lpparam.packageName)
		val context = getContext(lpparam.classLoader)
		val services =
			context.packageManager.queryIntentServices(intent, PackageManager.GET_RESOLVED_FILTER)
		if (services.isNotEmpty()) {
			val service = services.first()
			return service?.serviceInfo?.name
		}
		// no MediaBrowserService found in this application
		return null
	}

	fun hookMediaBrowserService(lpparam: XC_LoadPackage.LoadPackageParam, serviceClassName: String) {

		val serviceClass = try {
			XposedHelpers.findClass(serviceClassName, lpparam.classLoader)
		} catch (e: XposedHelpers.ClassNotFoundError) {
			return
		}
		// find the overridden onGetRoot method, might be obfuscated to a different name
		val rootHook = GetRootHook(
			lpparam.packageName,
			unlockerEngine
		)
		discoverOnGetRoot(serviceClass)?.apply {
			XposedBridge.hookMethod(this, rootHook)
		}

		// find the overridden loadChildren method, might be obfuscated to a different name
		val childrenHook = LoadChildrenHook(
			lpparam.packageName,
			unlockerEngine
		)
		discoverLoadChildren(serviceClass)?.apply {
			XposedBridge.hookMethod(this, childrenHook)
		}
	}

	fun hookSendResult(lpparam: XC_LoadPackage.LoadPackageParam) {
		// the system MediaBrowserService can't be obfuscated, I think
		try {
			val osResult = XposedHelpers.findClass(MEDIA_BROWSER_SERVICE_RESULT, lpparam.classLoader)
			osResult.declaredMethods.filter {
				it.name == "sendResult"
			}.forEach {
				XposedBridge.hookMethod(it, browseResultHook)
			}
		} catch (e: XposedHelpers.ClassNotFoundError) {
			XposedBridge.log("Could not find system MediaBrowserService.Result")
			// might be using the compat package
		}

		try {
			val osResult = XposedHelpers.findClass(MEDIA_BROWSER_SERVICE_COMPAT_RESULT, lpparam.classLoader)
			osResult.declaredMethods.filter {
				it.name == "sendResult"
			}.forEach {
				XposedBridge.hookMethod(it, browseResultHook)
			}
		} catch (e: XposedHelpers.ClassNotFoundError) {
			// might be using the compat package
		}
	}
}