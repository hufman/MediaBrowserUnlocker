package me.hufman.mediabrowserunlocker.data

import androidx.room.*
import java.util.*

@Dao
interface MediaRootDao {
	@Query("SELECT * FROM mediaRoot")
	fun getAll(): List<MediaRoot>

	@Query("SELECT * FROM mediaRoot WHERE servicePackage = :servicePackage ORDER BY lastAccess desc")
	fun getRootsForService(servicePackage: String): List<MediaRoot>

	@Query("SELECT * FROM mediaRoot WHERE servicePackage = :servicePackage AND clientPackage = :clientPackage ORDER BY lastAccess desc")
	fun getRootsForService(servicePackage: String, clientPackage: String): List<MediaRoot>

	@Query("SELECT * FROM mediaRoot WHERE servicePackage = :servicePackage AND success = 1 ORDER BY lastAccess desc")
	fun getSuccessfulRootsForService(servicePackage: String): List<MediaRoot>

	@Query("SELECT * FROM mediaRoot WHERE servicePackage = :servicePackage AND clientPackage = :clientPackage AND rootId = :rootId ORDER BY lastAccess desc LIMIT 1")
	fun getRootForService(servicePackage: String, clientPackage: String, rootId: String?): MediaRoot?

	@Query("SELECT * FROM mediaRoot WHERE servicePackage = :servicePackage AND rootId = :rootId ORDER BY lastAccess desc LIMIT 1")
	fun getRootForService(servicePackage: String, rootId: String): MediaRoot?

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun insert(vararg mediaRoot: MediaRoot)

	@Update
	fun update(vararg mediaRoot: MediaRoot)

	fun insertNew(servicePackage: String,
	              clientPackage: String,
	              rootId: String?,
	              success: Boolean = false) {
		this.insert(MediaRoot(servicePackage=servicePackage, clientPackage=clientPackage, rootId=rootId, success=success))
	}

	fun upsert(servicePackage: String,
	              clientPackage: String,
	              rootId: String?,
	              success: Boolean = false) {
		val existing = getRootForService(servicePackage, clientPackage, rootId)
		if (existing == null) {
			this.insert(
				MediaRoot(
					servicePackage = servicePackage,
					clientPackage = clientPackage,
					rootId = rootId,
					success = success
				)
			)
		} else {
			existing.success = success
			existing.lastAccess = Date()
			this.update(existing)
		}
	}

	@Query("UPDATE mediaRoot SET success = :success WHERE id = :id")
	fun updateSuccess(id: Int, success: Boolean)
}