package wtf.nebula.util.io

import java.awt.AWTException
import java.awt.SystemTray
import java.awt.TrayIcon

object SystemTrayUtil {
    private const val ICON_LOCATION = "/assets/nebula/textures/logo_16px.png"

    var trayIcon: TrayIcon? = null
        private set

    fun createIcon() {
        if (SystemTray.isSupported()) {
            val tray = SystemTray.getSystemTray()
            val image = ResourceUtil.getResourceAsImage(ICON_LOCATION) ?: return

            trayIcon = TrayIcon(image, "Nebula")

            try {
                tray.add(trayIcon)
            } catch (e: AWTException) {
                e.printStackTrace()
            }
        }
    }

    fun remove() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon)
            trayIcon = null
        }
    }

    fun showMessage(message: String?) {
        if (SystemTray.isSupported() && trayIcon != null) {
            trayIcon!!.displayMessage("", message, TrayIcon.MessageType.NONE)
        }
    }

    val isCreated: Boolean
        get() = trayIcon != null
}