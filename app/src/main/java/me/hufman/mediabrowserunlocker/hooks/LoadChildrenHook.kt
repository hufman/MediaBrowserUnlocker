package me.hufman.mediabrowserunlocker.hooks

import android.service.media.MediaBrowserService
import androidx.media.MediaBrowserServiceCompat
import de.robv.android.xposed.XC_MethodHook
import me.hufman.mediabrowserunlocker.UnlockerEngine


class LoadChildrenHook(val servicePackage: String, val unlockerEngine: UnlockerEngine) :
	XC_MethodHook() {
	override fun beforeHookedMethod(param: MethodHookParam?) {
		val parentId = param?.args?.getOrNull(0) as? String ?: return
		val result = param.args.getOrNull(1) ?: return
		when (result) {
			is MediaBrowserService.Result<*> -> unlockerEngine.beforeOnLoadChildren(
				servicePackage,
				parentId,
				result
			)
			is MediaBrowserServiceCompat.Result<*> -> unlockerEngine.beforeOnLoadChildren(
				servicePackage,
				parentId,
				result
			)
		}
	}
}