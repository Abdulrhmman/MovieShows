package com.abdelrahmman.movieshows.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.repositories.MovieRepository;

import java.util.List;

public class MoviesViewModel extends ViewModel {

    private MovieRepository movieRepository;
    private boolean isViewingSearchedMovies;
    private boolean isPerformingQuery;

    public MoviesViewModel() {
        movieRepository = MovieRepository.getInstance();
    }

    public LiveData<List<Movie>> getMovies() {
        return movieRepository.getMovies();
    }

    public LiveData<Boolean> isQueryExhausted() {
        return movieRepository.isQueryExhausted();
    }

    public void searchMoviesApi(String query, int pageNumber) {
        isViewingSearchedMovies = true;
        isPerformingQuery = true;
        movieRepository.searchMoviesApi(query, pageNumber);
    }

    public void searchNextPage() {
        if (!isPerformingQuery && isViewingSearchedMovies && !isQueryExhausted().getValue()) {
            movieRepository.searchNextPage();
        }
    }

    public void mainMoviesApi(int pageNumber) {
        movieRepository.mainMoviesApi(pageNumber);
    }

    public void mainNextPage() {
        if (!isPerformingQuery && !isViewingSearchedMovies) {
            movieRepository.mainNextPage();
        }
    }

    public boolean isViewingSearchedMovies() {
        return isViewingSearchedMovies;
    }

    public void setIsViewingSearchedMovies(boolean isViewingSearchedMovies) {
        this.isViewingSearchedMovies = isViewingSearchedMovies;
    }

    public void setIsPerformingQuery(boolean isPerformingQuery) {
        this.isPerformingQuery = isPerformingQuery;
    }

    public boolean onBackPressed(){
        if (isPerformingQuery){
            movieRepository.cancelRequest();
            isPerformingQuery = false;
        }
        if (isViewingSearchedMovies){
            isViewingSearchedMovies = false;
            return false;
        }
        return true;
    }
}
