package com.example.running_app.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(bmp : Bitmap) : ByteArray{
        val outPutSystem = ByteArrayOutputStream()
        //sets the outPut to the value declared up
        bmp.compress(Bitmap.CompressFormat.PNG,100, outPutSystem)
        return outPutSystem.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray) : Bitmap{
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }
}