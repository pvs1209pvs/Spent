package com.pvs.spent.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.pvs.spent.db.Convertor
import com.pvs.spent.encryption.AES
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.Serializable
import java.time.LocalDate



@Entity(tableName = "expense_table")
data class Expense(
    var ofUser: String = "",
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String = "",
    var amount: Float = 0f,
    var ofCategory: String = "",
    var createdOn: LocalDate = LocalDate.now(),
    @ColumnInfo(defaultValue = "0") var backedUp: Int = 0,
) : Serializable {

    init {
        title = title.trim()
    }

    fun isFromNow(): Boolean {
        val now = LocalDate.now()
        return createdOn.year == now.year && createdOn.monthValue == now.monthValue
    }

    override fun toString(): String {
        return "Expense(id=$id, title='$title', amount=$amount, ofCategory='$ofCategory', " +
                "createdOn='${createdOn.year},${createdOn.monthValue}', backup=$backedUp )"
    }

}