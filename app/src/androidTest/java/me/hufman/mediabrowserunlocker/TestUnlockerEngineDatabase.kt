package me.hufman.mediabrowserunlocker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.hufman.mediabrowserunlocker.data.AppDatabase
import me.hufman.mediabrowserunlocker.data.MediaRootDao
import me.hufman.mediabrowserunlocker.logger.MediaAccessLoggerDatabase
import me.hufman.mediabrowserunlocker.policy.MediaAccessPolicyDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestUnlockerEngineDatabase {

	lateinit var db: AppDatabase
	lateinit var dao: MediaRootDao

	@Before
	fun createDb() {
		val context = ApplicationProvider.getApplicationContext<Context>()
		db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
		dao = db.mediaRootDao()
	}

	@After
	fun closeDb() {
		db.close()
	}

	@Test
	fun testEngine() {
		val engine = UnlockerEngineLocal(MediaAccessPolicyDatabase(dao), MediaAccessLoggerDatabase(dao))
		// before we observe a valid root
		val missingSuggestion = engine.getRoot("service", "client", null)
		assertEquals("_ROOT_", missingSuggestion)

		// we see a valid root
		engine.onGetRoot("service", "gearhead", "working", "working")
		engine.onLoadChildren("service", "working", listOf("yes"))

		// now it should be a suggestion
		val workingSuggestion = engine.getRoot("service", "client", null)
		assertEquals("working", workingSuggestion)

	}
}