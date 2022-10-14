package com.pvs.spent.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pvs.spent.dao.CategoryDAO
import com.pvs.spent.dao.CategoryIconDAO
import com.pvs.spent.dao.ExpenseDAO
import com.pvs.spent.data.Category
import com.pvs.spent.data.CategoryIcon
import com.pvs.spent.data.Expense

@Database(
    entities = [Category::class, Expense::class, CategoryIcon::class],
    version = 10,
    exportSchema = true
)
@TypeConverters(Convertor::class)
abstract class LocalDB : RoomDatabase() {

    abstract fun categoryDAO(): CategoryDAO
    abstract fun expenseDAO(): ExpenseDAO
    abstract fun categoryIconDAO(): CategoryIconDAO

    companion object {

        @Volatile
        private var INSTANCE: LocalDB? = null

        fun getDatabase(context: Context): LocalDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDB::class.java,
                    "local_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                    .addMigrations(MIGRATION_6_7)
                    .addMigrations(MIGRATION_7_6)
                    .addMigrations(MIGRATION_6_7_1)
                    .addMigrations(MIGRATION_7_8)
                    .addMigrations(MIGRATION_8_9)
                    .addMigrations(MIGRATION_9_10)
//                    .addMigrations(MIGRATION_10_11)
                    .build()

                INSTANCE = instance
                return instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS expense_table (id integer primary key autoincrement not null, title text not null, amount real not null, ofCategory text not null )"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS category_table")
                database.execSQL("CREATE TABLE IF NOT EXISTS category_table (title text primary key not null, total float not null, budget float not null)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS expense_table")
                database.execSQL("CREATE TABLE IF NOT EXISTS expense_table (id integer primary key autoincrement not null, title text not null, amount real not null, ofCategory text not null, createdOn text not null)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS category_table")
                database.execSQL("CREATE TABLE IF NOT EXISTS category_table (title text primary key not null, total float not null, budget float not null, icon int not null)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS category_icon_table (title text primary key not null, icon int not null)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS registered_user (email text primary key not null)")
            }
        }

        private val MIGRATION_7_6 = object : Migration(7, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS registered_user")
            }
        }


        private val MIGRATION_6_7_1 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS category_table")
                database.execSQL("CREATE TABLE IF NOT EXISTS category_table (ofUser text not null, title text not null, total float not null, budget float not null, icon int not null, primary key(ofUser, title))")

                database.execSQL("DROP TABLE IF EXISTS expense_table")
                database.execSQL("CREATE TABLE IF NOT EXISTS expense_table (ofUser text not null, id integer primary key autoincrement not null, title text not null, amount real not null, ofCategory text not null, createdOn text not null)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS registered_user (email text primary key not null, firstName text not null, lastName text not null)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS registered_user")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS expense_table")
                database.execSQL("CREATE TABLE IF NOT EXISTS expense_table (ofUser text not null, id integer primary key autoincrement not null, title text not null, amount real not null, ofCategory text not null, createdOn text not null, backedUp integer not null default 0)")
            }
        }

//        private val MIGRATION_10_11 = object : Migration(10, 11) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("DROP TABLE IF EXISTS expense_table")
//                database.execSQL("CREATE TABLE IF NOT EXISTS expense_table (ofUser text not null, id integer primary key autoincrement not null, title text not null, amount real not null, ofCategory text not null, createdOn text not null, backedUp integer not null default 0, aesIv text not null)")
//            }
//        }

    }

}