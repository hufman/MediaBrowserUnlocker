package me.hufman.mediabrowserunlocker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.DeadObjectException
import android.os.IBinder
import android.util.Log
import java.lang.IllegalArgumentException

class UnlockerServiceClient(val context: Context): UnlockerEngine {
	companion object {
		val TAG = "MediaBrowserUnlClient"
	}

	var service: IUnlockerService? = null
	val connection = object: ServiceConnection {
		override fun onBindingDied(name: ComponentName?) {
			service = null
			disconnect()
			connect()
		}

		override fun onServiceDisconnected(p0: ComponentName?) {
			service = null
		}

		override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
			p1 ?: return
			service = IUnlockerService.Stub.asInterface(p1)
		}
	}

	private inline fun reconnectable(runnable: () -> Unit) {
		try {
			runnable.invoke()
		} catch (e: DeadObjectException) {
			disconnect()
			connect()
		}
	}
	private inline fun <T> reconnectableData(runnable: () -> T): T? {
		try {
			return runnable.invoke()
		} catch (e: DeadObjectException) {
			disconnect()
			connect()
		}
		return null
	}

	init {
		connect()
	}

	private fun connect() {
		val intent = Intent(UnlockerService.ACTION)
			.setPackage("me.hufman.mediabrowserunlocker")
		context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
	}

	override fun onGetRoot(
		servicePackage: String,
		clientPackage: String,
		originalRoot: String?,
		replacementRoot: String?
	) = reconnectable {
		if (service == null) {
			Log.i(TAG, "onGetRoot but not connected yet")
		}
		service?.onGetRoot(servicePackage, clientPackage, originalRoot, replacementRoot)
	}

	override fun onLoadChildren(servicePackage: String, parentId: String, results: List<*>?) = reconnectable {
		if (service == null) {
			Log.i(TAG, "onLoadChildren but not connected yet")
		}
		service?.onLoadChildren(servicePackage, parentId, results)
	}

	override fun getRoot(
		servicePackage: String,
		clientPackage: String,
		originalRoot: String?
	): String? {
		val service = service
		if (service != null) {
			return reconnectableData {
				service.getRoot(servicePackage, clientPackage, originalRoot)
			} ?: originalRoot
		} else {
			return originalRoot
		}
	}

	fun disconnect() {
		try {
			context.unbindService(connection)
		} catch (e: IllegalArgumentException) {
			// apparently not bound anymore
		}
	}
}