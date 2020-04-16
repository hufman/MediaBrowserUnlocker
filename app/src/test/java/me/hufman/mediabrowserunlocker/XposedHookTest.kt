package me.hufman.mediabrowserunlocker

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import org.junit.Assert.*
import org.junit.Test
import kotlin.reflect.jvm.javaMethod

// Represents the obfuscated MediaBrowserServiceCompat
abstract class ServiceClass {
	abstract fun root(clientPackageName: String, clientUid: Int, rootHints: Bundle)
	abstract fun children(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>)
}
class ObfuscatedSubject: ServiceClass() {
	override fun root(clientPackageName: String, clientUid: Int, rootHints: Bundle) {
	}

	override fun children(
		parentId: String,
		result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>
	) {
	}

	fun private() {}
}

class XposedHookTest {
	@Test
	fun isOverrides() {
		ObfuscatedSubject::class.java.declaredMethods.forEach {
			if (it.name == "private") {
				assertFalse(it.isOverrides())
			} else {
				assertTrue(it.isOverrides())
			}
		}
	}
	@Test
	fun onGetRoot() {
		val function = XposedHook.discoverOnGetRoot(ObfuscatedSubject::class.java)
		assertEquals("root", function?.name)
	}

	@Test
	fun onLoadChildren() {
		val function = XposedHook.discoverLoadChildren(ObfuscatedSubject::class.java)
		assertEquals("children", function?.name)
	}
}