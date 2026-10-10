package com.eshkerist.budspopup

import android.app.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class PopupService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var window: android.view.View? = null
    private var wm: WindowManager? = null
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_ACL_CONNECTED) {
                val device = if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    else @Suppress("DEPRECATION") (intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice)
                val name = try { device?.name ?: "Bluetooth-устройство" } catch (_: SecurityException) { "Bluetooth-устройство" }
                if (name.contains("Buds", true) || name.contains("Samsung", true) || name.contains("Core", true)) {
                    handler.postDelayed({ showPopup(name, device, false) }, 900)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val filter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED) else @Suppress("DEPRECATION") registerReceiver(receiver, filter)
        val channelId = "buds_popup_service"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "Buds Popup", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channelId) else Notification.Builder(this)
        startForeground(11, notification.setContentTitle("Buds Popup активен").setContentText("Ожидание подключения Galaxy Buds").setSmallIcon(android.R.drawable.stat_sys_data_bluetooth).build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(EXTRA_DEMO, false) == true) handler.post { showPopup("Galaxy Buds Core", null, true) }
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null

    private fun battery(device: BluetoothDevice?): Int? = try {
        if (Build.VERSION.SDK_INT >= 33 && device != null) {
            val method = BluetoothDevice::class.java.getMethod("getBatteryLevel")
            (method.invoke(device) as? Int)?.takeIf { it in 0..100 }
        } else null
    } catch (_: Throwable) { null }

    private fun showPopup(name: String, device: BluetoothDevice?, demo: Boolean) {
        if (!Settings.canDrawOverlays(this)) return
        removePopup()
        val bg = GradientDrawable().apply { setColor(Color.rgb(24, 26, 31)); cornerRadius = dp(28).toFloat(); setStroke(dp(1), Color.rgb(58, 66, 78)) }
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(18), dp(22), dp(16)); background = bg }
        fun text(s: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false) = TextView(this).apply {
            text = s; textSize = size; setTextColor(color); if (bold) typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, dp(4), 0, dp(4))
        }
        card.addView(text("Galaxy Buds Core", 20f, Color.WHITE, true))
        card.addView(text("ᛒ  Подключено · Bluetooth", 13f, Color.rgb(151, 194, 255)))
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, dp(12), 0, dp(8)) }
        val level = battery(device)
        val shown = if (demo) 78 else level
        val status = if (shown != null) "$shown%" else "—"
        listOf("Левый\n$status", "Правый\n$status", "Кейс\n—").forEach { value ->
            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
            col.addView(text("◉", 24f, Color.rgb(127, 203, 176), true)); col.addView(text(value, 13f, Color.LTGRAY)); row.addView(col)
        }
        card.addView(row)
        card.addView(text(if (level == null && !demo) "Заряд недоступен через стандартный Bluetooth API" else "Уровень Bluetooth · кейс отдельно не определён", 11f, Color.GRAY))
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = dp(52); x = dp(12) }
        try {
            wm?.addView(card, params); window = card
            handler.postDelayed({ removePopup() }, 6500)
        } catch (_: Exception) { }
    }
    private fun removePopup() { try { window?.let { wm?.removeView(it) } } catch (_: Exception) {}; window = null }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    override fun onDestroy() { try { unregisterReceiver(receiver) } catch (_: Exception) {}; removePopup(); super.onDestroy() }

    companion object {
        private const val EXTRA_DEMO = "show_demo_popup"
        fun showDemo(context: Context) {
            if (!Settings.canDrawOverlays(context)) return
            val i = Intent(context, PopupService::class.java).putExtra(EXTRA_DEMO, true)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i) else context.startService(i)
        }
    }
}
