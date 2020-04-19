package me.hufman.mediabrowserunlocker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities=[MediaRoot::class], version=1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
	abstract fun mediaRootDao(): MediaRootDao
}