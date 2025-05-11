package com.callibri.miograph

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val color = SurfaceColors.SURFACE_2.getColor(this)
        window.statusBarColor = color

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.navigationBarColor = getColor(R.color.colorDevState)
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        CallibriController.closeSensor()
    }
}