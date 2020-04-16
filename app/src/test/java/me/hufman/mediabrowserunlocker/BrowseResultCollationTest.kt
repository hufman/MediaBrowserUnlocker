package me.hufman.mediabrowserunlocker

import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import me.hufman.mediabrowserunlocker.logger.MediaAccessLogger
import org.junit.Assert.*
import org.junit.Test

class BrowseResultCollationTest {
	val logger = mock<MediaAccessLogger>()
	@Test
	fun completeResult() {
		val result = mock<MediaBrowserService.Result<*>>()
		val value = listOf(mock<MediaBrowserCompat.MediaItem>())
		val subject = BrowseResultCollation(logger)
		subject.startResult(result, "packageName", "parentId")
		verifyNoMoreInteractions(logger)
		subject.completeResult(result, value)
		verify(logger).onLoadChildren("packageName", "parentId", value)
	}
}