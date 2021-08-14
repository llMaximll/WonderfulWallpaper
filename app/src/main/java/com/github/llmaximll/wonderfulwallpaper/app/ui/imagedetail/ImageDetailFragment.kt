package com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.github.llmaximll.wonderfulwallpaper.R
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.ImageFavorite
import com.github.llmaximll.wonderfulwallpaper.app.ui.images.ImagesFragment
import com.github.llmaximll.wonderfulwallpaper.app.ui.main.MainActivity
import com.github.llmaximll.wonderfulwallpaper.databinding.FragmentImageDetailBinding
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ImageDetailFragment : Fragment() {

    interface Callbacks {
        fun onImageDetailFragment(exit: Boolean)
    }

    private var _binding: FragmentImageDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImageDetailViewModel by viewModels()
    private var callbacks: Callbacks? = null
    private var image: Image? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbacks?.onImageDetailFragment(false)
        try {
            image = Gson().fromJson(arguments?.getString(ImagesFragment.ARG_IMAGE), Image::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setImageToImageView()
        setupObservers()
        restoreStateViews()
    }

    override fun onStart() {
        super.onStart()
        binding.backButton.setOnClickListener {
            callbacks?.onImageDetailFragment(true)
            (requireActivity() as MainActivity).navController.popBackStack()
        }
        binding.imageView.setOnClickListener {
            viewModel.toggleButtonsState()
        }
        binding.favoriteImageButton.setOnClickListener {
            viewModel.toggleFavoriteState()
        }
    }

    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                callbacks?.onImageDetailFragment(true)
                (requireActivity() as MainActivity).navController.popBackStack()
            }
        })
    }

    private fun setImageToImageView() {
        Glide.with(this)
            .load(image?.largeImageURL)
            .thumbnail(
                Glide.with(binding.root)
                    .load(image?.previewURL)
            )
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(Glide.with(binding.root).load(image?.webFormatURL?.replace("_640", "_340")))
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.hide()
                    return false
                }
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.hide()
                    return false
                }
            })
            .into(binding.imageView)
            .clearOnDetach()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.buttonsState.collect { state ->
                        if (state) {
                            binding.backButton.visibility = View.VISIBLE
                            binding.favoriteImageButton.visibility = View.VISIBLE
                            binding.includeBottomSheet.bottomSheet.visibility = View.VISIBLE
                        } else {
                            binding.backButton.visibility = View.GONE
                            binding.favoriteImageButton.visibility = View.GONE
                            binding.includeBottomSheet.bottomSheet.visibility = View.GONE
                        }
                    }
                }
                launch {
                    viewModel.favoriteState.collect { state ->
                        if (state) {
                            if (image?.id != null) {
                                withContext(Dispatchers.IO) {
                                    viewModel.insertFavoriteImage(ImageFavorite(id = image!!.id))
                                }
                                binding.favoriteImageButton.setImageResource(R.drawable.ic_favorite_selected)
                            }
                        } else {
                            if (image?.id != null) {
                                withContext(Dispatchers.IO) {
                                    viewModel.deleteFavoriteImages(listOf(ImageFavorite(image!!.id)))
                                }
                                binding.favoriteImageButton.setImageResource(R.drawable.ic_favorite)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun restoreStateViews() {
        viewLifecycleOwner.lifecycleScope.launch {
            val favoriteImage = viewModel.getFavoriteImage("${image?.id}")
            if (favoriteImage != null) {
                viewModel.toggleFavoriteState(true)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}