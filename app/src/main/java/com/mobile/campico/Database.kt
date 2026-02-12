package com.mobile.campico

import android.content.Context
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
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

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
)],
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

@Dao
interface CampicoDao {
    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int

    @Query("SELECT * FROM Tree")
    suspend fun getTrees(): List<Tree>

    @Insert
    suspend fun insertAll(vararg tree: Tree)


    @Query(
        "SELECT * FROM Tree WHERE uid LIKE :uid LIMIT 1"
    )
    suspend fun findTreeByUid(uid: Int): Tree?

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
@Database(entities = [Tree::class, Fruit::class], version = 2)
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
                )
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