package com.droiddistro.app

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(8, 12, 18)
    private val panel = Color.rgb(17, 24, 34)
    private val panelAlt = Color.rgb(22, 32, 44)
    private val accent = Color.rgb(111, 240, 174)
    private val textMain = Color.rgb(238, 244, 248)
    private val textMuted = Color.rgb(151, 166, 180)

    private lateinit var prefs: SharedPreferences
    private var page = 0
    private var distro = "Debian"
    private var desktop = "XFCE"
    private var display = "Built-in Display (VNC)"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("droiddistro", Context.MODE_PRIVATE)
        distro = prefs.getString("distro", "Debian") ?: "Debian"
        desktop = prefs.getString("desktop", "XFCE") ?: "XFCE"
        display = prefs.getString("display", "Built-in Display (VNC)") ?: "Built-in Display (VNC)"
        window.statusBarColor = bg
        window.navigationBarColor = bg
        render()
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
            setPadding(dp(20), dp(20), dp(20), dp(28))
        }
        scroll.addView(body)
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(outer)

        addText(body, "D R O I D D I S T R O", 13, accent, true)
        addText(body, when (page) {
            0 -> "Your Linux. Your rules."
            1 -> "Device inspection"
            2 -> "Choose a distribution"
            3 -> "Choose your desktop"
            4 -> "Choose display mode"
            5 -> "Review configuration"
            7 -> "Boot sequence preview"
            else -> "Environment manager"
        }, 27, textMain, true, top = 8)
        addText(body, "ANDROID LINUX ENVIRONMENT BUILDER  •  PRE-ALPHA 0.1.0", 10, textMuted, true, top = 5)
        addSpacer(body, 20)

        when (page) {
            0 -> welcome(body)
            1 -> devicePage(body)
            2 -> distroPage(body)
            3 -> desktopPage(body)
            4 -> displayPage(body)
            5 -> reviewPage(body)
            6 -> dashboardPage(body)
            7 -> bootPreviewPage(body)
        }
    }

    private fun welcome(body: LinearLayout) {
        card(body) {
            addText(this, "A pocket-sized Linux lab", 22, textMain, true)
            addText(this, "Build your own Linux environment with a guided setup. Pick a distro, desktop, and display backend. No command-line setup wizard required.", 14, textMuted, false, top = 10)
            addSpacer(this, 18)
            pill(this, "PRoot-based • Android 10+ target")
        }
        addSpacer(body, 14)
        action(body, "START GUIDED SETUP", true) { page = 1; render() }
        addSpacer(body, 8)
        action(body, "OPEN DASHBOARD", false) { page = 6; render() }
        addText(body, "This first build creates the app shell and saves your choices. Linux installation and desktop streaming are the next development stages.", 12, textMuted, false, top = 18)
    }

    private fun devicePage(body: LinearLayout) {
        card(body) {
            addText(this, "DEVICE REPORT", 11, accent, true)
            addSpacer(this, 8)
            val memory = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mem = ActivityManager.MemoryInfo()
            memory.getMemoryInfo(mem)
            val storage = StatFs(Environment.getDataDirectory().path)
            val totalStorage = storage.blockCountLong * storage.blockSizeLong
            val freeStorage = storage.availableBlocksLong * storage.blockSizeLong
            infoRow(this, "Device", "${Build.MANUFACTURER} ${Build.MODEL}")
            infoRow(this, "Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            infoRow(this, "CPU ABI", Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown")
            infoRow(this, "Total RAM", formatBytes(mem.totalMem))
            infoRow(this, "Available RAM", formatBytes(mem.availMem))
            infoRow(this, "Free internal storage", formatBytes(freeStorage))
            infoRow(this, "Total internal storage", formatBytes(totalStorage))
        }
        addText(body, "These values are read from Android. They help the installer choose sensible defaults; they do not imply that every desktop will run smoothly.", 12, textMuted, false, top = 14)
        footer(body, "BACK", { page = 0; render() }, "CONTINUE", { page = 2; render() })
    }

    private fun distroPage(body: LinearLayout) {
        addText(body, "Choose the Linux userland you want to prepare.", 14, textMuted, false)
        option(body, "Debian", "Stable base • recommended for the first profile", distro == "Debian") { distro = "Debian"; render() }
        option(body, "Ubuntu", "General-purpose environment", distro == "Ubuntu") { distro = "Ubuntu"; render() }
        option(body, "Alpine Linux", "Small and minimalist", distro == "Alpine Linux") { distro = "Alpine Linux"; render() }
        option(body, "Arch Linux", "Rolling-release style environment", distro == "Arch Linux") { distro = "Arch Linux"; render() }
        footer(body, "BACK", { page = 1; render() }, "CONTINUE", { page = 3; render() })
    }

    private fun desktopPage(body: LinearLayout) {
        addText(body, "Choose a graphical desktop, window manager, or terminal-only setup.", 14, textMuted, false)
        option(body, "XFCE", "Balanced desktop • recommended starting point", desktop == "XFCE") { desktop = "XFCE"; render() }
        option(body, "LXQt", "Lightweight Qt desktop", desktop == "LXQt") { desktop = "LXQt"; render() }
        option(body, "LXDE", "Classic lightweight desktop", desktop == "LXDE") { desktop = "LXDE"; render() }
        option(body, "MATE", "Traditional full desktop", desktop == "MATE") { desktop = "MATE"; render() }
        option(body, "Openbox", "Minimal window manager", desktop == "Openbox") { desktop = "Openbox"; render() }
        option(body, "Terminal only", "No graphical desktop", desktop == "Terminal only") { desktop = "Terminal only"; render() }
        footer(body, "BACK", { page = 2; render() }, "CONTINUE", { page = 4; render() })
    }

    private fun displayPage(body: LinearLayout) {
        addText(body, "Choose how you want to view the Linux environment.", 14, textMuted, false)
        option(body, "Built-in Display (VNC)", "Desktop rendered inside DroidDistro • planned", display == "Built-in Display (VNC)") { display = "Built-in Display (VNC)"; render() }
        option(body, "Termux:X11", "Use the separate Termux:X11 Android app", display == "Termux:X11") { display = "Termux:X11"; render() }
        option(body, "Terminal only", "Run shell sessions without a desktop display", display == "Terminal only") { display = "Terminal only"; render() }
        addText(body, "Built-in Display is the intended default. In this pre-alpha build, these are saved preferences only; no Linux server is started yet.", 12, textMuted, false, top = 12)
        footer(body, "BACK", { page = 3; render() }, "REVIEW", { page = 5; render() })
    }

    private fun reviewPage(body: LinearLayout) {
        card(body) {
            addText(this, "YOUR PROFILE", 11, accent, true)
            addSpacer(this, 10)
            infoRow(this, "Distribution", distro)
            infoRow(this, "Desktop", desktop)
            infoRow(this, "Display", display)
            infoRow(this, "Install mode", "PRoot (planned engine)")
        }
        addText(body, "This stage saves your configuration locally. The next milestone will connect the installer to Termux and install the actual Linux root filesystem.", 12, textMuted, false, top = 14)
        addSpacer(body, 16)
        action(body, "SAVE PROFILE & FINISH SETUP", true) {
            prefs.edit()
                .putString("distro", distro)
                .putString("desktop", desktop)
                .putString("display", display)
                .putBoolean("configured", true)
                .apply()
            page = 6
            render()
        }
        addSpacer(body, 8)
        action(body, "BACK", false) { page = 4; render() }
    }

    private fun dashboardPage(body: LinearLayout) {
        card(body) {
            addText(this, "DEFAULT ENVIRONMENT", 11, accent, true)
            addText(this, distro, 25, textMain, true, top = 8)
            addText(this, "$desktop  /  $display", 13, textMuted, false, top = 4)
            addSpacer(this, 14)
            pill(this, "NOT INSTALLED YET")
            addText(this, "Your profile is saved. Linux installation is not connected in v0.1 yet.", 13, textMuted, false, top = 12)
        }
        addSpacer(body, 14)
        action(body, "PREVIEW BOOT SEQUENCE", true) { page = 7; render() }
        addSpacer(body, 8)
        action(body, "CONFIGURE PROFILE", false) { page = 1; render() }
        addText(body, "ROADMAP  •  Installer engine → PRoot integration → desktop packages → built-in VNC display → boot manager", 11, textMuted, false, top = 18)
    }

    private fun bootPreviewPage(body: LinearLayout) {
        card(body) {
            addText(this, "DROID DISTRO // BOOT CONSOLE", 11, accent, true)
            addSpacer(this, 12)
            val log = """
                [  OK  ] DroidDistro launcher initialized
                [  OK  ] Profile loaded: $distro
                [  OK  ] Desktop target: $desktop
                [  OK  ] Display target: $display
                [ INFO ] Checking Linux environment...
                [ WAIT ] Linux root filesystem not installed

                BOOT PREVIEW COMPLETE
                This is a visual preview, not a real Linux boot.
            """.trimIndent()
            addText(this, log, 13, Color.rgb(170, 238, 190), false, mono = true)
        }
        addText(body, "When the real engine is integrated, this screen will show live installation and startup output from the Linux process.", 12, textMuted, false, top = 14)
        addSpacer(body, 14)
        action(body, "BACK TO DASHBOARD", true) { page = 6; render() }
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
        val line = View(this).apply { setBackgroundColor(Color.rgb(39, 51, 65)) }
        parent.addView(line, LinearLayout.LayoutParams(-1, dp(1)))
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
}
