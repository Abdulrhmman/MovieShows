package com.abdelrahmman.movieshows.repositories;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.network.MovieApiClient;

import java.util.List;

import static com.abdelrahmman.movieshows.util.Constants.QUERY_SIZE;

public class MovieRepository {

    private static MovieRepository instance;
    private MovieApiClient movieApiClient;
    private String query;
    private int pageNumber;
    private MutableLiveData<Boolean> isQueryExhausted = new MutableLiveData<>();
    private MediatorLiveData<List<Movie>> movies = new MediatorLiveData<>();

    public static MovieRepository getInstance(){
        if(instance == null){
            instance = new MovieRepository();
        }
        return instance;
    }

    private MovieRepository(){
        movieApiClient = MovieApiClient.getInstance();
        initMediators();
    }

    private void initMediators(){
        LiveData<List<Movie>> movieListApiSource = movieApiClient.getMovies();
        movies.addSource(movieListApiSource, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> moviesList) {

                if(moviesList != null){
                    movies.setValue(moviesList);
                    doneQuery(moviesList);
                }
                else{
                    doneQuery(null);
                }
            }
        });
    }

    private void doneQuery(List<Movie> list){
        if(list != null){
            if (list.size() % QUERY_SIZE != 0) {
                isQueryExhausted.setValue(true);
            }
        }
        else{
            isQueryExhausted.setValue(true);
        }
    }

    public LiveData<Boolean> isQueryExhausted(){
        return isQueryExhausted;
    }

    public LiveData<List<Movie>> getMovies(){
        return movies;
    }

    public LiveData<Movie> getMovie(){
        return movieApiClient.getMovie();
    }

    public void searchMovieById(int id){
        movieApiClient.searchMoviesById(id);
    }

    public void mainMoviesApi(int pageNumber){
        if(pageNumber == 0){
            pageNumber = 1;
        }
        this.pageNumber = pageNumber;
        isQueryExhausted.setValue(false);
        movieApiClient.mainMoviesApi(pageNumber);
    }

    public void mainNextPage(){
        mainMoviesApi(pageNumber + 1);
    }

    public void searchMoviesApi(String query, int pageNumber){
        if(pageNumber == 0){
            pageNumber = 1;
        }
        this.query = query;
        this.pageNumber = pageNumber;
        isQueryExhausted.setValue(false);
        movieApiClient.searchMoviesApi(pageNumber, query);
    }

    public void searchNextPage(){
        searchMoviesApi(query, pageNumber + 1);
    }

    public void cancelRequest(){
        movieApiClient.cancelRequest();
    }

    public LiveData<Boolean> isRequestTimedOut(){
        return movieApiClient.isRequestTimedOut();
    }

}
