package com.moodup.movies.ui.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.movies.R
import com.moodup.movies.model.Movie
import com.moodup.movies.state.AddedToDatabaseState
import com.moodup.movies.viewmodel.DetailsViewModel
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_details.movie_title

class DetailsFragment : Fragment() {
    companion object {
        const val MOVIE_KEY = "MOVIE_KEY"
    }

    private var viewModel: DetailsViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val movie = arguments?.getSerializable(MOVIE_KEY)
        setDataIntoFields(movie as Movie)


        activity?.let {
            viewModel = ViewModelProvider(it).get(DetailsViewModel::class.java)
        }
        observeLiveData()

        add_to_favourites_btn.setOnClickListener {
            viewModel?.checkIfMovieIsInDatabase(movie)
        }
    }


    private fun setDataIntoFields(movie: Movie) {

        movie_title.text = if(movie.title == null || movie.title.isEmpty()) getString(R.string.no_title) else  movie.title
        movie_description.text = if(movie.description ==null || movie.description.isEmpty()) getString(R.string.no_description) else movie.description
        movie_page_count.text =  if(movie.pageCount == null || movie.pageCount.toString().isEmpty()) getString(R.string.no_page_count) else movie.pageCount.toString()
        movie_format.text = if(movie.format == null || movie.format.isEmpty()) getString(R.string.no_format) else movie.format


        Glide.with(this)
            .load("${movie.thumbnail.path}.${movie.thumbnail.extension}")
            .into(movie_picture_details)
    }

    private fun observeLiveData() {

        viewModel?.databaseState?.observe(viewLifecycleOwner, Observer { database ->
            when (database) {
                AddedToDatabaseState.ADDED_SUCCESS -> {
                    showSuccessAddedMessage()
                    disableAddToFavouritesButton()
                }
                AddedToDatabaseState.REMOVED_SUCCESS -> {
                    showSuccessRemovedMessage()
                    enableAddToFavouritesButton()
                }

                AddedToDatabaseState.FAILURE -> {
                    showFailureAddedMessage()
                }
                AddedToDatabaseState.NO_DOCUMENT -> {
                    showNoDocumentMessage()
                }
                AddedToDatabaseState.DEFAULT -> {

                }

            }
        })


    }

    private fun enableAddToFavouritesButton(){
        add_to_favourites_btn.text = context?.resources?.getString(R.string.add_to_favourites)
        context?.resources?.getColor(R.color.green)?.let { add_to_favourites_btn.setBackgroundColor(it) }
    }

    private fun disableAddToFavouritesButton(){
        add_to_favourites_btn.text = context?.resources?.getString(R.string.remove_from_favourites)
        context?.resources?.getColor(R.color.red)?.let { add_to_favourites_btn.setBackgroundColor(it) }
    }


    private fun showFailureAddedMessage() {
        Toast.makeText(
            context,
            context?.resources?.getString(R.string.add_failure),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSuccessAddedMessage() {
        Toast.makeText(
            context,
            context?.resources?.getString(R.string.add_success),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSuccessRemovedMessage() {
        Toast.makeText(
            context,
            context?.resources?.getString(R.string.remove_success),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showNoDocumentMessage() {
        Toast.makeText(
            context,
            context?.resources?.getString(R.string.no_document),
            Toast.LENGTH_SHORT
        ).show()
    }
}