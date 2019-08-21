package xyz.ismailnurudeen.numberfacts

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface FactDao {
    @Insert
    fun insert(fact: Fact)

    @Delete
    fun delete(fact: Fact): Int

    @Query("SELECT * FROM facts")
    fun allFact(): List<Fact>
}
