package io.jamesclonk.oevview

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.EditText
import android.widget.LinearLayout
import io.jamesclonk.oevview.databinding.ActivityMainBinding

const val URL_MESSAGE = "io.jamesclonk.oevview.URL_MESSAGE"
const val URL_A_DEFAULT = "https://oevplus.ch/monitor/?viewType=splitView&layout=1&showClock=true&showPerron=true&stationGroup1Title=Bern%20HB&stationGroup2Title=Bern%2C%20Wankdorf%20Center&station_1_id=85%3A7000&station_1_name=Bern&station_1_quantity=10&station_1_group=1&station_2_id=85%3A88699&station_2_name=Bern%2C%20Wankdorf%20Center&station_2_quantity=5&station_2_group=2"
const val URL_B_DEFAULT = "https://oevplus.ch/"
const val URL_C_DEFAULT = "https://www.srf.ch/news"
const val URL_D_DEFAULT = "https://jamesclonk.io/news"
const val URL_E_DEFAULT = "https://news.ycombinator.com/"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fullscreenContent: LinearLayout
    private val hideHandler = Handler(Looper.myLooper()!!)

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = binding.fullscreenContent
        fullscreenContent.setOnClickListener { toggle() }

        restoreSettings()
    }

    fun display(view: View) {
        saveSettings()

        val url: String
        when(view.id) {
            R.id.display_url_a -> url = findViewById<EditText>(R.id.url_a).text.toString()
            R.id.display_url_b -> url = findViewById<EditText>(R.id.url_b).text.toString()
            R.id.display_url_c -> url = findViewById<EditText>(R.id.url_c).text.toString()
            R.id.display_url_d -> url = findViewById<EditText>(R.id.url_d).text.toString()
            R.id.display_url_e -> url = findViewById<EditText>(R.id.url_e).text.toString()
            else -> url = URL_A_DEFAULT
        }

        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra(URL_MESSAGE, url)
        }
        startActivity(intent)
    }

    fun reset(view: View) {
        findViewById<EditText>(R.id.url_a).setText(URL_A_DEFAULT)
        findViewById<EditText>(R.id.url_b).setText(URL_B_DEFAULT)
        findViewById<EditText>(R.id.url_c).setText(URL_C_DEFAULT)
        findViewById<EditText>(R.id.url_d).setText(URL_D_DEFAULT)
        findViewById<EditText>(R.id.url_e).setText(URL_E_DEFAULT)
        saveSettings()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun saveSettings() {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putString("io.jamesclonk.oevview.url_a", findViewById<EditText>(R.id.url_a).text.toString())
        edit.putString("io.jamesclonk.oevview.url_b", findViewById<EditText>(R.id.url_b).text.toString())
        edit.putString("io.jamesclonk.oevview.url_c", findViewById<EditText>(R.id.url_c).text.toString())
        edit.putString("io.jamesclonk.oevview.url_d", findViewById<EditText>(R.id.url_d).text.toString())
        edit.putString("io.jamesclonk.oevview.url_e", findViewById<EditText>(R.id.url_e).text.toString())
        edit.commit()
    }

    private fun restoreSettings() {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        findViewById<EditText>(R.id.url_a).setText(prefs.getString("io.jamesclonk.oevview.url_a", URL_A_DEFAULT))
        findViewById<EditText>(R.id.url_b).setText(prefs.getString("io.jamesclonk.oevview.url_b", URL_B_DEFAULT))
        findViewById<EditText>(R.id.url_c).setText(prefs.getString("io.jamesclonk.oevview.url_c", URL_C_DEFAULT))
        findViewById<EditText>(R.id.url_d).setText(prefs.getString("io.jamesclonk.oevview.url_d", URL_D_DEFAULT))
        findViewById<EditText>(R.id.url_e).setText(prefs.getString("io.jamesclonk.oevview.url_e", URL_E_DEFAULT))
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}
