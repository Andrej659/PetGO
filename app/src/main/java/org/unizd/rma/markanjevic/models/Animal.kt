package org.unizd.rma.markanjevic.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animals")
data class Animal(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "age") val age: Int,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "dateAdded") val dateAdded: String,
    @ColumnInfo(name = "gender") val gender: String,
)