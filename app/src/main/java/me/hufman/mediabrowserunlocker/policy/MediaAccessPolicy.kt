package me.hufman.mediabrowserunlocker.policy

interface MediaAccessPolicy {
	fun getRootAction(servicePackage: String, clientPackage: String, originalRoot: String?): MediaAccessPolicyAction
}