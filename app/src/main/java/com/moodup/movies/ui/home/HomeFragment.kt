package com.moodup.movies.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movies.R
import com.moodup.movies.model.Movie
import com.moodup.movies.state.UIState
import com.moodup.movies.ui.details.DetailsFragment.Companion.MOVIE_KEY
import com.moodup.movies.utils.adapter.MoviesAdapter
import com.moodup.movies.viewmodel.MovieViewModel
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var viewModel: MovieViewModel? = null
    private var adapter: MoviesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpSearchView()
        setUpRecyclerView()

        activity?.let {
            viewModel = ViewModelProvider(it).get(MovieViewModel::class.java)
        }

        observeLiveData()

    }

    private fun setUpRecyclerView() {
        linearLayoutManager = LinearLayoutManager(activity)
        movies_recycler_view.layoutManager = linearLayoutManager

        movies_recycler_view.addOnScrollListener (object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalItemCount: Int = linearLayoutManager.itemCount
                val lastVisibleItem: Int = linearLayoutManager.findLastVisibleItemPosition()

                if (!viewModel?.isDataLoading!!  && lastVisibleItem == totalItemCount - 1) {
                    viewModel?.isDataLoading = true
                    adapter?.showFooterProgressBar()
                    viewModel?.getMovies(totalItemCount+1, movie_searchview.query.toString())
                }
            }
        })
    }

        private fun setUpSearchView() {
            movie_searchview.queryHint = context?.getString(R.string.search_for_a_movie)
            movie_searchview.isIconified = false
            movie_searchview.setBackgroundColor(Color.LTGRAY)
            movie_searchview.clearFocus()

            movie_searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextChange(query: String): Boolean {
                    //viewModel?.getMovies(query)
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    //viewModel?.getMovies(query)
                    return false
                }

            })

        }

        private fun setUpAdapter(movies: List<Movie>) {
            adapter = MoviesAdapter()
            adapter?.updateAdapter(movies)
            movies_recycler_view.adapter = adapter
            movies_recycler_view.addItemDecoration(
                HorizontalDividerItemDecoration.Builder(context).color(
                    Color.DKGRAY
                ).sizeResId(R.dimen.divider).marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                    .build()
            )

            adapter!!.onItemClick = {
                val bundle = Bundle()
                bundle.putSerializable(MOVIE_KEY, it)
                findNavController().navigate(R.id.action_homeFragment_to_detailsFragment, bundle)
            }
        }

        private fun observeLiveData() {
            viewModel?.movieLiveData?.observe(viewLifecycleOwner, Observer {
                updateAdapter(it)
                viewModel?.isDataLoading = false
            })

            viewModel?.UIstateLiveData?.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    UIState.LOADING -> {
                        showProgressBar()
                    }
                    UIState.ON_ERROR -> {
                        showOnError()
                    }
                    UIState.ON_RESULT -> {
                        hideProgressBar()
                    }
                    UIState.ON_EMPTY_RESULTS -> {
                        showEmptyResults()
                    }
                    UIState.INITIALIZED -> {
                        viewModel?.UIstateLiveData?.postValue (UIState.LOADING)
                        viewModel?.getMovies(0, "")
                    }

                }
            })
        }

        private fun updateAdapter(movies: List<Movie>) {
            //setUpAdapter(movies)
            //todo: on details page and on back reset of adapter?

            if (adapter == null) {
                setUpAdapter(movies)
            } else {
                adapter!!.updateAdapter(movies)
            }
        }

        private fun showEmptyResults() {
            movies_recycler_view.visibility = View.GONE
            results_textView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }

        private fun showOnError() {
            movies_recycler_view.visibility = View.GONE
            results_textView.visibility = View.VISIBLE
            results_textView.text = R.string.error.toString()
            progressBar.visibility = View.GONE
        }

        private fun showProgressBar() {
            movies_recycler_view.visibility = View.GONE
            results_textView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        }

        private fun hideProgressBar() {
            movies_recycler_view.visibility = View.VISIBLE
            results_textView.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }