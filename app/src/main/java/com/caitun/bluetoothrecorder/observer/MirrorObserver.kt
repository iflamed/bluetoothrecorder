package com.caitun.bluetoothrecorder.observer

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.core.net.toUri


class MirrorObserver(private val context: Context, private val handler: Handler) : ContentObserver(handler) {
    private val tag = "MirrorObserver"

    companion object {
        private const val AUTHORITY = "com.caitun.mirror.beautylauncher.provider"
        private const val PATH_CONTROL_DATA = "data"

        val contentUri: Uri = "content://$AUTHORITY/$PATH_CONTROL_DATA".toUri()
    }

    override fun onChange(selfChange: Boolean) {
        val cr = context.contentResolver
        val c = cr.query(
            contentUri, null, "0", null, "date desc"
        )
        val sb = StringBuilder()
        while (c!!.moveToNext()) {
            //灯
            val lightValue = c.getFloat(c.getColumnIndexOrThrow("light_value"))
            //色温
            val colorTempValue = c.getFloat(c.getColumnIndexOrThrow("color_temp_value"))
            //亮度 表示是否已经读
            val brightnessValue = c.getFloat(c.getColumnIndexOrThrow("brightness_value"))
            Log.d(tag, "onChange: 灯:$lightValue 色温: $colorTempValue 亮度: $brightnessValue")
            sb.append("灯:$lightValue 色温: $colorTempValue 亮度: $brightnessValue")
        }
        c.close()
        handler.obtainMessage(100, sb.toString()).sendToTarget()
    }
}