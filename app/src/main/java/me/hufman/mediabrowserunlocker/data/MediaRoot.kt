package me.hufman.mediabrowserunlocker.data


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(indices=[Index(value=["servicePackage", "clientPackage", "rootId"], unique=true)])
data class MediaRoot(
	val servicePackage: String,
	val clientPackage: String,
	val rootId: String?,
	var success: Boolean
) {
	@PrimaryKey(autoGenerate = true) var id: Int = 0
	var lastAccess = Date()
}