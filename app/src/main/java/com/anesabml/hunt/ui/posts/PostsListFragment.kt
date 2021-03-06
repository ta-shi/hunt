package com.anesabml.hunt.ui.posts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.anesabml.hunt.R
import com.anesabml.hunt.databinding.FragmentPostsListBinding
import com.anesabml.hunt.model.Post
import com.anesabml.hunt.ui.posts.adapter.PostsListRecyclerViewAdapter
import com.anesabml.hunt.ui.posts.adapter.PostsLoadStateAdapter
import com.anesabml.lib.extension.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostsListFragment : Fragment(R.layout.fragment_posts_list), PostsListInteractions {

    companion object {
        const val ARG_SORT_BY = "sort_by"

        @JvmStatic
        fun newInstance(sortBy: String) =
            PostsListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SORT_BY, sortBy)
                }
            }
    }

    private val binding by viewBinding(FragmentPostsListBinding::bind)
    private lateinit var _adapter: PostsListRecyclerViewAdapter

    private val viewModel: PostsListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _adapter = PostsListRecyclerViewAdapter(this@PostsListFragment)
        _adapter.withLoadStateFooter(PostsLoadStateAdapter(_adapter::retry))

        // Setup the RecyclerView
        binding.postsRecyclerView.adapter = _adapter

        // Setup the SwipeRefresh callback
        binding.swipeRefresh.setOnRefreshListener {
            _adapter.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.postsPagingFlow.collectLatest { pagingData ->
                _adapter.submitData(pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            _adapter.loadStateFlow.collectLatest { loadState ->
                binding.swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading
            }
        }
    }

    override fun onClickPost(itemView: View, post: Post) {
        val extras = FragmentNavigatorExtras(itemView to post.id)
        val action =
            PostsFragmentDirections.actionPostsFragmentToPostDetailsFragment(post)
        findNavController().navigate(action, extras)
    }
}
