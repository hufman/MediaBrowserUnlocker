package me.hufman.mediabrowserunlocker.policy

class MediaAccessPolicyConnectable : MediaAccessPolicy {
	override fun getRootAction(servicePackage: String, clientPackage: String): MediaAccessPolicyAction {
		return MediaAccessPolicyAction.OVERRIDE_REJECTED_ROOT("__ROOT__")
	}
}