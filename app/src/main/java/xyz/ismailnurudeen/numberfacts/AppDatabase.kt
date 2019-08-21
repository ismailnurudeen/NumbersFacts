package xyz.ismailnurudeen.numberfacts

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase


@Database(entities = [(Fact::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getFactDao(): FactDao
}