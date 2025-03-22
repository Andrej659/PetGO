package org.unizd.rma.markanjevic.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animals")
data class Animal(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "age") var age: Int,
    @ColumnInfo(name = "image") var image: String,
    @ColumnInfo(name = "dateAdded") var dateAdded: String,
    @ColumnInfo(name = "gender") var gender: String,
)