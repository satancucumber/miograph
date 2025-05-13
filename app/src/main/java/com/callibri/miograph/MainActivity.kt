package com.callibri.miograph

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.color.DynamicColors
import com.google.android.material.elevation.SurfaceColors
import com.neurosdk2.neuro.types.SensorState
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocale(newBase))
    }

    private fun updateLocale(context: Context): Context {
        val language = App.prefs.getString("language", "en") ?: "en"
        val locale = Locale(language)
        Locale.setDefault(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            val resources = context.resources
            val config = Configuration(resources.configuration)
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
            context
        }
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        updateLocale(this)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLanguageSelector()

        setSupportActionBar(binding.toolbar)

        val color = SurfaceColors.SURFACE_2.getColor(this)
        window.statusBarColor = color

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.navigationBarColor = getColor(R.color.colorDevState)
        }

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        CallibriController.connectionStateChanged = { state ->
            lifecycleScope.launch(Dispatchers.Main) {
                binding.txtDevState.text = if (state == SensorState.StateInRange)
                    getString(R.string.dev_state_connected)
                else
                    getString(R.string.dev_state_disconnected)

                if (state == SensorState.StateOutOfRange) {
                    binding.txtDevBatteryPower.text = getString(R.string.dev_power_prc, 0)
                    val currentDest = navController.currentDestination?.id
                    if (currentDest == R.id.MenuFragment || currentDest == R.id.emgFragment || currentDest == R.id.infoFragment) {
                        navController.popBackStack(R.id.MenuFragment, false)
                        navController.navigate(R.id.MenuFragment)
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.disconnect_message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


        CallibriController.onBatteryChanged = { level ->
            lifecycleScope.launch(Dispatchers.Main) {
                binding.txtDevBatteryPower.text = getString(R.string.dev_power_prc, level)
            }
        }
    }

    private fun setupLanguageSelector() {
        val languages = arrayOf(
            getString(R.string.language_english),
            getString(R.string.language_russian)
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.toolbar.findViewById<Spinner>(R.id.languageSpinner).apply {
            this.adapter = adapter
            setSelection(if (App.prefs.getString("language", "en") == "en") 0 else 1)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    val lang = if (pos == 0) "en" else "ru"
                    if (App.prefs.getString("language", "en") != lang) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            App.prefs.edit().putString("language", lang).apply()
                            runOnUiThread {
                                recreate()
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        CallibriController.closeSensor()
    }
}