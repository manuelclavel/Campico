package com.mobile.campico

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

import java.util.Date

//To store a Date object in a Kotlin Room database,
//you must use a TypeConverter to convert the Date
//into a known type that Room can persist, such as a Long
//timestamp. Room doesn't inherently know how to store
//non-primitive types like Date.

//Step 1: Create a TypeConverter Class
//Define a class containing @TypeConverter methods that
//convert between Date and Long (Unix timestamp in milliseconds).

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Entity(tableName = "Visit", indices = [Index(
    value = ["date"],
    unique = true
)])
data class Visit(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "date") val date: Date?
)


@Entity(tableName = "Tree", indices = [Index(
    value = ["id"],
    unique = true
)])
data class Tree(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "id") val id: String
)

@Entity(tableName = "Fruit",
    indices = [Index(
    value = ["id"],
    unique = true
), Index(value = ["uidTree"], unique = false)],
    foreignKeys = [
        ForeignKey(
            entity = Tree::class, // Parent entity
            parentColumns = ["uid"], // Column in the parent entity
            childColumns = ["uidTree"], // Column in the child entity (this table)
            onDelete = ForeignKey.CASCADE // Optional: action on parent deletion
        )
    ])
data class Fruit(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "uidTree") val uidTree: Int
)

data class MediaVisit(
   val uid: Int,  val visitUid: Int, val s3key: String
)

@Dao
interface CampicoDao {
    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int

    @Query("SELECT * FROM Visit")
    suspend fun getVisits(): List<Visit>

    @Insert
    suspend fun insertVisits(vararg visit: Visit)

    @Query(
        "SELECT * FROM Visit WHERE uid LIKE :uid LIMIT 1"
    )
    suspend fun findVisitByUid(uid: Int): Visit?

    @Query(
        "SELECT * FROM Visit WHERE date LIKE :date LIMIT 1"
    )
    suspend fun findVisitByDate(date: Date): Visit?

    @Query(
        "UPDATE Visit SET date = :dateNew " +
                "WHERE date = :dateOld "
    )
    suspend fun updateVisit(
        dateOld: Date, dateNew: Date
    )

    @Query(
        "DELETE FROM Visit WHERE date = :date "
    )
    suspend fun deleteVisit(date: Date)

    @Query("SELECT * FROM Tree")
    suspend fun getTrees(): List<Tree>

    @Insert
    suspend fun insertAll(vararg tree: Tree)


    @Query(
        "SELECT * FROM Tree WHERE uid LIKE :uid LIMIT 1"
    )
    suspend fun findTreeByUid(uid: Int): Tree?

    @Query(
        "SELECT * FROM Tree WHERE id LIKE :id LIMIT 1"
    )
    suspend fun findTreeById(id: String): Tree?


    @Query(
        "UPDATE Tree SET id = :idNew " +
                "WHERE id = :idOld "
    )
    suspend fun updateTree(
        idOld: String, idNew: String
    )

    @Query(
        "DELETE FROM Tree WHERE id = :id "
    )
    suspend fun deleteTree(id: String)

    @Query(
        "SELECT * FROM Fruit WHERE id LIKE :id LIMIT 1"
    )
    suspend fun findFruitById(id: String): Fruit?

    @Query("SELECT * FROM Fruit WHERE uidTree = :uidTree")
    suspend fun getFruitsByTreeUid(uidTree : Int): List<Fruit>

    @Query("SELECT COUNT(*) FROM Fruit WHERE uidTree = :uidTree")
    suspend fun getTotalFruitsByTreeUid(uidTree : Int): Int

    @Insert
    suspend fun insertAll(vararg fruit: Fruit)

    @Query(
        "SELECT * FROM Fruit WHERE uid LIKE :uid LIMIT 1"
    )
    suspend fun findFruitByUid(uid: Int): Fruit?

    @Query(
        "DELETE FROM Fruit WHERE id = :id "
    )
    suspend fun deleteFruit(id: String)

    @Query(
        "UPDATE Fruit SET id = :idNew " +
                "WHERE id = :idOld "
    )
    suspend fun updateFruit(
        idOld: String, idNew: String
    )

}
@Database(entities = [Visit::class, Tree::class, Fruit::class],
    autoMigrations = [
        AutoMigration(from = 2, to = 3)],
    version = 3)
@TypeConverters(Converters::class)
abstract class CampicoDatabase : RoomDatabase() {
    abstract fun campicoDao(): CampicoDao


    companion object {
        @Volatile // Ensures visibility to all threads
        private var INSTANCE: CampicoDatabase? = null

        fun getDatabase(context: Context): CampicoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to prevent memory leaks
                    CampicoDatabase::class.java,
                    "CampicoDatabase"
                )//.addMigrations(MIGRATION_2_3)
                    //.createFromAsset("databases/campico.db")
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}



//val MIGRATION_1_2 = object : Migration(1, 3) {
//    override fun migrate(db: SupportSQLiteDatabase) {
// SQL query to create the new table
//        db.execSQL("CREATE TABLE IF NOT EXISTS `Fruit` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uidTree` INTEGER NOT NULL, `id` TEXT, FOREIGN KEY(`uidTree`) REFERENCES `Tree`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE)")
//    }
//}