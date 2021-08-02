package com.github.llmaximll.wonderfulwallpaper.app.ui.images

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import com.github.llmaximll.wonderfulwallpaper.databinding.FragmentImagesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ImagesFragment : Fragment() {

    private var _binding: FragmentImagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImagesViewModel by viewModels()
    private lateinit var adapter: ImagesAdapter

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

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = ImagesAdapter()
        binding.imagesRV.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.imagesRV.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getImages(requireContext()).observe(viewLifecycleOwner) {
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            binding.imagesRV.visibility = View.GONE
                            binding.progressBar.visibility = View.VISIBLE
                            Timber.v("loading")
                        }
                        Resource.Status.SUCCESS -> {
                            binding.imagesRV.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE
                            if (!it.data.isNullOrEmpty()) adapter.setItems(it.data)
                            Timber.v("success | ${it.data?.size}")
                        }
                        Resource.Status.ERROR -> {
                            Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}