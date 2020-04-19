package me.hufman.mediabrowserunlocker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.awaitility.Awaitility.await
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UnlockerServiceClientTest {
	@Test
	fun createClient() {
		val appContext = InstrumentationRegistry.getInstrumentation().targetContext
		val client = UnlockerServiceClient(appContext)
		val disconnectedRoot = client.getRoot("source", "client", "testRoot")
		assertEquals("testRoot", disconnectedRoot)

		await().until { client.service != null }
		val newRoot = client.getRoot("source", "client", "testRoot")
		assertEquals("testRoot", newRoot)
	}
}