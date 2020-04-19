package me.hufman.mediabrowserunlocker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import me.hufman.mediabrowserunlocker.data.AppDatabase
import me.hufman.mediabrowserunlocker.logger.MediaAccessLoggerDatabase
import me.hufman.mediabrowserunlocker.policy.MediaAccessPolicyDatabase

class UnlockerService: Service() {
	companion object {
		val TAG = "MediaBrowserUnlocker"
		val ACTION = "me.hufman.mediabrowserunlocker.UnlockerService"
	}
	class IUnlocker(val engine: UnlockerEngine): IUnlockerService.Stub() {
		override fun onGetRoot(
			servicePackage: String,
			clientPackage: String,
			origRoot: String?,
			replacementRoot: String?
		) {
//			Log.d(TAG, "onGetRoot($servicePackage, $clientPackage, $origRoot, $replacementRoot)")
			engine.onGetRoot(servicePackage, clientPackage, origRoot, replacementRoot)
		}

		override fun onLoadChildren(
			servicePackage: String,
			parentId: String,
			results: List<Any?>?
		) {
//			Log.d(TAG, "onLoadChildren($servicePackage, $parentId, $results)")
			engine.onLoadChildren(servicePackage, parentId, results)
		}

		override fun getRoot(
			servicePackage: String,
			clientPackage: String,
			origRoot: String?
		): String? {
//			Log.d(TAG, "getRoot($servicePackage, $clientPackage, $origRoot)")
			return engine.getRoot(servicePackage, clientPackage, origRoot)
		}
	}

	val db by lazy { Room.databaseBuilder(this, AppDatabase::class.java, "mediabrowserunlocker.db").build() }
	val engine by lazy { UnlockerEngineLocal(MediaAccessPolicyDatabase(db.mediaRootDao()), MediaAccessLoggerDatabase(db.mediaRootDao())) }

	override fun onBind(p0: Intent?): IBinder? {
		return IUnlocker(engine)
	}

	override fun onDestroy() {
		super.onDestroy()
		db.close()
	}
}