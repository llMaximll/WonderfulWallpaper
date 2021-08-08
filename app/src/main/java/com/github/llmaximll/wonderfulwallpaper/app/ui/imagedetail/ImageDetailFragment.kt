package com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Parameters
import com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail.pager.PagerAdapter
import com.github.llmaximll.wonderfulwallpaper.app.ui.images.ImagesFragment
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import com.github.llmaximll.wonderfulwallpaper.databinding.FragmentImageDetailBinding
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class ImageDetailFragment : Fragment() {

    private var _binding: FragmentImageDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImageDetailViewModel by viewModels()
    private lateinit var collector: FlowCollector<Resource<List<Image>>>
    private lateinit var imageList: List<Image>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val parameters = Gson().fromJson(arguments?.getString(ImagesFragment.ARG_PARAMETERS), Parameters::class.java)
            viewModel.run {
                page = parameters.page
                q = parameters.q
                imageType = parameters.imageType
                orientation = parameters.orientation
                category.addAll(parameters.category)
                colors.addAll(parameters.colors)
                editorsChoice = parameters.editorsChoice
            }
            viewModel.positionImage = parameters.positionImage
            parameters.items?.let { imageList = it }
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

        setupObservers()
        setupViewPager()
    }

    private fun setupViewPager() {
        if (viewModel.adapter == null) {
            viewModel.adapter = PagerAdapter(this)
            viewModel.adapter?.addItems(imageList)
            binding.pager.adapter = viewModel.adapter
            binding.pager.setCurrentItem(viewModel.positionImage, false)
            Timber.v("positionImage=${viewModel.positionImage}")
        } else {
            binding.pager.adapter = viewModel.adapter
            Timber.v("items=${viewModel.adapter?.items}")
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collector = object : FlowCollector<Resource<List<Image>>> {
                    override suspend fun emit(value: Resource<List<Image>>) {
                        when (value.status) {
                            Resource.Status.LOADING -> {
                                Timber.v("loading")
                            }
                            Resource.Status.SUCCESS -> {
                                withContext(Dispatchers.Main) {
                                    if (
                                        !value.data.isNullOrEmpty() &&
                                        value.data[1].id != viewModel.adapter?.recentItems?.getOrNull(1)?.id
                                    ) {
                                        viewModel.adapter?.apply {
                                            addItems(value.data)
                                            recentItems.clear()
                                            recentItems.addAll(value.data)
                                            Timber.v("Элементы добавлены")
                                        }
                                    }
                                }
                                Timber.v("success | ${value.data?.size}")
                            }
                            else -> {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "${value.message}", Toast.LENGTH_LONG).show()
                                }
                                Timber.v("Ошибка загрузки данных | ${value.message}")
                            }
                        }
                    }
                }
//                viewModel.getImages(requireContext()).collect { collector.emit(it) } TODO
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.pager.adapter = null
        _binding = null
    }
}