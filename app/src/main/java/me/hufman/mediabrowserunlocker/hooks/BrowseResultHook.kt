package me.hufman.mediabrowserunlocker.hooks

import android.media.browse.MediaBrowser
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import de.robv.android.xposed.XC_MethodHook
import me.hufman.mediabrowserunlocker.BrowseResultCollation
import me.hufman.mediabrowserunlocker.UnlockerEngineLocal

/** Attaches to the MediaBrowserService.Result.sendResult function call
 * When a MediaBrowserService.Result is called from onLoadChildren
 * post the result up to the Collation service
 */
class BrowseResultHook(val collation: BrowseResultCollation) : XC_MethodHook() {
	override fun afterHookedMethod(param: MethodHookParam?) {
		val firstArg = param?.args?.getOrNull(0)
		if (firstArg is List<*>) {
			val convertedList = when(firstArg) {
				firstArg.getOrNull(0) is MediaBrowser.MediaItem -> MediaBrowserCompat.MediaItem.fromMediaItemList(firstArg)
				firstArg.getOrNull(0) is MediaBrowserCompat.MediaItem -> firstArg as List<MediaBrowserCompat.MediaItem>
				firstArg.isEmpty() -> firstArg as List<MediaBrowserCompat.MediaItem>
				else -> null
			}
			val thisObject = param.thisObject
			when (thisObject) {
				is MediaBrowserService.Result<*> -> collation.completeResult(
					thisObject,
					convertedList
				)
				is MediaBrowserServiceCompat.Result<*> -> collation.completeResult(
					thisObject,
					convertedList
				)
			}
		}
	}
}