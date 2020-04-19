// IUnlockerService.aidl
package me.hufman.mediabrowserunlocker;

// Declare any non-default types here with import statements

interface IUnlockerService {
	void onGetRoot(
		String servicePackage,
		String clientPackage,
		@nullable String origRoot,
		@nullable String replacementRoot);

	void onLoadChildren(
		String servicePackage,
		String parentId,
		in @nullable List results);

	@nullable String getRoot(
		String servicePackage,
		String clientPackage,
		@nullable String origRoot);

}
