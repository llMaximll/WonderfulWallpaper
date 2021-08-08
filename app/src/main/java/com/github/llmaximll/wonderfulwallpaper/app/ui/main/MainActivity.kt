package com.github.llmaximll.wonderfulwallpaper.app.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.github.llmaximll.wonderfulwallpaper.R
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Parameters
import com.github.llmaximll.wonderfulwallpaper.app.ui.images.ImagesFragment
import com.github.llmaximll.wonderfulwallpaper.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    ImagesFragment.Callbacks {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.favoriteFragment,
            R.id.imagesFragment,
            R.id.priorityFragment,
            R.id.settingsFragment
        ))
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainState.collect { state ->
                    if (state) {
                        binding.motionLayout.transitionToState(R.id.start)
                    } else {
                        binding.motionLayout.transitionToState(R.id.end)
                    }
                }
            }
        }
    }

    override fun onItemClicked(parameters: Parameters) {
        viewModel.toggleMainState(false)
        val gson = GsonBuilder().create().toJson(parameters)
        val args = bundleOf(ImagesFragment.ARG_PARAMETERS to gson)

        navController.navigate(R.id.imageDetailFragment, args)
    }
}