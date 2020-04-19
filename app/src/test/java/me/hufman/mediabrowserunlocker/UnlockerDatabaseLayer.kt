package me.hufman.mediabrowserunlocker

import com.nhaarman.mockito_kotlin.*
import me.hufman.mediabrowserunlocker.data.MediaRoot
import me.hufman.mediabrowserunlocker.data.MediaRootDao
import me.hufman.mediabrowserunlocker.logger.MediaAccessLoggerDatabase
import me.hufman.mediabrowserunlocker.policy.MediaAccessPolicyAction
import me.hufman.mediabrowserunlocker.policy.MediaAccessPolicyDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnlockerDatabaseLayer {
	val dao = mock<MediaRootDao>()

	@Test
	fun testLogger() {
		val logger = MediaAccessLoggerDatabase(dao)
		logger.onGetRoot("service", "client", null, null)
		verify(dao).upsert("service", "client", null, false)
	}

	@Test
	fun testPolicy() {
		val policy = MediaAccessPolicyDatabase(dao)
		val action = policy.getRootAction("service", "client", null)
		assertTrue(action is MediaAccessPolicyAction.OVERRIDE_REJECTED_ROOT)
		assertEquals("_ROOT_", (action as MediaAccessPolicyAction.OVERRIDE_REJECTED_ROOT).newRoot)

		// test with a working root
		whenever(dao.getRootsForService(any())).doAnswer {
			listOf(MediaRoot("service", "client", "workingRoot", true))
		}
		val newAction = policy.getRootAction("service", "client", null)
		assertTrue(newAction is MediaAccessPolicyAction.OVERRIDE_ROOT)
		assertEquals("workingRoot", (newAction as MediaAccessPolicyAction.OVERRIDE_ROOT).newRoot)
	}
}