package me.hufman.mediabrowserunlocker.policy

sealed class MediaAccessPolicyAction {
	class IGNORE : MediaAccessPolicyAction()
	class OVERRIDE_ROOT(val newRoot: String?) : MediaAccessPolicyAction()
	class OVERRIDE_REJECTED_ROOT(val newRoot: String?) : MediaAccessPolicyAction()
}