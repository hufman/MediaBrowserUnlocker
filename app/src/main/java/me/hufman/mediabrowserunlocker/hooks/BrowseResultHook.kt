package me.hufman.mediabrowserunlocker.hooks

import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import de.robv.android.xposed.XC_MethodHook
import me.hufman.mediabrowserunlocker.UnlockerEngine

/** Attaches to the MediaBrowserService.Result.sendResult function call
 * When a MediaBrowserService.Result is called from onLoadChildren
 * post the result up to the Collation service
 */
class BrowseResultHook(val unlockerEngine: UnlockerEngine) : XC_MethodHook() {
	override fun afterHookedMethod(param: MethodHookParam?) {
		if (param?.method?.name == "sendResult") {
			val firstArg = param.args.getOrNull(0)
			if (firstArg is List<*>) {
				val convertedList = MediaBrowserCompat.MediaItem.fromMediaItemList(firstArg)
				val thisObject = param.thisObject
				when (thisObject) {
					is MediaBrowserService.Result<*> -> unlockerEngine.onCompleteLoadChildren(
						thisObject,
						convertedList
					)
					is MediaBrowserServiceCompat.Result<*> -> unlockerEngine.onCompleteLoadChildren(
						thisObject,
						convertedList
					)
				}
			}
		}
	}
}