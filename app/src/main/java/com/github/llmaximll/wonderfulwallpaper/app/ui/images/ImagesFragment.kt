package com.github.llmaximll.wonderfulwallpaper.app.ui.images

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.wonderfulwallpaper.R
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Parameters
import com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail.ImageDetailFragment
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import com.github.llmaximll.wonderfulwallpaper.databinding.FragmentImagesBinding
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import timber.log.Timber

@AndroidEntryPoint
class ImagesFragment : Fragment() {

    interface Callbacks {
        fun onItemClicked(parameters: Parameters)
    }

    private var _binding: FragmentImagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImagesViewModel by viewModels()
    private lateinit var collector: FlowCollector<Resource<List<Image>>>
    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolBar()
        setupObservers()
        setupRecyclerView()
//        setupFragmentResultListener()
    }

    override fun onStart() {
        super.onStart()
        binding.clickableImageView.setOnClickListener { viewModel.toggleMainState(true) }
    }

    private fun setupRecyclerView() {
        if (viewModel.adapter == null) viewModel.adapter = ImagesAdapter(callbacks, viewModel)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.imagesRV.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            binding.imagesRV.layoutManager = GridLayoutManager(requireContext(), 4)
        }
        binding.imagesRV.adapter = viewModel.adapter
        binding.imagesRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!viewModel.loadingNewPageFlag &&
                    !binding.imagesRV.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.loadingNewPageFlag = true // Флаг, запрещающий подгружать новые страницы
                        viewModel.page++
                        collector.emitAll(viewModel.getImages(requireContext()))
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupToolBar() {
        binding.progressBar.setVisibilityAfterHide(View.INVISIBLE)
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.adapter = ImagesAdapter(callbacks, viewModel)
                binding.imagesRV.adapter = viewModel.adapter
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.q = binding.searchEditText.text.toString().replace(" ", "+")
                    viewModel.page = 1
                    collector.emitAll(viewModel.getImages(requireContext(), viewModel.page))
                }
            }
            false
        }
        binding.filterImageView.setOnClickListener {
            viewModel.toggleMainState()
        }
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener(ImageDetailFragment.REQUEST_KEY_RESULT_IMAGE_LIST) { _, bundle ->
            val itemType = object : TypeToken<List<Image>>() {}.type
            val imageList = Gson().fromJson<List<Image>>(bundle.getString(ImageDetailFragment.KEY_IMAGE_LIST), itemType)
            viewModel.currentPosition = bundle.getInt(ImageDetailFragment.KEY_CURRENT_POSITION)
            val page = bundle.getInt(ImageDetailFragment.KEY_PAGE)
            viewModel.transitionToNewPositionFlag = true
            val newItems = imageList.toMutableList()
            Timber.v("imageList.size(1)=${newItems.size}")
            newItems.removeAll(viewModel.adapter?.items!!)
            Timber.v("imageList.size(2)=${newItems.size}")
            viewModel.adapter?.addItems(newItems)
            viewModel.page = page
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    collector = object : FlowCollector<Resource<List<Image>>> {
                        override suspend fun emit(value: Resource<List<Image>>) {
                            when (value.status) {
                                Resource.Status.LOADING -> {
                                    withContext(Dispatchers.Main) {
                                        binding.progressBar.show()
                                    }
                                    Timber.v("loading")
                                }
                                Resource.Status.SUCCESS -> {
                                    withContext(Dispatchers.Main) {
                                        if (!value.data.isNullOrEmpty() &&
                                            value.data[1].id != viewModel.adapter?.recentItems?.getOrNull(1)?.id
                                        ) {
                                            viewModel.adapter?.apply {
                                                addItems(value.data)
                                                recentItems.clear()
                                                recentItems.addAll(value.data)
                                                viewModel.loadingNewPageFlag = false // Разрешение для погрузки новой страницы
                                                Timber.v("Элементы добавлены")
                                            }
                                        }
                                        binding.progressBar.hide()
                                    }
                                    Timber.v("success | ${value.data?.size}")
                                }
                                Resource.Status.ERROR -> {
                                    withContext(Dispatchers.Main) {
                                        binding.progressBar.hide()
                                        viewModel.loadingNewPageFlag = false // Разрешение для погрузки новой страницы
                                        Toast.makeText(requireContext(), "${value.message}", Toast.LENGTH_LONG).show()
                                    }
                                    Timber.v("Ошибка загрузки данных | ${value.message}")
                                }
                            }
                        }
                    }
                }
                launch(Dispatchers.Main) {
                    viewModel.mainState.collect { state ->
                        if (!state) {
                            binding.motionLayout.transitionToState(R.id.end)
                            binding.clickableImageView.animate().apply {
                                withStartAction { binding.clickableImageView.visibility = View.VISIBLE }
                                alpha(0.7f)
                            }
                        } else {
                            binding.motionLayout.transitionToState(R.id.start)
                            binding.clickableImageView.animate().apply {
                                alpha(0.0f)
                                withEndAction { binding.clickableImageView.visibility = View.GONE }
                            }
                            val chipGroupList: List<ChipGroup>
                            binding.includeFilter.run {
                                chipGroupList = listOf(
                                    imageTypeGroup,
                                    orientationGroup,
                                    categoryGroup,
                                    colorsGroup,
                                    choiceGroup
                                )
                            }
                            if (viewModel.checkChangeState(chipGroupList)) {
                                viewModel.adapter = ImagesAdapter(callbacks, viewModel)
                                binding.imagesRV.adapter = viewModel.adapter
                                viewModel.page = 1
                                viewModel.setParamListFromTags(chipGroupList)
                                Timber.v("""
                                imageType=${viewModel.imageType}
                                orientation=${viewModel.orientation}
                                category=${viewModel.category}
                                colors=${viewModel.colors}
                                editorsChoice=${viewModel.editorsChoice}
                                """.trimIndent())
                                collector.emitAll(viewModel.getImages(requireContext()))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
        _binding = null
    }

    companion object {
        const val ARG_PARAMETERS = "arg_parameters"
    }
}