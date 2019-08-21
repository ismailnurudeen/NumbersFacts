package xyz.ismailnurudeen.numberfacts

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "facts")
data class Fact(
        @ColumnInfo(name = "text") val text: String,
        @ColumnInfo(name = "number") val number: String,
        @ColumnInfo(name = "found") val found: Boolean = true,
        @ColumnInfo(name = "type") val type: String,
        @PrimaryKey val uid: String = UUID.randomUUID().toString()

)
