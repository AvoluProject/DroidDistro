package com.droiddistro.app

import android.app.Activity
import android.app.AlertDialog
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
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
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : Activity() {
    private val lightTheme: Boolean get() = prefs.getString("theme", "dark") == "light"
    private val bg: Int get() = if (lightTheme) Color.rgb(243, 247, 250) else Color.rgb(8, 12, 18)
    private val panel: Int get() = if (lightTheme) Color.WHITE else Color.rgb(17, 24, 34)
    private val panelAlt: Int get() = if (lightTheme) Color.rgb(231, 238, 244) else Color.rgb(22, 32, 44)
    private val accent: Int get() = if (lightTheme) Color.rgb(0, 137, 92) else Color.rgb(111, 240, 174)
    private val textMain: Int get() = if (lightTheme) Color.rgb(25, 39, 52) else Color.rgb(238, 244, 248)
    private val textMuted: Int get() = if (lightTheme) Color.rgb(92, 108, 122) else Color.rgb(151, 166, 180)
    private val prefs by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    private var page = 0
    private var distro = "Debian"
    private var desktop = "XFCE"
    private var displayMode = DISPLAY_X11
    private var profileName = "My Linux"
    private var installState = "NOT INSTALLED"
    private var lastMessage = "No activity yet"
    private var logText = "DroidDistro ready.\n"
    private var language = "auto"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadPreferences()
        applySystemBars()
        page = if (prefs.getBoolean("configured", false)) PAGE_DASHBOARD else PAGE_WELCOME
        render()
    }

    override fun onResume() {
        super.onResume()
        loadPreferences()
        if (page == PAGE_DASHBOARD || page == PAGE_LOGS || page == PAGE_BOOT) render()
    }

    @Deprecated("Use system back navigation")
    override fun onBackPressed() {
        page = when (page) {
            PAGE_TOOLS, PAGE_SETTINGS, PAGE_PACKAGES, PAGE_BOOT, PAGE_LOGS -> PAGE_DASHBOARD
            PAGE_DEVICE -> PAGE_WELCOME
            PAGE_DISTRO -> PAGE_DEVICE
            PAGE_DESKTOP -> PAGE_DISTRO
            PAGE_DISPLAY -> PAGE_DESKTOP
            PAGE_REVIEW -> PAGE_DISPLAY
            else -> PAGE_WELCOME
        }
        render()
    }

    private fun applySystemBars() {
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = if (lightTheme) {
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else 0
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
        language = prefs.getString("language", "auto") ?: "auto"
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
        addText(body, "LINUX ENVIRONMENT BUILDER  •  v0.3.0", 10, textMuted, true, top = 5)
        if (prefs.getBoolean("configured", false) && page != PAGE_WELCOME && page != PAGE_DEVICE && page != PAGE_DISTRO && page != PAGE_DESKTOP && page != PAGE_DISPLAY && page != PAGE_REVIEW) {
            addSpacer(body, 14)
            quickNavigation(body)
        }
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
            PAGE_TOOLS -> toolsPage(body)
            PAGE_SETTINGS -> settingsPage(body)
            PAGE_PACKAGES -> packagesPage(body)
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
        PAGE_TOOLS -> "System tools"
        PAGE_SETTINGS -> "App settings"
        PAGE_PACKAGES -> "Package manager"
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
            infoRow(this, "Kernel", System.getProperty("os.version") ?: "Unknown")
            infoRow(this, "Battery", batterySummary())
            infoRow(this, "Screen", "${resources.displayMetrics.widthPixels} × ${resources.displayMetrics.heightPixels} px")
            infoRow(this, "App version", "0.3.0 (3)")
            addSpacer(this, 9)
            addText(this, "STORAGE USAGE", 10, accent, true)
            val used = (totalStorage - freeStorage).coerceAtLeast(0L)
            val usage = if (totalStorage > 0L) ((used * 100L) / totalStorage).toInt().coerceIn(0, 100) else 0
            val bar = ProgressBar(this@MainActivity, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = usage
                progressTintList = android.content.res.ColorStateList.valueOf(accent)
                progressBackgroundTintList = android.content.res.ColorStateList.valueOf(panelAlt)
            }
            addView(bar, LinearLayout.LayoutParams(-1, dp(8)).apply { topMargin = dp(7) })
            addText(this, "$usage% used • ${formatBytes(freeStorage)} free", 11, textMuted, false, top = 6)
        }
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo().apply { manager.getMemoryInfo(this) }
        val memoryHint = if (memory.availMem < 1200L * 1024L * 1024L) "Your available RAM is low. Terminal-only or Openbox may be a smoother starting point." else "For a balanced first desktop, XFCE is a practical choice. Desktop packages and the Linux filesystem need additional storage."
        addText(body, memoryHint, 12, textMuted, false, top = 14)
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
        option(body, "Built-in VNC display", "Not available yet • use Termux:X11 for a graphical session", displayMode == DISPLAY_BUILTIN) { displayMode = DISPLAY_BUILTIN; render() }
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
            val storage = StatFs(Environment.getDataDirectory().path)
            val freeBytes = storage.availableBlocksLong * storage.blockSizeLong
            val totalBytes = storage.blockCountLong * storage.blockSizeLong
            val usedPct = if (totalBytes > 0) (((totalBytes - freeBytes) * 100) / totalBytes).toInt() else 0
            addSpacer(this, 12)
            addText(this, "DEVICE STORAGE  •  $usedPct% USED", 10, textMuted, true)
            val bar = ProgressBar(this@MainActivity, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = usedPct.coerceIn(0, 100)
                progressTintList = android.content.res.ColorStateList.valueOf(accent)
                progressBackgroundTintList = android.content.res.ColorStateList.valueOf(panelAlt)
            }
            addView(bar, LinearLayout.LayoutParams(-1, dp(7)).apply { topMargin = dp(7) })
            addText(this, "${formatBytes(freeBytes)} free of ${formatBytes(totalBytes)}", 11, textMuted, false, top = 5)
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
        action(body, "PACKAGE MANAGER & MAINTENANCE", false) { page = PAGE_PACKAGES; render() }
        addSpacer(body, 8)
        action(body, "SYSTEM TOOLS & DIAGNOSTICS", false) { page = PAGE_TOOLS; render() }
        addSpacer(body, 8)
        action(body, "APP SETTINGS", false) { page = PAGE_SETTINGS; render() }
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
        action(body, "SHARE / EXPORT LOG", false) { shareText("DroidDistro activity log", logText) }
        addSpacer(body, 8)
        action(body, "CLEAR ACTIVITY LOG", false) {
            AlertDialog.Builder(this).setTitle("Clear activity log?")
                .setMessage("This only clears the local DroidDistro log. It does not remove your Linux files.")
                .setPositiveButton("CLEAR") { _, _ ->
                    prefs.edit().putString("logText", "DroidDistro log cleared.\n").apply()
                    loadPreferences(); render()
                }.setNegativeButton("CANCEL", null).show()
        }
        addSpacer(body, 8)
        action(body, "BACK TO MANAGER", false) { page = PAGE_DASHBOARD; render() }
    }

    private fun settingsPage(body: LinearLayout) {
        card(body) {
            addText(this, "PREFERENCES", 11, accent, true)
            addText(this, "Personalize the interface. Auto language follows your Android system language.", 13, textMuted, false, top = 7)
            addSpacer(this, 10)
            infoRow(this, "Language", languageLabel())
            infoRow(this, "System locale", Locale.getDefault().displayLanguage)
            infoRow(this, "Appearance", if (lightTheme) "Light" else "Dark")
            infoRow(this, "Minimum Android", "Android 10 (API 29)")
        }
        action(body, "CHANGE LANGUAGE", true) { chooseLanguage() }
        addSpacer(body, 8)
        action(body, "CHANGE APPEARANCE", false) { chooseTheme() }
        addSpacer(body, 8)
        action(body, "RESTART SETUP WIZARD", false) {
            AlertDialog.Builder(this).setTitle("Start setup again?")
                .setMessage("Your saved profile remains available. This only reopens the guided configuration steps.")
                .setPositiveButton("CONTINUE") { _, _ -> page = PAGE_DEVICE; render() }
                .setNegativeButton("CANCEL", null).show()
        }
        addSpacer(body, 8)
        action(body, "EXPORT PROFILE SETTINGS", false) { shareText("DroidDistro profile", profileExportText()) }
        addSpacer(body, 8)
        action(body, "ABOUT DROIDDISTRO", false) {
            AlertDialog.Builder(this).setTitle("DroidDistro 0.3.0")
                .setMessage("A guided Android front-end for Termux + PRoot-Distro. Linux runs as a userland on the Android kernel; this is not a virtual machine, native dual boot, or a kernel replacement.\n\nBuilt for Android 10 and newer.")
                .setPositiveButton("OK", null).show()
        }
        addSpacer(body, 8)
        action(body, "BACK TO MANAGER", false) { page = PAGE_DASHBOARD; render() }
    }

    private fun toolsPage(body: LinearLayout) {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo().apply { manager.getMemoryInfo(this) }
        val storage = StatFs(Environment.getDataDirectory().path)
        val free = storage.availableBlocksLong * storage.blockSizeLong
        val total = storage.blockCountLong * storage.blockSizeLong
        card(body) {
            addText(this, "LIVE DEVICE SNAPSHOT", 11, accent, true)
            infoRow(this, "Device", "${Build.MANUFACTURER} ${Build.MODEL}")
            infoRow(this, "Android", "${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
            infoRow(this, "Architecture", Build.SUPPORTED_ABIS.joinToString(", "))
            infoRow(this, "RAM available", formatBytes(memory.availMem))
            infoRow(this, "RAM total", formatBytes(memory.totalMem))
            infoRow(this, "Storage free", formatBytes(free))
            infoRow(this, "Storage total", formatBytes(total))
            infoRow(this, "Termux", if (isPackageInstalled(PKG_TERMUX)) "Installed" else "Missing")
            infoRow(this, "Termux:X11", if (isPackageInstalled(PKG_X11)) "Installed" else "Missing")
            infoRow(this, "RUN_COMMAND", if (checkSelfPermission(PERM_RUN_COMMAND) == PackageManager.PERMISSION_GRANTED) "Granted" else "Not granted")
            infoRow(this, "Battery", batterySummary())
        }
        action(body, "RUN SETUP HEALTH CHECK", true) { showTroubleshooter() }
        addSpacer(body, 8)
        action(body, "CHECK TERMUX CONNECTION", false) { checkTermuxSetup() }
        addSpacer(body, 8)
        action(body, "COPY TERMUX CONFIGURATION COMMAND", false) {
            val command = "mkdir -p ~/.termux && grep -qxF 'allow-external-apps = true' ~/.termux/termux.properties 2>/dev/null || echo 'allow-external-apps = true' >> ~/.termux/termux.properties; termux-reload-settings"
            copyText("Termux setup command", command)
        }
        addSpacer(body, 8)
        action(body, "OPEN TERMUX", false) { openPackage(PKG_TERMUX) }
        addSpacer(body, 8)
        action(body, "OPEN TERMUX:X11", false) { openPackage(PKG_X11) }
        addSpacer(body, 8)
        action(body, "SHARE DIAGNOSTIC REPORT", false) { shareText("DroidDistro diagnostics", diagnosticsText()) }
        addSpacer(body, 8)
        action(body, "OPEN TERMUX RUN_COMMAND GUIDE", false) { openUrl(URL_RUN_COMMAND) }
        addSpacer(body, 8)
        action(body, "OPEN PRoot-DISTRO DOCUMENTATION", false) { openUrl(URL_PROOT) }
        addSpacer(body, 8)
        action(body, "BACK TO MANAGER", false) { page = PAGE_DASHBOARD; render() }
    }

    private fun packagesPage(body: LinearLayout) {
        addText(body, "Run common package maintenance tasks inside your selected Linux environment. Termux must be installed and RUN_COMMAND permission must be granted.", 13, textMuted, false)
        val packageField = EditText(this).apply {
            hint = "Example: git curl python3"
            setTextColor(textMain)
            setHintTextColor(textMuted)
            textSize = 15f
            setSingleLine(true)
            setPadding(dp(14), dp(13), dp(14), dp(13))
            background = rounded(panelAlt, 12, Color.rgb(54, 70, 87))
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        card(body) {
            addText(this, "PACKAGE INSTALLER", 11, accent, true)
            addText(this, "Separate package names with spaces. Only letters, numbers, dots, plus, colon, underscore and hyphen are accepted.", 12, textMuted, false, top = 7)
            addView(packageField, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(12) })
        }
        action(body, "INSTALL SELECTED PACKAGES", true) {
            val names = readPackageNames(packageField)
            if (names != null) {
                val joined = names.joinToString(" ")
                confirmDistroTask("Install packages", "Install these packages in $distro?\n\n$joined", packageInstallCommand(joined))
            }
        }
        addSpacer(body, 8)
        action(body, "REMOVE SELECTED PACKAGES", false) {
            val names = readPackageNames(packageField)
            if (names != null) {
                val joined = names.joinToString(" ")
                confirmDistroTask("Remove packages", "Remove these packages from $distro? Review dependencies in the Termux session.\n\n$joined", packageRemoveCommand(joined))
            }
        }
        addSpacer(body, 8)
        action(body, "LIST INSTALLED PACKAGES", false) { confirmDistroTask("List installed packages", "Run the package manager's installed-package listing in $distro? The output appears in Termux.", packageListCommand()) }
        addSpacer(body, 8)
        action(body, "CHECK LINUX DISK USAGE", false) { confirmDistroTask("Check disk usage", "Show filesystem usage from inside $distro?", "df -h / && du -sh / 2>/dev/null || true") }
        addSpacer(body, 8)
        action(body, "UPDATE PACKAGE LISTS", false) { confirmDistroTask("Update package lists", "Refresh the package index in $distro?", packageIndexCommand()) }
        addSpacer(body, 8)
        action(body, "UPGRADE INSTALLED PACKAGES", false) { confirmDistroTask("Upgrade Linux packages", "Upgrade installed packages in $distro. This may download a large amount of data.", packageUpgradeCommand()) }
        addSpacer(body, 8)
        action(body, "CLEAN PACKAGE CACHE", false) { confirmDistroTask("Clean package cache", "Remove cached package downloads inside $distro? Installed applications and personal files are kept.", packageCleanCommand()) }
        addSpacer(body, 8)
        action(body, "OPEN LINUX SHELL", false) { if (isCurrentEnvironmentInstalled()) startLinux() else toast("Install the Linux environment first") }
        addSpacer(body, 8)
        action(body, "BACK TO MANAGER", false) { page = PAGE_DASHBOARD; render() }
    }

    private fun showTroubleshooter() {
        val storage = StatFs(Environment.getDataDirectory().path)
        val freeBytes = storage.availableBlocksLong * storage.blockSizeLong
        val termuxReady = isPackageInstalled(PKG_TERMUX)
        val permissionReady = checkSelfPermission(PERM_RUN_COMMAND) == PackageManager.PERMISSION_GRANTED
        val x11Required = displayMode == DISPLAY_X11 && desktop != "Terminal only"
        val x11Ready = !x11Required || isPackageInstalled(PKG_X11)
        val profileReady = !prefs.getBoolean("installed", false) || isCurrentEnvironmentInstalled()
        val report = buildString {
            appendLine("${if (termuxReady) "✓" else "✗"} Termux: ${if (termuxReady) "detected" else "not installed"}")
            appendLine("${if (permissionReady) "✓" else "✗"} RUN_COMMAND: ${if (permissionReady) "granted" else "not granted"}")
            appendLine("${if (x11Ready) "✓" else "✗"} Termux:X11: ${if (x11Ready) if (x11Required) "detected" else "not required" else "not installed"}")
            appendLine("${if (freeBytes >= 3L * 1024L * 1024L * 1024L) "✓" else "!"} Free storage: ${formatBytes(freeBytes)}")
            appendLine("${if (profileReady) "✓" else "!"} Selected profile: ${if (profileReady) "matches installed environment" else "changed; reinstall/repair required"}")
            append("\nThis check reads local Android app/device state. It does not test download mirrors or verify packages inside Linux.")
        }
        AlertDialog.Builder(this).setTitle("DroidDistro setup health")
            .setMessage(report)
            .setPositiveButton(if (!termuxReady || !permissionReady) "FIX TERMUX" else "CHECK TERMUX") { _, _ -> checkTermuxSetup() }
            .setNeutralButton("CLOSE", null)
            .show()
    }

    private fun chooseLanguage() {
        val options = arrayOf("Auto (System)", "Bahasa Indonesia", "English")
        val values = arrayOf("auto", "id", "en")
        val selected = values.indexOf(language).coerceAtLeast(0)
        AlertDialog.Builder(this).setTitle("App language")
            .setSingleChoiceItems(options, selected) { dialog, which ->
                language = values[which]
                prefs.edit().putString("language", language).apply()
                dialog.dismiss(); render()
            }.setNegativeButton("CANCEL", null).show()
    }

    private fun chooseTheme() {
        val options = arrayOf("Dark", "Light")
        val selected = if (lightTheme) 1 else 0
        AlertDialog.Builder(this).setTitle("Appearance")
            .setSingleChoiceItems(options, selected) { dialog, which ->
                prefs.edit().putString("theme", if (which == 1) "light" else "dark").apply()
                applySystemBars()
                dialog.dismiss(); render()
            }.setNegativeButton("CANCEL", null).show()
    }

    private fun languageLabel(): String = when (language) {
        "id" -> "Bahasa Indonesia"
        "en" -> "English"
        else -> "Auto (${Locale.getDefault().displayLanguage})"
    }

    private fun profileExportText(): String = """DroidDistro profile export
Version: 0.3.0
Profile: $profileName
Distro: $distro
Desktop: $desktop
Display: ${displayLabel(displayMode)}
Language: $language
Theme: ${if (lightTheme) "light" else "dark"}

This export contains profile settings only. It is NOT a backup of the Linux filesystem."""

    private fun batterySummary(): String {
        val battery = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return "Unavailable"
        val level = battery.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
        val scale = battery.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
        val status = battery.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
        val percent = if (level >= 0 && scale > 0) "${(level * 100) / scale}%" else "Unknown"
        val charging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING || status == android.os.BatteryManager.BATTERY_STATUS_FULL
        return "$percent${if (charging) " • Charging" else ""}"
    }

    private fun diagnosticsText(): String = """DroidDistro diagnostics
Version: 0.3.0
Device: ${Build.MANUFACTURER} ${Build.MODEL}
Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
ABIs: ${Build.SUPPORTED_ABIS.joinToString()}
Kernel: ${System.getProperty("os.version")}
Battery: ${batterySummary()}
Termux: ${isPackageInstalled(PKG_TERMUX)}
Termux:X11: ${isPackageInstalled(PKG_X11)}
RUN_COMMAND granted: ${checkSelfPermission(PERM_RUN_COMMAND) == PackageManager.PERMISSION_GRANTED}
Profile: $profileName / $distro / $desktop
Install state: $installState
Free storage: ${formatBytes(StatFs(Environment.getDataDirectory().path).availableBlocksLong * StatFs(Environment.getDataDirectory().path).blockSizeLong)}

Recent log:
$logText"""

    private fun shareText(subject: String, content: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, content)
            }
            startActivity(Intent.createChooser(intent, "Share with"))
        } catch (_: Exception) { toast("No compatible sharing app found") }
    }

    private fun copyText(label: String, content: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, content))
        toast("Copied to clipboard")
    }

    private fun openPackage(packageName: String) {
        if (!isPackageInstalled(packageName)) {
            toast("App not installed: $packageName")
            if (packageName == PKG_TERMUX) openUrl(URL_TERMUX)
            if (packageName == PKG_X11) openUrl(URL_X11)
            return
        }
        try { packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) } ?: toast("Could not open app") }
        catch (_: Exception) { toast("Could not open app") }
    }

    private fun quickNavigation(body: LinearLayout) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        body.addView(row, LinearLayout.LayoutParams(-1, dp(48)))
        val items = listOf("HOME" to PAGE_DASHBOARD, "TOOLS" to PAGE_TOOLS, "PACKAGES" to PAGE_PACKAGES, "SETTINGS" to PAGE_SETTINGS)
        items.forEachIndexed { index, item ->
            val chip = TextView(this).apply {
                text = translate(item.first)
                contentDescription = translate(item.first)
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(if (page == item.second) { if (lightTheme) Color.WHITE else Color.rgb(7, 19, 14) } else textMain)
                background = rounded(if (page == item.second) accent else panelAlt, 10)
                isClickable = true
                isFocusable = true
                setOnClickListener { page = item.second; render() }
            }
            row.addView(chip, LinearLayout.LayoutParams(0, -1, 1f).apply {
                if (index > 0) marginStart = dp(5)
                if (index < items.lastIndex) marginEnd = dp(5)
            })
        }
    }

    private fun readPackageNames(field: EditText): List<String>? {
        val names = field.text.toString().trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (names.isEmpty() || names.size > 20 || names.any { !it.matches(Regex("[A-Za-z0-9][A-Za-z0-9.+:_-]{0,63}")) }) {
            toast("Enter 1–20 valid package names. Avoid command symbols.")
            return null
        }
        return names
    }

    private fun packageRemoveCommand(packages: String): String = when (distro) {
        "Arch Linux" -> "pacman -Rns --noconfirm $packages"
        "Alpine Linux" -> "apk del $packages"
        else -> "DEBIAN_FRONTEND=noninteractive apt-get remove -y $packages"
    }

    private fun packageListCommand(): String = when (distro) {
        "Arch Linux" -> "pacman -Q"
        "Alpine Linux" -> "apk info"
        else -> "apt list --installed"
    }

    private fun packageIndexCommand(): String = when (distro) {
        "Arch Linux" -> "pacman -Sy --noconfirm"
        "Alpine Linux" -> "apk update"
        else -> "apt-get update"
    }

    private fun packageUpgradeCommand(): String = when (distro) {
        "Arch Linux" -> "pacman -Syu --noconfirm"
        "Alpine Linux" -> "apk update && apk upgrade"
        else -> "apt-get update && DEBIAN_FRONTEND=noninteractive apt-get upgrade -y"
    }

    private fun packageCleanCommand(): String = when (distro) {
        "Arch Linux" -> "pacman -Sc --noconfirm"
        "Alpine Linux" -> "apk cache clean"
        else -> "apt-get autoremove -y && apt-get clean"
    }

    private fun packageInstallCommand(packages: String): String = when (distro) {
        "Arch Linux" -> "pacman -S --noconfirm --needed $packages"
        "Alpine Linux" -> "apk add $packages"
        else -> "DEBIAN_FRONTEND=noninteractive apt-get install -y $packages"
    }

    private fun confirmDistroTask(title: String, message: String, command: String) {
        if (!isCurrentEnvironmentInstalled()) {
            toast("Install the selected Linux environment first")
            return
        }
        AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("RUN") { _, _ -> runDistroTask(title, command) }
            .setNegativeButton("CANCEL", null).show()
    }

    private fun runDistroTask(title: String, guestCommand: String) {
        val token = getOrCreateToken()
        val safeCommand = "proot-distro login ${distroId(distro)} --shared-tmp -- bash -lc ${shellQuote(guestCommand)}"
        val script = """#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail
TOKEN=${shellQuote(token)}
notify_app() { /system/bin/am broadcast --user 0 -n com.droiddistro.app/.TermuxEventReceiver -a com.droiddistro.app.TERMUX_EVENT --es token "${'$'}TOKEN" --es state "${'$'}1" --es message "${'$'}2" >/dev/null 2>&1 || true; }
trap 'code=${'$'}?; notify_app TASK_ERROR "Task failed (exit ${'$'}code). See the Termux session for details."; exit "${'$'}code"' ERR
notify_app TASK_RUNNING ${shellQuote(title + " started")}
$safeCommand
notify_app TASK_DONE ${shellQuote(title + " completed")}
""".trimIndent()
        if (sendToTermux(script, "DroidDistro: $title")) {
            prefs.edit().putString("installState", "TASK_RUNNING").putString("lastMessage", "$title started in Termux").apply()
            addLocalLog("TASK", "$title sent to Termux")
            page = PAGE_LOGS
            render()
        }
    }

    private fun confirmInstall(reinstall: Boolean) {
        if (displayMode == DISPLAY_BUILTIN && desktop != "Terminal only") {
            AlertDialog.Builder(this)
                .setTitle("Built-in display is not ready")
                .setMessage("The embedded VNC viewer is not included in v0.3.0. Choose Termux:X11 for a working graphical desktop, or Terminal only for a shell session.")
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
        val storage = StatFs(Environment.getDataDirectory().path)
        val freeSpace = storage.availableBlocksLong * storage.blockSizeLong
        val storageNote = if (freeSpace < 3L * 1024L * 1024L * 1024L) "\n\nHeads-up: this device currently reports less than 3 GB free. Installation may fail or leave too little space. Free space first if possible." else "\n\nFree device storage reported: ${formatBytes(freeSpace)}."
        AlertDialog.Builder(this)
            .setTitle(if (reinstall) "Repair environment?" else "Install $distro?")
            .setMessage("DroidDistro will ask Termux to update packages, install PRoot-Distro, download the $distro root filesystem, and install ${desktop} packages if selected. This can use several gigabytes of data and storage. Keep the phone charged and use a stable connection.$storageNote")
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
                .setMessage("The embedded VNC viewer is not part of v0.3.0. Choose Termux:X11 in Configure Profile to use a working graphical backend now.")
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
            .setPositiveButton("OPEN DROIDDISTRO APP INFO") { _, _ ->
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

    private fun translate(value: String): String {
        val english = when (language) {
            "en" -> true
            "id" -> false
            else -> !Locale.getDefault().language.equals("id", ignoreCase = true)
        }
        if (english) return value
        return when (value) {
            "Your Linux. Your rules." -> "Linux milikmu, atur sesukamu."
            "Device inspection" -> "Pemeriksaan perangkat"
            "Choose a distribution" -> "Pilih distribusi Linux"
            "Choose your desktop" -> "Pilih desktop"
            "Display configuration" -> "Pengaturan tampilan"
            "Review configuration" -> "Periksa konfigurasi"
            "Environment manager" -> "Pengelola lingkungan Linux"
            "Boot console" -> "Konsol sistem"
            "Activity log" -> "Log aktivitas"
            "System tools" -> "Alat sistem"
            "App settings" -> "Pengaturan aplikasi"
            "Package manager" -> "Pengelola paket"
            "HOME" -> "BERANDA"
            "TOOLS" -> "ALAT"
            "PACKAGES" -> "PAKET"
            "SETTINGS" -> "SETELAN"
            "A pocket-sized Linux lab" -> "Laboratorium Linux dalam genggaman"
            "Create a Linux userland, add a desktop, and start it from a guided Android interface. No manual Linux commands are needed for the normal setup flow." -> "Buat lingkungan Linux, tambahkan desktop, lalu jalankan melalui panduan Android. Pengaturan normal tidak memerlukan perintah Linux manual."
            "Choose the Linux userland to install using PRoot-Distro." -> "Pilih lingkungan Linux yang akan dipasang menggunakan PRoot-Distro."
            "Choose the graphical desktop or a terminal-only environment." -> "Pilih desktop grafis atau lingkungan terminal saja."
            "Choose how to display the Linux session." -> "Pilih cara menampilkan sesi Linux."
            "Stable base • recommended first environment" -> "Basis stabil • disarankan untuk pemula"
            "General-purpose environment" -> "Lingkungan serbaguna"
            "Small and minimalist" -> "Kecil dan minimalis"
            "Rolling-release environment" -> "Rilis bergulir dengan pembaruan terbaru"
            "Balanced desktop • recommended for the first run" -> "Desktop seimbang • disarankan untuk awal"
            "Lightweight Qt desktop" -> "Desktop Qt yang ringan"
            "Classic lightweight desktop" -> "Desktop klasik yang ringan"
            "Traditional full desktop" -> "Desktop lengkap bergaya tradisional"
            "Minimal window manager" -> "Window manager minimalis"
            "No graphical desktop packages" -> "Tanpa paket desktop grafis"
            "Available backend • opens the separate X11 app" -> "Tersedia • membuka aplikasi X11 terpisah"
            "Run the Linux shell in a Termux session" -> "Jalankan shell Linux melalui sesi Termux"
            "Not available yet • use Termux:X11 for a graphical session" -> "Belum tersedia • gunakan Termux:X11 untuk sesi grafis"
            "REQUIREMENTS FOR TERMUX:X11" -> "PERSYARATAN TERMUX:X11"
            "PROFILE NAME" -> "NAMA PROFIL"
            "INSTALLATION PLAN" -> "RENCANA INSTALASI"
            "Saving this profile does not download Linux yet. The INSTALL action on the dashboard performs the actual installation through Termux." -> "Menyimpan profil belum mengunduh Linux. Proses pemasangan sebenarnya dijalankan melalui Termux dari dashboard."
            "The app currently installs and launches real PRoot environments through Termux. Termux:X11 is an external display app; the embedded VNC screen remains a future feature." -> "Aplikasi ini memasang dan menjalankan lingkungan PRoot melalui Termux. Termux:X11 adalah aplikasi tampilan terpisah; tampilan VNC bawaan belum tersedia."
            "Personalize the interface. Auto language follows your Android system language." -> "Atur tampilan aplikasi. Bahasa otomatis mengikuti bahasa sistem Android."
            "Run common package maintenance tasks inside your selected Linux environment. Termux must be installed and RUN_COMMAND permission must be granted." -> "Jalankan pemeliharaan paket pada lingkungan Linux pilihanmu. Termux harus terpasang dan izin RUN_COMMAND harus diberikan."
            "Separate package names with spaces. Only letters, numbers, dots, plus, colon, underscore and hyphen are accepted." -> "Pisahkan nama paket dengan spasi. Hanya huruf, angka, titik, plus, titik dua, garis bawah, dan tanda hubung yang diterima."
            "START GUIDED SETUP" -> "MULAI PANDUAN PENGATURAN"
            "OPEN ENVIRONMENT MANAGER" -> "BUKA PENGELOLA LINUX"
            "DEVICE REPORT" -> "LAPORAN PERANGKAT"
            "Total RAM" -> "Total RAM"
            "Available RAM" -> "RAM tersedia"
            "Free storage" -> "Penyimpanan kosong"
            "Total storage" -> "Total penyimpanan"
            "Termux" -> "Termux"
            "Termux:X11" -> "Termux:X11"
            "SAVE PROFILE & FINISH SETUP" -> "SIMPAN PROFIL & SELESAIKAN"
            "INSTALL LINUX ENVIRONMENT" -> "INSTAL LINGKUNGAN LINUX"
            "CHECK TERMUX CONNECTION" -> "PERIKSA KONEKSI TERMUX"
            "OPEN DROIDDISTRO APP INFO" -> "BUKA INFO APLIKASI DROIDDISTRO"
            "POWER ON LINUX DESKTOP" -> "JALANKAN DESKTOP LINUX"
            "START LINUX SHELL" -> "JALANKAN SHELL LINUX"
            "REINSTALL / REPAIR ENVIRONMENT" -> "INSTAL ULANG / PERBAIKI LINUX"
            "STOP RUNNING SESSION" -> "HENTIKAN SESI YANG BERJALAN"
            "REMOVE LINUX ENVIRONMENT" -> "HAPUS LINGKUNGAN LINUX"
            "PACKAGE MANAGER & MAINTENANCE" -> "PENGELOLA PAKET & PEMELIHARAAN"
            "SYSTEM TOOLS & DIAGNOSTICS" -> "ALAT SISTEM & DIAGNOSTIK"
            "APP SETTINGS" -> "PENGATURAN APLIKASI"
            "BOOT CONSOLE" -> "KONSOL SISTEM"
            "FULL ACTIVITY LOG" -> "LOG AKTIVITAS LENGKAP"
            "CONFIGURE PROFILE" -> "ATUR PROFIL"
            "BACK TO MANAGER" -> "KEMBALI KE PENGELOLA"
            "OPEN FULL ACTIVITY LOG" -> "BUKA LOG AKTIVITAS"
            "LATEST EVENTS" -> "AKTIVITAS TERBARU"
            "REFRESH LOG" -> "SEGARKAN LOG"
            "SHARE / EXPORT LOG" -> "BAGIKAN / EKSPOR LOG"
            "CLEAR ACTIVITY LOG" -> "HAPUS LOG AKTIVITAS"
            "LIVE DEVICE SNAPSHOT" -> "RINGKASAN PERANGKAT"
            "RUN SETUP HEALTH CHECK" -> "PERIKSA KONDISI PENGATURAN"
            "CHANGE LANGUAGE" -> "UBAH BAHASA"
            "CHANGE APPEARANCE" -> "UBAH TAMPILAN"
            "RESTART SETUP WIZARD" -> "ULANGI PANDUAN PENGATURAN"
            "EXPORT PROFILE SETTINGS" -> "EKSPOR PENGATURAN PROFIL"
            "ABOUT DROIDDISTRO" -> "TENTANG DROIDDISTRO"
            "INSTALL SELECTED PACKAGES" -> "INSTAL PAKET TERPILIH"
            "REMOVE SELECTED PACKAGES" -> "HAPUS PAKET TERPILIH"
            "LIST INSTALLED PACKAGES" -> "LIHAT PAKET TERINSTAL"
            "CHECK LINUX DISK USAGE" -> "PERIKSA PENGGUNAAN DISK LINUX"
            "UPDATE PACKAGE LISTS" -> "PERBARUI DAFTAR PAKET"
            "UPGRADE INSTALLED PACKAGES" -> "TINGKATKAN PAKET TERINSTAL"
            "PACKAGE INSTALLER" -> "PEMASANG PAKET"
            "CLEAN PACKAGE CACHE" -> "BERSIHKAN CACHE PAKET"
            "OPEN LINUX SHELL" -> "BUKA SHELL LINUX"
            "PREFERENCES" -> "PREFERENSI"
            "Language" -> "Bahasa"
            "Appearance" -> "Tampilan"
            "System locale" -> "Bahasa sistem"
            "Dark" -> "Gelap"
            "Light" -> "Terang"
            "Detected" -> "Terdeteksi"
            "Not detected" -> "Tidak terdeteksi"
            "Installed" -> "Terpasang"
            "Missing" -> "Belum ada"
            "NOT INSTALLED" -> "BELUM TERPASANG"
            "READY" -> "SIAP"
            "INSTALLING" -> "SEDANG MEMASANG"
            "INSTALL" -> "INSTAL"
            "PROFILE CHANGED — REINSTALL" -> "PROFIL BERUBAH — INSTAL ULANG"
            "TASK_RUNNING" -> "TUGAS BERJALAN"
            "TASK_DONE" -> "TUGAS SELESAI"
            "TASK_ERROR" -> "TUGAS GAGAL"
            "Terminal only" -> "Terminal saja"
            "Built-in VNC (planned)" -> "VNC bawaan (direncanakan)"
            "BACK" -> "KEMBALI"
            "CONTINUE" -> "LANJUTKAN"
            "REVIEW" -> "TINJAU"
            "CANCEL" -> "BATAL"
            "REMOVE" -> "HAPUS"
            "RUN" -> "JALANKAN"
            "CLOSE" -> "TUTUP"
            else -> value
        }
    }

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
            background = rounded(if (selected) { if (lightTheme) Color.rgb(221, 245, 236) else Color.rgb(24, 55, 44) } else panel, 15, if (selected) accent else Color.rgb(40, 53, 67))
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
            text = translate(label)
            contentDescription = translate(label)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(if (primary) { if (lightTheme) Color.WHITE else Color.rgb(7, 19, 14) } else textMain)
            background = rounded(if (primary) accent else panelAlt, 14, if (primary) accent else Color.rgb(54, 70, 87))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }

    private fun pill(parent: LinearLayout, label: String) {
        val badge = TextView(this).apply {
            text = translate(label)
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(accent)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = rounded(if (lightTheme) Color.rgb(221, 245, 236) else Color.rgb(24, 55, 44), 50)
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
            text = translate(value)
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
        private const val PAGE_TOOLS = 9
        private const val PAGE_SETTINGS = 10
        private const val PAGE_PACKAGES = 11
        private const val DISPLAY_X11 = "Termux:X11"
        private const val DISPLAY_TERMINAL = "Terminal only"
        private const val DISPLAY_BUILTIN = "Built-in VNC"
        private const val PKG_TERMUX = "com.termux"
        private const val PKG_X11 = "com.termux.x11"
        private const val PERM_RUN_COMMAND = "com.termux.permission.RUN_COMMAND"
        private const val URL_TERMUX = "https://github.com/termux/termux-app"
        private const val URL_X11 = "https://github.com/termux/termux-x11/releases"
        private const val URL_RUN_COMMAND = "https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent"
        private const val URL_PROOT = "https://github.com/termux/proot-distro"
    }
}
