package me.hufman.mediabrowserunlocker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.hufman.mediabrowserunlocker.hooks.MediaServiceHook
import me.hufman.mediabrowserunlocker.hooks.MediaServiceHook.Companion.MEDIA_BROWSER_SERVICE_INTENT_ACTION
import java.lang.NullPointerException


class XposedHook : IXposedHookLoadPackage {
	override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
		lpparam ?: return

		try {
			val serviceClassName = locateMediaBrowserService(lpparam)
			if (serviceClassName != null) {
				// hook the service's functions
				hookMediaBrowserService(lpparam, serviceClassName)
			}
		} catch (e: NullPointerException) {}
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
		val packageName = lpparam.packageName
		val mediaServiceHook = MediaServiceHook(packageName, serviceClass)
		XposedBridge.hookMethod(serviceClass.getMethod("onCreate"), mediaServiceHook)
		XposedBridge.hookMethod(serviceClass.getMethod("onDestroy"), mediaServiceHook)
		XposedBridge.log("MediaBrowserUnlocker: Hooked $packageName")
	}

}