package com.eshkerist.budspopup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color

class MainActivity : Activity() {
    private val blue = Color.rgb(125, 185, 255)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 38, 28, 28)
            setBackgroundColor(Color.rgb(17, 18, 21))
        }
        fun label(text: String, size: Float, color: Int = Color.WHITE): TextView = TextView(this).apply {
            this.text = text; textSize = size; setTextColor(color); setPadding(0, 8, 0, 8)
        }
        root.addView(label("Galaxy Buds Core", 27f))
        root.addView(label("One UI-inspired popup · HyperOS 3", 15f, blue))
        root.addView(label("Это экспериментальная версия. Окно срабатывает при Bluetooth-подключении; открытие кейса отдельно может не определяться.", 15f, Color.LTGRAY))
        val permission = Button(this).apply { text = "Разрешить отображение поверх приложений"; setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        } }
        root.addView(permission)
        val start = Button(this).apply { text = "Включить отслеживание"; setOnClickListener {
            requestNeededPermissions()
            if (Settings.canDrawOverlays(this@MainActivity)) {
                val i = Intent(this@MainActivity, PopupService::class.java)
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
            }
        } }
        root.addView(start)
        root.addView(Button(this).apply { text = "Показать тестовое окно"; setOnClickListener { PopupService.showDemo(this@MainActivity) } })
        root.addView(Button(this).apply { text = "Остановить"; setOnClickListener { stopService(Intent(this@MainActivity, PopupService::class.java)) } })
        root.addView(label("Заряд: приложение запрашивает стандартный уровень Bluetooth, если телефон и наушники его предоставляют. Данные левого/правого вкладыша и кейса могут быть недоступны вне Samsung.", 13f, Color.GRAY))
        setContentView(root)
    }
    private fun requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 4)
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 5)
        }
    }
}
