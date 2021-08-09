package com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Parameters
import com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail.pager.PagerAdapter
import com.github.llmaximll.wonderfulwallpaper.app.ui.images.ImagesFragment
import com.github.llmaximll.wonderfulwallpaper.app.ui.main.MainActivity
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import com.github.llmaximll.wonderfulwallpaper.databinding.FragmentImageDetailBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class ImageDetailFragment : Fragment() {

    interface Callbacks {
        fun onImageDetailFragment(exit: Boolean)
    }

    private var _binding: FragmentImageDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImageDetailViewModel by viewModels()
    private lateinit var collector: FlowCollector<Resource<List<Image>>>
    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

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
            viewModel.currentItem = parameters.positionImage
            parameters.items?.let { viewModel.imageList = it.toMutableList() }
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        }
        callbacks?.onImageDetailFragment(false)
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
        setupObservers()
        setupViewPager()
    }

    private fun setupViewPager() {
        viewModel.adapter = PagerAdapter(this)
        viewModel.adapter?.addItems(viewModel.imageList)
        binding.pager.adapter = viewModel.adapter
        binding.pager.setCurrentItem(viewModel.currentItem, false)
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (!viewModel.loadingNewPageFlag &&
                    !binding.pager.canScrollHorizontally(RecyclerView.HORIZONTAL) &&
                    state == RecyclerView.SCROLL_STATE_IDLE) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.loadingNewPageFlag = true // Флаг, запрещающий подгружать новые страницы
                        viewModel.page++
                        collector.emitAll(viewModel.getImages(requireContext()))
                    }
                }
            }
        })
        Timber.v("currentItem=${binding.pager.currentItem}")
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
                                            viewModel.imageList.addAll(value.data)
                                            viewModel.loadingNewPageFlag = false // Разрешение для погрузки новой страницы
                                            Timber.v("Элементы добавлены")
                                        }
                                    }
                                }
                                Timber.v("success | ${value.data?.size}")
                            }
                            Resource.Status.ERROR -> {
                                withContext(Dispatchers.Main) {
                                    viewModel.loadingNewPageFlag = false // Разрешение для погрузки новой страницы
                                    Toast.makeText(requireContext(), "${value.message}", Toast.LENGTH_LONG).show()
                                }
                                Timber.v("Ошибка загрузки данных | ${value.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.currentItem = binding.pager.currentItem
    }

    override fun onDetach() {
        super.onDetach()
        val gson = GsonBuilder().create()
        val imageListGson = gson.toJson(viewModel.imageList.toList())
        setFragmentResult(REQUEST_KEY_RESULT_IMAGE_LIST, bundleOf(
            KEY_IMAGE_LIST to imageListGson,
            KEY_CURRENT_POSITION to viewModel.currentItem,
            KEY_PAGE to viewModel.page
        ))
        callbacks = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Result API
        const val REQUEST_KEY_RESULT_IMAGE_LIST = "request_key_result_image_list"
        const val KEY_IMAGE_LIST = "key_image_view"
        const val KEY_CURRENT_POSITION = "key_current_position"
        const val KEY_PAGE = "key_page"
    }
}