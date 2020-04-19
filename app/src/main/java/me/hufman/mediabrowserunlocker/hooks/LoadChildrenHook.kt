package me.hufman.mediabrowserunlocker.hooks

import android.service.media.MediaBrowserService
import androidx.media.MediaBrowserServiceCompat
import de.robv.android.xposed.XC_MethodHook
import me.hufman.mediabrowserunlocker.BrowseResultCollation
import me.hufman.mediabrowserunlocker.UnlockerEngineLocal


class LoadChildrenHook(val servicePackage: String, val collation: BrowseResultCollation) :
	XC_MethodHook() {
	override fun beforeHookedMethod(param: MethodHookParam?) {
		val parentId = param?.args?.getOrNull(0) as? String ?: return
		val result = param.args.getOrNull(1) ?: return
		when (result) {
			is MediaBrowserService.Result<*> -> collation.startResult(
				servicePackage,
				parentId,
				result
			)
			is MediaBrowserServiceCompat.Result<*> -> collation.startResult(
				servicePackage,
				parentId,
				result
			)
		}
	}
}