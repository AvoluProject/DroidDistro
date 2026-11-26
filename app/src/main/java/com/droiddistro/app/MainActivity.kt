package com.droiddistro.app

import android.app.Activity
import android.app.AlertDialog
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : Activity() {
    private val bg = Color.rgb(8, 12, 18)
    private val panel = Color.rgb(17, 24, 34)
    private val panelAlt = Color.rgb(22, 32, 44)
    private val accent = Color.rgb(111, 240, 174)
    private val textMain = Color.rgb(238, 244, 248)
    private val textMuted = Color.rgb(151, 166, 180)
    private val prefs by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    private var page = 0
    private var distro = "Debian"
    private var desktop = "XFCE"
    private var displayMode = DISPLAY_X11
    private var profileName = "My Linux"
    private var installState = "NOT INSTALLED"
    private var lastMessage = "No activity yet"
    private var logText = "DroidDistro ready.\n"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0
        loadPreferences()
        page = if (prefs.getBoolean("configured", false)) PAGE_DASHBOARD else PAGE_WELCOME
        render()
    }

    override fun onResume() {
        super.onResume()
        loadPreferences()
        if (page == PAGE_DASHBOARD || page == PAGE_LOGS || page == PAGE_BOOT) render()
    }

    private fun loadPreferences() {
        distro = prefs.getString("distro", "Debian") ?: "Debian"
        desktop = prefs.getString("desktop", "XFCE") ?: "XFCE"
        displayMode = prefs.getString("displayMode", DISPLAY_X11) ?: DISPLAY_X11
        profileName = prefs.getString("profileName", "My Linux") ?: "My Linux"
        installState = prefs.getString("installState", if (prefs.getBoolean("installed", false)) "READY" else "NOT INSTALLED") ?: "NOT INSTALLED"
        if (prefs.getBoolean("installed", false) && !isCurrentEnvironmentInstalled()) {
            installState = "PROFILE CHANGED — REINSTALL"
        }
        lastMessage = prefs.getString("lastMessage", "No activity yet") ?: "No activity yet"
        logText = prefs.getString("logText", "DroidDistro ready.\n") ?: "DroidDistro ready.\n"
    }

    private fun render() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(30))
        }
        scroll.addView(body)
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(outer)

        addText(body, "D R O I D D I S T R O", 13, accent, true)
        addText(body, pageTitle(), 27, textMain, true, top = 8)
        addText(body, "LINUX ENVIRONMENT BUILDER  •  v0.2.0", 10, textMuted, true, top = 5)
        addSpacer(body, 20)

        when (page) {
            PAGE_WELCOME -> welcomePage(body)
            PAGE_DEVICE -> devicePage(body)
            PAGE_DISTRO -> distroPage(body)
            PAGE_DESKTOP -> desktopPage(body)
            PAGE_DISPLAY -> displayPage(body)
            PAGE_REVIEW -> reviewPage(body)
            PAGE_DASHBOARD -> dashboardPage(body)
            PAGE_BOOT -> bootPage(body)
            PAGE_LOGS -> logsPage(body)
        }

        addSpacer(body, 22)
        addText(body, "PRoot uses the Android kernel. This app creates a Linux userland; it does not replace Android or perform a native dual boot.", 11, textMuted, false)
    }

    private fun pageTitle(): String = when (page) {
        PAGE_WELCOME -> "Your Linux. Your rules."
        PAGE_DEVICE -> "Device inspection"
        PAGE_DISTRO -> "Choose a distribution"
        PAGE_DESKTOP -> "Choose your desktop"
        PAGE_DISPLAY -> "Display configuration"
        PAGE_REVIEW -> "Review configuration"
        PAGE_DASHBOARD -> "Environment manager"
        PAGE_BOOT -> "Boot console"
        else -> "Activity log"
    }

    private fun welcomePage(body: LinearLayout) {
        card(body) {
            addText(this, "A pocket-sized Linux lab", 22, textMain, true)
            addText(this, "Create a Linux userland, add a desktop, and start it from a guided Android interface. No manual Linux commands are needed for the normal setup flow.", 14, textMuted, false, top = 10)
            addSpacer(this, 16)
            pill(this, "ANDROID 10+  •  TERMUX + PRoot")
        }
        addSpacer(body, 12)
        action(body, "START GUIDED SETUP", true) { page = PAGE_DEVICE; render() }
        addSpacer(body, 8)
        action(body, "OPEN ENVIRONMENT MANAGER", false) { page = PAGE_DASHBOARD; render() }
        addText(body, "Real distro installation is available through Termux RUN_COMMAND after the required Termux permission and external-app setting are enabled.", 12, textMuted, false, top = 16)
    }

    private fun devicePage(body: LinearLayout) {
        card(body) {
            addText(this, "DEVICE REPORT", 11, accent, true)
            addSpacer(this, 8)
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memory = ActivityManager.MemoryInfo()
            manager.getMemoryInfo(memory)
            val storage = StatFs(Environment.getDataDirectory().path)
            val totalStorage = storage.blockCountLong * storage.blockSizeLong
            val freeStorage = storage.availableBlocksLong * storage.blockSizeLong
            infoRow(this, "Device", "${Build.MANUFACTURER} ${Build.MODEL}")
            infoRow(this, "Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            infoRow(this, "CPU ABI", Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown")
            infoRow(this, "Total RAM", formatBytes(memory.totalMem))
            infoRow(this, "Available RAM", formatBytes(memory.availMem))
            infoRow(this, "Free storage", formatBytes(freeStorage))
            infoRow(this, "Total storage", formatBytes(totalStorage))
            infoRow(this, "Termux", if (isPackageInstalled(PKG_TERMUX)) "Detected" else "Not detected")
            infoRow(this, "Termux:X11", if (isPackageInstalled(PKG_X11)) "Detected" else "Not detected")
        }
        addText(body, "For phones with limited RAM, XFCE or Openbox is a better first choice than a heavyweight desktop. Installation also needs free storage for the Linux root filesystem and packages.", 12, textMuted, false, top = 14)
        footer(body, "BACK", { page = PAGE_WELCOME; render() }, "CONTINUE", { page = PAGE_DISTRO; render() })
    }

    private fun distroPage(body: LinearLayout) {
        addText(body, "Choose the Linux userland to install using PRoot-Distro.", 14, textMuted, false)
        option(body, "Debian", "Stable base • recommended first environment", distro == "Debian") { distro = "Debian"; render() }
        option(body, "Ubuntu", "General-purpose environment", distro == "Ubuntu") { distro = "Ubuntu"; render() }
        option(body, "Alpine Linux", "Small and minimalist", distro == "Alpine Linux") { distro = "Alpine Linux"; render() }
        option(body, "Arch Linux", "Rolling-release environment", distro == "Arch Linux") { distro = "Arch Linux"; render() }
        footer(body, "BACK", { page = PAGE_DEVICE; render() }, "CONTINUE", { page = PAGE_DESKTOP; render() })
    }

    private fun desktopPage(body: LinearLayout) {
        addText(body, "Choose the graphical desktop or a terminal-only environment.", 14, textMuted, false)
        option(body, "XFCE", "Balanced desktop • recommended for the first run", desktop == "XFCE") { desktop = "XFCE"; render() }
        option(body, "LXQt", "Lightweight Qt desktop", desktop == "LXQt") { desktop = "LXQt"; render() }
        option(body, "LXDE", "Classic lightweight desktop", desktop == "LXDE") { desktop = "LXDE"; render() }
        option(body, "MATE", "Traditional full desktop", desktop == "MATE") { desktop = "MATE"; render() }
        option(body, "Openbox", "Minimal window manager", desktop == "Openbox") { desktop = "Openbox"; render() }
        option(body, "Terminal only", "No graphical desktop packages", desktop == "Terminal only") { desktop = "Terminal only"; render() }
        footer(body, "BACK", { page = PAGE_DISTRO; render() }, "CONTINUE", { page = PAGE_DISPLAY; render() })
    }

    private fun displayPage(body: LinearLayout) {
        addText(body, "Choose how to display the Linux session.", 14, textMuted, false)
        option(body, "Termux:X11", "Available backend • opens the separate X11 app", displayMode == DISPLAY_X11) { displayMode = DISPLAY_X11; render() }
        option(body, "Terminal only", "Run the Linux shell in a Termux session", displayMode == DISPLAY_TERMINAL) { displayMode = DISPLAY_TERMINAL; render() }
        option(body, "Built-in VNC display", "Planned • embedded viewer is not included in this build", displayMode == DISPLAY_BUILTIN) { displayMode = DISPLAY_BUILTIN; render() }
        card(body) {
            addText(this, "REQUIREMENTS FOR TERMUX:X11", 11, accent, true)
            addText(this, "Install both the Termux:X11 Android app and its Termux companion package. The setup flow can install the companion package, but you must install the Android app separately.", 12, textMuted, false, top = 8)
            addSpacer(this, 8)
            action(this, "OPEN OFFICIAL TERMUX:X11 RELEASES", false) { openUrl(URL_X11) }
        }
        footer(body, "BACK", { page = PAGE_DESKTOP; render() }, "REVIEW", { page = PAGE_REVIEW; render() })
    }

    private fun reviewPage(body: LinearLayout) {
        val nameField = EditText(this).apply {
            setText(profileName)
            setTextColor(textMain)
            setHintTextColor(textMuted)
            hint = "Profile name"
            textSize = 15f
            setSingleLine(true)
            setPadding(dp(14), dp(13), dp(14), dp(13))
            background = rounded(panelAlt, 12, Color.rgb(54, 70, 87))
        }
        addText(body, "PROFILE NAME", 11, accent, true)
        body.addView(nameField, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(8); bottomMargin = dp(14) })
        card(body) {
            addText(this, "INSTALLATION PLAN", 11, accent, true)
            addSpacer(this, 8)
            infoRow(this, "Profile", nameField.text.toString().ifBlank { "My Linux" })
            infoRow(this, "Distribution", distro)
            infoRow(this, "Desktop", desktop)
            infoRow(this, "Display", displayLabel(displayMode))
            infoRow(this, "Engine", "PRoot-Distro")
            infoRow(this, "Host", "Termux")
        }
        addText(body, "Saving this profile does not download Linux yet. The INSTALL action on the dashboard performs the actual installation through Termux.", 12, textMuted, false, top = 12)
        addSpacer(body, 14)
        action(body, "SAVE PROFILE & FINISH SETUP", true) {
            profileName = nameField.text.toString().trim().ifBlank { "My Linux" }
            prefs.edit()
                .putString("profileName", profileName)
                .putString("distro", distro)
                .putString("desktop", desktop)
                .putString("displayMode", displayMode)
                .putBoolean("configured", true)
                .apply()
            page = PAGE_DASHBOARD
            render()
        }
        addSpacer(body, 8)
        action(body, "BACK", false) { page = PAGE_DISPLAY; render() }
    }

    private fun dashboardPage(body: LinearLayout) {
        card(body) {
            addText(this, "${profileName.uppercase(Locale.US)}  •  DEFAULT PROFILE", 10, accent, true)
            addText(this, distro, 27, textMain, true, top = 8)
            addText(this, "$desktop  /  ${displayLabel(displayMode)}", 13, textMuted, false, top = 4)
            addSpacer(this, 12)
            val visibleState = if (prefs.getBoolean("installed", false) && !isCurrentEnvironmentInstalled()) "PROFILE CHANGED — REINSTALL" else installState
            pill(this, visibleState)
            addText(this, lastMessage, 12, textMuted, false, top = 10)
        }
        addSpacer(body, 8)
        if (isCurrentEnvironmentInstalled()) {
            action(body, if (desktop == "Terminal only" || displayMode == DISPLAY_TERMINAL) "START LINUX SHELL" else "POWER ON LINUX DESKTOP", true) { startLinux() }
            addSpacer(body, 8)
            action(body, "REINSTALL / REPAIR ENVIRONMENT", false) { confirmInstall(reinstall = true) }
            addSpacer(body, 8)
            action(body, "STOP RUNNING SESSION", false) { stopLinux() }
            addSpacer(body, 8)
            action(body, "REMOVE LINUX ENVIRONMENT", false) { confirmRemove() }
        } else {
            action(body, "INSTALL LINUX ENVIRONMENT", true) { confirmInstall(reinstall = false) }
            addSpacer(body, 8)
            action(body, "CHECK TERMUX CONNECTION", false) { checkTermuxSetup() }
        }
        addSpacer(body, 8)
        action(body, "BOOT CONSOLE", false) { page = PAGE_BOOT; render() }
        addSpacer(body, 8)
        action(body, "FULL ACTIVITY LOG", false) { page = PAGE_LOGS; render() }
        addSpacer(body, 8)
        action(body, "CONFIGURE PROFILE", false) { page = PAGE_DEVICE; render() }
        addText(body, "The app currently installs and launches real PRoot environments through Termux. Termux:X11 is an external display app; the embedded VNC screen remains a future feature.", 12, textMuted, false, top = 16)
    }

    private fun bootPage(body: LinearLayout) {
        card(body) {
            addText(this, "DROID DISTRO // SYSTEM CONSOLE", 11, accent, true)
            addSpacer(this, 12)
            addText(this, logText, 12, Color.rgb(170, 238, 190), false, mono = true)
        }
        action(body, "OPEN FULL ACTIVITY LOG", true) { page = PAGE_LOGS; render() }
        addSpacer(body, 8)
        action(body, "BACK TO MANAGER", false) { page = PAGE_DASHBOARD; render() }
    }

    private fun logsPage(body: LinearLayout) {
        card(body) {
            addText(this, "LATEST EVENTS", 11, accent, true)
            addSpacer(this, 10)
            addText(this, logText.ifBlank { "No events recorded yet." }, 12, Color.rgb(170, 238, 190), false, mono = true)
        }
        action(body, "REFRESH LOG", true) { loadPreferences(); render() }
        addSpacer(body, 8)
        action(body, "BACK TO MANAGER", false) { page = PAGE_DASHBOARD; render() }
    }

    private fun confirmInstall(reinstall: Boolean) {
        if (displayMode == DISPLAY_BUILTIN && desktop != "Terminal only") {
            AlertDialog.Builder(this)
                .setTitle("Built-in display is not ready")
                .setMessage("The embedded VNC viewer is not included in v0.2.0. Choose Termux:X11 for a working graphical desktop, or Terminal only for a shell session.")
                .setPositiveButton("CHOOSE DISPLAY") { _, _ -> page = PAGE_DISPLAY; render() }
                .setNegativeButton("CANCEL", null)
                .show()
            return
        }
        if (!isPackageInstalled(PKG_TERMUX)) {
            AlertDialog.Builder(this)
                .setTitle("Termux is required")
                .setMessage("Install Termux first, open it once, and complete its initial package setup. DroidDistro uses Termux as the Linux host environment.")
                .setPositiveButton("OPEN TERMUX DOWNLOAD") { _, _ -> openUrl(URL_TERMUX) }
                .setNegativeButton("CANCEL", null)
                .show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(if (reinstall) "Repair environment?" else "Install $distro?")
            .setMessage("DroidDistro will ask Termux to update packages, install PRoot-Distro, download the $distro root filesystem, and install ${desktop} packages if selected. This can use several gigabytes of data and storage. Keep the phone charged and use a stable connection.")
            .setPositiveButton(if (reinstall) "CONTINUE" else "INSTALL") { _, _ -> launchInstaller() }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun launchInstaller() {
        val token = getOrCreateToken()
        val script = buildInstallScript(token)
        if (!sendToTermux(script, "DroidDistro Linux installer")) return
        prefs.edit().putString("installState", "INSTALLING").putString("lastMessage", "Installer started in Termux").apply()
        addLocalLog("INSTALL", "Install request sent to Termux")
        loadPreferences()
        page = PAGE_LOGS
        render()
    }

    private fun buildInstallScript(token: String): String {
        val distroId = distroId(distro)
        val guestInstall = guestPackageCommand(distro, desktop)
        val x11Setup = if (displayMode == DISPLAY_X11 && desktop != "Terminal only") {
            "pkg install -y x11-repo && pkg install -y termux-x11-nightly"
        } else ""
        val guestCommand = if (guestInstall.isBlank()) "" else "proot-distro login \"\$DISTRO_ID\" -- bash -lc ${shellQuote(guestInstall)}"
        return """
            #!/data/data/com.termux/files/usr/bin/bash
            set -Eeuo pipefail
            TOKEN=${shellQuote(token)}
            DISTRO_ID=${shellQuote(distroId)}
            APP_ID=${shellQuote(applicationContext.packageName)}
            DESKTOP_NAME=${shellQuote(desktop)}
            notify_app() {
              /system/bin/am broadcast --user 0 -n "${'$'}APP_ID/.TermuxEventReceiver" -a "${'$'}APP_ID.TERMUX_EVENT" --es token "${'$'}TOKEN" --es state "${'$'}1" --es message "${'$'}2" --es distro "${'$'}DISTRO_ID" --es desktop "${'$'}DESKTOP_NAME" >/dev/null 2>&1 || true
            }
            on_error() {
              code=${'$'}?
              notify_app ERROR "Installation failed (exit ${'$'}code). Check the Termux session for details."
              exit ${'$'}code
            }
            trap on_error ERR
            notify_app INSTALLING "Updating Termux package lists"
            pkg update -y
            notify_app INSTALLING "Installing PRoot-Distro"
            pkg install -y proot-distro
            if proot-distro list --quiet 2>/dev/null | grep -Fxq "${'$'}DISTRO_ID"; then
              notify_app INSTALLING "Existing ${'$'}DISTRO_ID environment found"
            else
              notify_app INSTALLING "Downloading ${'$'}DISTRO_ID root filesystem"
              proot-distro install "${'$'}DISTRO_ID"
            fi
            if [ -n ${shellQuote(guestInstall)} ]; then
              notify_app INSTALLING "Installing ${desktop} desktop packages"
              $guestCommand
            fi
            if [ -n ${shellQuote(x11Setup)} ]; then
              notify_app INSTALLING "Installing Termux X11 companion package"
              $x11Setup
            fi
            mkdir -p "${'$'}HOME/.droiddistro"
            cat > "${'$'}HOME/.droiddistro/start-${distroId}.sh" <<'DROIDSTART'
            #!/data/data/com.termux/files/usr/bin/bash
            set -e
            termux-x11 :1 -legacy-drawing >/dev/null 2>&1 &
            sleep 2
            proot-distro login ${distroId} --shared-tmp -- bash -lc ${shellQuote(guestStartCommand(desktop))}
            DROIDSTART
            chmod +x "${'$'}HOME/.droiddistro/start-${distroId}.sh"
            notify_app READY "$distro environment installed successfully"
            echo
            echo "DroidDistro: installation completed. You can return to the app."
        """.trimIndent()
    }

    private fun startLinux() {
        if (!isCurrentEnvironmentInstalled()) {
            toast("Linux environment is not installed yet")
            return
        }
        if (displayMode == DISPLAY_X11 && desktop != "Terminal only" && !isPackageInstalled(PKG_X11)) {
            AlertDialog.Builder(this)
                .setTitle("Termux:X11 app not found")
                .setMessage("Install the Termux:X11 Android APK first. The Termux companion package is installed by DroidDistro, but the Android app must be installed separately.")
                .setPositiveButton("OPEN OFFICIAL RELEASES") { _, _ -> openUrl(URL_X11) }
                .setNegativeButton("CANCEL", null)
                .show()
            return
        }
        if (displayMode == DISPLAY_BUILTIN && desktop != "Terminal only") {
            AlertDialog.Builder(this)
                .setTitle("Built-in display is not ready")
                .setMessage("The embedded VNC viewer is not part of this build yet. Choose Termux:X11 in Configure Profile to use a working graphical backend now.")
                .setPositiveButton("CONFIGURE") { _, _ -> page = PAGE_DISPLAY; render() }
                .setNegativeButton("OK", null)
                .show()
            return
        }
        val token = getOrCreateToken()
        val distroId = distroId(distro)
        val script = if (desktop == "Terminal only" || displayMode == DISPLAY_TERMINAL) {
            """
            TOKEN=${shellQuote(token)}
            /system/bin/am broadcast --user 0 -n com.droiddistro.app/.TermuxEventReceiver -a com.droiddistro.app.TERMUX_EVENT --es token "${'$'}TOKEN" --es state RUNNING --es message 'Linux shell session started' >/dev/null 2>&1 || true
            proot-distro login ${distroId}
            """.trimIndent()
        } else {
            """
            TOKEN=${shellQuote(token)}
            /system/bin/am broadcast --user 0 -n com.droiddistro.app/.TermuxEventReceiver -a com.droiddistro.app.TERMUX_EVENT --es token "${'$'}TOKEN" --es state RUNNING --es message 'Starting ${desktop} on Termux:X11' >/dev/null 2>&1 || true
            pkg install -y x11-repo && pkg install -y termux-x11-nightly
            termux-x11 :1 -legacy-drawing >/dev/null 2>&1 &
            sleep 2
            proot-distro login ${distroId} --shared-tmp -- bash -lc ${shellQuote(guestStartCommand(desktop))}
            """.trimIndent()
        }
        if (!sendToTermux(script, "Start ${distro} ${desktop}")) return
        if (displayMode == DISPLAY_X11 && desktop != "Terminal only") {
            try {
                val x11Intent = packageManager.getLaunchIntentForPackage(PKG_X11)
                    ?: Intent().setClassName(PKG_X11, "com.termux.x11.MainActivity")
                x11Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(x11Intent)
            } catch (_: Exception) {
                toast("Termux:X11 app was not found. Install its Android app first.")
            }
        }
        page = PAGE_LOGS
        render()
    }

    private fun stopLinux() {
        val token = getOrCreateToken()
        val distroId = distroId(distro)
        val script = """
            TOKEN=${shellQuote(token)}
            /system/bin/am broadcast --user 0 -n com.droiddistro.app/.TermuxEventReceiver -a com.droiddistro.app.TERMUX_EVENT --es token "${'$'}TOKEN" --es state STOPPING --es message 'Stopping Linux session' >/dev/null 2>&1 || true
            proot-distro kill ${distroId} || true
            /system/bin/am broadcast --user 0 -n com.droiddistro.app/.TermuxEventReceiver -a com.droiddistro.app.TERMUX_EVENT --es token "${'$'}TOKEN" --es state STOPPED --es message 'Linux sessions stopped' >/dev/null 2>&1 || true
        """.trimIndent()
        if (sendToTermux(script, "Stop ${distro}")) {
            page = PAGE_LOGS
            render()
        }
    }

    private fun confirmRemove() {
        AlertDialog.Builder(this)
            .setTitle("Remove Linux environment?")
            .setMessage("This permanently deletes the $distro root filesystem and its files. This cannot be undone.")
            .setPositiveButton("REMOVE") { _, _ -> removeEnvironment() }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun removeEnvironment() {
        val token = getOrCreateToken()
        val script = """
            TOKEN=${shellQuote(token)}
            /system/bin/am broadcast --user 0 -n com.droiddistro.app/.TermuxEventReceiver -a com.droiddistro.app.TERMUX_EVENT --es token "${'$'}TOKEN" --es state REMOVING --es message 'Removing Linux environment' >/dev/null 2>&1 || true
            proot-distro remove ${distroId(distro)}
            /system/bin/am broadcast --user 0 -n com.droiddistro.app/.TermuxEventReceiver -a com.droiddistro.app.TERMUX_EVENT --es token "${'$'}TOKEN" --es state NOT_INSTALLED --es message 'Linux environment removed' >/dev/null 2>&1 || true
        """.trimIndent()
        if (sendToTermux(script, "Remove ${distro}")) {
            prefs.edit().putBoolean("installed", false).putString("installState", "REMOVING").apply()
            page = PAGE_LOGS
            render()
        }
    }

    private fun checkTermuxSetup() {
        if (!isPackageInstalled(PKG_TERMUX)) {
            AlertDialog.Builder(this).setTitle("Termux not detected")
                .setMessage("Install Termux and open it once before using DroidDistro.")
                .setPositiveButton("OPEN DOWNLOAD") { _, _ -> openUrl(URL_TERMUX) }
                .setNegativeButton("CLOSE", null).show()
            return
        }
        val permissionGranted = checkSelfPermission(PERM_RUN_COMMAND) == PackageManager.PERMISSION_GRANTED
        val message = buildString {
            append(if (permissionGranted) "RUN_COMMAND permission: granted\n" else "RUN_COMMAND permission: NOT GRANTED\n")
            append("\nOne-time command to run inside Termux:\nmkdir -p ~/.termux\ngrep -qxF 'allow-external-apps = true' ~/.termux/termux.properties 2>/dev/null || echo 'allow-external-apps = true' >> ~/.termux/termux.properties\ntermux-reload-settings\n\nThen grant DroidDistro the 'Run commands in Termux environment' permission in Android App info > Permissions > Additional permissions.")
        }
        AlertDialog.Builder(this)
            .setTitle("Termux integration setup")
            .setMessage(message)
            .setPositiveButton("OPEN TERMUX APP INFO") { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                } catch (_: Exception) { }
            }
            .setNeutralButton("OPEN TERMUX") { _, _ ->
                packageManager.getLaunchIntentForPackage(PKG_TERMUX)?.let { startActivity(it) }
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun sendToTermux(stdinScript: String, label: String): Boolean {
        if (!isPackageInstalled(PKG_TERMUX)) {
            toast("Termux is not installed")
            return false
        }
        if (checkSelfPermission(PERM_RUN_COMMAND) != PackageManager.PERMISSION_GRANTED) {
            checkTermuxSetup()
            return false
        }
        return try {
            val intent = Intent().apply {
                setClassName(PKG_TERMUX, "com.termux.app.RunCommandService")
                setAction("com.termux.RUN_COMMAND")
                putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS", emptyArray<String>())
                putExtra("com.termux.RUN_COMMAND_STDIN", stdinScript)
                putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
                putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0")
                putExtra("com.termux.RUN_COMMAND_COMMAND_LABEL", label)
                putExtra("com.termux.RUN_COMMAND_COMMAND_DESCRIPTION", "DroidDistro controls this Linux environment through Termux.")
            }
            startService(intent)
            true
        } catch (e: SecurityException) {
            toast("Termux denied the request. Enable allow-external-apps and grant RUN_COMMAND permission.")
            false
        } catch (e: Exception) {
            toast("Could not start Termux: ${e.message ?: "unknown error"}")
            false
        }
    }

    private fun guestPackageCommand(distroName: String, desktopName: String): String {
        if (desktopName == "Terminal only") return ""
        val packages = when (desktopName) {
            "XFCE" -> when (distroName) {
                "Arch Linux" -> "xfce4 xfce4-goodies dbus"
                "Alpine Linux" -> "xfce4 xfce4-terminal dbus dbus-x11"
                else -> "xfce4 dbus-x11"
            }
            "LXQt" -> when (distroName) {
                "Arch Linux" -> "lxqt dbus"
                "Alpine Linux" -> "lxqt dbus dbus-x11"
                else -> "lxqt dbus-x11"
            }
            "LXDE" -> when (distroName) {
                "Arch Linux" -> "lxde dbus"
                "Alpine Linux" -> "lxde dbus dbus-x11"
                else -> "lxde dbus-x11"
            }
            "MATE" -> when (distroName) {
                "Arch Linux" -> "mate mate-extra dbus"
                "Alpine Linux" -> "mate dbus dbus-x11"
                else -> "mate-desktop-environment dbus-x11"
            }
            else -> when (distroName) {
                "Arch Linux" -> "openbox dbus"
                "Alpine Linux" -> "openbox dbus dbus-x11"
                else -> "openbox dbus-x11"
            }
        }
        val command = when (distroName) {
            "Arch Linux" -> "pacman -Syu --noconfirm --needed $packages"
            "Alpine Linux" -> "apk update && apk add $packages"
            else -> "apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y $packages"
        }
        return command
    }

    private fun guestStartCommand(desktopName: String): String {
        val session = when (desktopName) {
            "XFCE" -> "xfce4-session"
            "LXQt" -> "startlxqt"
            "LXDE" -> "startlxde"
            "MATE" -> "mate-session"
            "Openbox" -> "openbox-session"
            else -> "bash -l"
        }
        return "export DISPLAY=:1; export PULSE_SERVER=127.0.0.1; if command -v dbus-launch >/dev/null 2>&1; then exec dbus-launch --exit-with-session $session; elif command -v dbus-run-session >/dev/null 2>&1; then exec dbus-run-session -- $session; else exec $session; fi"
    }

    private fun distroId(name: String): String = when (name) {
        "Ubuntu" -> "ubuntu"
        "Alpine Linux" -> "alpine"
        "Arch Linux" -> "archlinux"
        else -> "debian"
    }

    private fun isCurrentEnvironmentInstalled(): Boolean {
        if (!prefs.getBoolean("installed", false)) return false
        val installedDistro = prefs.getString("installedDistro", null) ?: return false
        val installedDesktop = prefs.getString("installedDesktop", null) ?: return false
        return installedDistro == distroId(distro) && installedDesktop == desktop
    }

    private fun getOrCreateToken(): String {
        val current = prefs.getString("eventToken", null)
        if (!current.isNullOrBlank()) return current
        val generated = UUID.randomUUID().toString().replace("-", "")
        prefs.edit().putString("eventToken", generated).apply()
        return generated
    }

    private fun addLocalLog(state: String, message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val updated = (logText + "[$time] [$state] $message\n").takeLast(12000)
        prefs.edit().putString("logText", updated).putString("lastMessage", message).apply()
        loadPreferences()
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun displayLabel(mode: String): String = when (mode) {
        DISPLAY_TERMINAL -> "Terminal only"
        DISPLAY_BUILTIN -> "Built-in VNC (planned)"
        else -> "Termux:X11"
    }

    private fun openUrl(url: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        catch (_: Exception) { toast("Could not open link") }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun card(parent: LinearLayout, content: LinearLayout.() -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(panel, 18)
        }
        parent.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        box.content()
    }

    private fun option(parent: LinearLayout, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(14), dp(15), dp(14))
            background = rounded(if (selected) Color.rgb(24, 55, 44) else panel, 15, if (selected) accent else Color.rgb(40, 53, 67))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
        parent.addView(item, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(9) })
        addText(item, (if (selected) "●  " else "○  ") + title, 16, if (selected) accent else textMain, true)
        addText(item, subtitle, 12, textMuted, false, top = 5)
    }

    private fun infoRow(parent: LinearLayout, label: String, value: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        val left = makeText(label, 12, textMuted, false)
        val right = makeText(value, 12, textMain, true).apply { gravity = Gravity.END }
        row.addView(left, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(right, LinearLayout.LayoutParams(0, -2, 1.2f))
        parent.addView(row)
        parent.addView(View(this).apply { setBackgroundColor(Color.rgb(39, 51, 65)) }, LinearLayout.LayoutParams(-1, dp(1)))
    }

    private fun footer(parent: LinearLayout, backText: String, back: () -> Unit, nextText: String, next: () -> Unit) {
        addSpacer(parent, 18)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        parent.addView(row, LinearLayout.LayoutParams(-1, -2))
        row.addView(makeAction(backText, false, back), LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginEnd = dp(6) })
        row.addView(makeAction(nextText, true, next), LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginStart = dp(6) })
    }

    private fun action(parent: LinearLayout, label: String, primary: Boolean, onClick: () -> Unit) {
        parent.addView(makeAction(label, primary, onClick), LinearLayout.LayoutParams(-1, dp(54)))
    }

    private fun makeAction(label: String, primary: Boolean, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(if (primary) Color.rgb(7, 19, 14) else textMain)
            background = rounded(if (primary) accent else panelAlt, 14, if (primary) accent else Color.rgb(54, 70, 87))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }

    private fun pill(parent: LinearLayout, label: String) {
        val badge = TextView(this).apply {
            text = label
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(accent)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = rounded(Color.rgb(24, 55, 44), 50)
        }
        parent.addView(badge, LinearLayout.LayoutParams(-2, -2))
    }

    private fun addText(parent: LinearLayout, value: String, size: Int, color: Int, bold: Boolean, top: Int = 0, mono: Boolean = false) {
        parent.addView(makeText(value, size, color, bold, mono), LinearLayout.LayoutParams(-1, -2).apply {
            if (top > 0) topMargin = dp(top)
        })
    }

    private fun makeText(value: String, size: Int, color: Int, bold: Boolean, mono: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            typeface = if (mono) Typeface.MONOSPACE else if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            setLineSpacing(dp(3).toFloat(), 1f)
        }
    }

    private fun addSpacer(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun rounded(color: Int, radius: Int, stroke: Int? = null): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun formatBytes(bytes: Long): String {
        var size = bytes.toDouble()
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var index = 0
        while (size >= 1024 && index < units.lastIndex) {
            size /= 1024
            index++
        }
        return String.format(Locale.US, "%.1f %s", size, units[index])
    }

    private fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

    companion object {
        private const val PREFS = "droiddistro"
        private const val PAGE_WELCOME = 0
        private const val PAGE_DEVICE = 1
        private const val PAGE_DISTRO = 2
        private const val PAGE_DESKTOP = 3
        private const val PAGE_DISPLAY = 4
        private const val PAGE_REVIEW = 5
        private const val PAGE_DASHBOARD = 6
        private const val PAGE_BOOT = 7
        private const val PAGE_LOGS = 8
        private const val DISPLAY_X11 = "Termux:X11"
        private const val DISPLAY_TERMINAL = "Terminal only"
        private const val DISPLAY_BUILTIN = "Built-in VNC"
        private const val PKG_TERMUX = "com.termux"
        private const val PKG_X11 = "com.termux.x11"
        private const val PERM_RUN_COMMAND = "com.termux.permission.RUN_COMMAND"
        private const val URL_TERMUX = "https://github.com/termux/termux-app"
        private const val URL_X11 = "https://github.com/termux/termux-x11/releases"
    }
}
