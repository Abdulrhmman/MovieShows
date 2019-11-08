package com.abdelrahmman.movieshows.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.repositories.MovieRepository;

public class MovieDetailsViewModel extends ViewModel {

    private MovieRepository movieRepository;
    private int movieId;
    private boolean didRetrieveMovie;

    public MovieDetailsViewModel() {
        movieRepository = MovieRepository.getInstance();
        didRetrieveMovie = false;
    }

    public LiveData<Movie> getMovie(){
        return movieRepository.getMovie();
    }

    public LiveData<Boolean> isRequestTimedOut() {
        return movieRepository.isRequestTimedOut();
    }

    public void searchMovieById(int id){
        movieId = id;
        movieRepository.searchMovieById(id);
    }

    public int getMovieId(){
        return movieId;
    }

    public void setDidRetrieveMovie(boolean didRetrieveMovie){
        this.didRetrieveMovie = didRetrieveMovie;
    }

    public boolean didRetrieveMovie(){
        return didRetrieveMovie;
    }
}
