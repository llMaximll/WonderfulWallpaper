package com.github.llmaximll.wonderfulwallpaper.app.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.github.llmaximll.wonderfulwallpaper.R
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail.ImageDetailFragment
import com.github.llmaximll.wonderfulwallpaper.app.ui.images.ImagesFragment
import com.github.llmaximll.wonderfulwallpaper.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    ImagesFragment.Callbacks,
    ImageDetailFragment.Callbacks {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    val navController: NavController
        get() = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

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
                        binding.motionLayout.transitionToStart()
                    } else {
                        binding.motionLayout.transitionToEnd()
                    }
                }
            }
        }
    }

    // Callbacks

    override fun onItemClicked(image: Image) {
        val gson = GsonBuilder().create().toJson(image)
        val args = bundleOf(ImagesFragment.ARG_IMAGE to gson)

        findNavController(R.id.nav_host_fragment).navigate(R.id.action_imagesFragment_to_imageDetailFragment, args)
    }

    override fun onImageDetailFragment(exit: Boolean) {
        viewModel.toggleMainState(state = exit)
    }
}