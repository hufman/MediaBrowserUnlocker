package me.hufman.mediabrowserunlocker

import me.hufman.mediabrowserunlocker.logger.MediaAccessLogger
import me.hufman.mediabrowserunlocker.policy.MediaAccessPolicy
import me.hufman.mediabrowserunlocker.policy.MediaAccessPolicyAction

interface UnlockerEngine: MediaAccessLogger {
	override fun onGetRoot(servicePackage: String, clientPackage: String, originalRoot: String?, replacementRoot: String?)
	override fun onLoadChildren(servicePackage: String, parentId: String, results: List<*>?)
	fun getRoot(servicePackage: String, clientPackage: String, originalRoot: String?): String?
}

class UnlockerEngineLocal(
	val policy: MediaAccessPolicy,
	val logger: MediaAccessLogger
) : UnlockerEngine {
	override fun onGetRoot(servicePackage: String, clientPackage: String, originalRoot: String?, replacementRoot: String?) {
		logger.onGetRoot(servicePackage, clientPackage, originalRoot, replacementRoot)
	}

	override fun onLoadChildren(servicePackage: String, parentId: String, results: List<*>?) {
		logger.onLoadChildren(servicePackage, parentId, results)
	}

	override fun getRoot(servicePackage: String, clientPackage: String, originalRoot: String?): String? {
		val action = policy.getRootAction(servicePackage, clientPackage, originalRoot)
		val newRoot = when (action) {
			is MediaAccessPolicyAction.OVERRIDE_ROOT -> action.newRoot
			is MediaAccessPolicyAction.OVERRIDE_REJECTED_ROOT -> originalRoot ?: action.newRoot
			else -> originalRoot
		}
		logger.onGetRoot(servicePackage, clientPackage, originalRoot, newRoot)
		return newRoot
	}

}