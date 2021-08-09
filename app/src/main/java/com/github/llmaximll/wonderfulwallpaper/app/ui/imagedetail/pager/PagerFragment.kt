package com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail.pager

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.databinding.FragmentPagerImageDetailBinding
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class PagerFragment : Fragment() {

    private var _binding: FragmentPagerImageDetailBinding? = null
    private val binding get() =  _binding!!
    private lateinit var image: Image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            image = Gson().fromJson(arguments?.getString(ARG_IMAGE_JSON) ?: "", Image::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagerImageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(binding.root)
            .load(image.largeImageURL)
            .thumbnail(
                Glide.with(binding.root)
                    .load(image.previewURL)
            )
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(Glide.with(binding.root).load(image.webFormatURL.replace("_640", "_340")))
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.hide()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(image: String): PagerFragment {
            val args = bundleOf(
                ARG_IMAGE_JSON to image
            )
            return PagerFragment().apply {
                arguments = args
            }
        }
        private const val ARG_IMAGE_JSON = "arg_image_json"
    }
}