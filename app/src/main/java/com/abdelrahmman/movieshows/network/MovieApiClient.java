package com.abdelrahmman.movieshows.network;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.abdelrahmman.movieshows.AppExecutors;
import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.models.MoviesResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

import static com.abdelrahmman.movieshows.util.Constants.API_KEY;
import static com.abdelrahmman.movieshows.util.Constants.MAIN_NETWORK_TIMEOUT;
import static com.abdelrahmman.movieshows.util.Constants.NETWORK_TIMEOUT;

public class MovieApiClient {

    private static final String TAG = "MovieApiClient";

    private static MovieApiClient instance;
    private MutableLiveData<List<Movie>> movies;
    private MutableLiveData<Movie> movie;
    private RetrieveSearchedMoviesRunnable retrieveSearchedMoviesRunnable;
    private RetrieveMainMoviesRunnable retrieveMainMoviesRunnable;
    private RetrieveMovieRunnable retrieveMovieRunnable;
    private MutableLiveData<Boolean> requestTimeout = new MutableLiveData<>();

    public static MovieApiClient getInstance() {
        if (instance == null) {
            instance = new MovieApiClient();
        }
        return instance;
    }

    private MovieApiClient() {
        movies = new MutableLiveData<>();
        movie = new MutableLiveData<>();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    public LiveData<Boolean> isRequestTimedOut() {
        return requestTimeout;
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }

    public void searchMoviesApi(int pageNumber, String query) {
        if (retrieveSearchedMoviesRunnable != null){
            retrieveSearchedMoviesRunnable = null;
        }
        retrieveSearchedMoviesRunnable = new RetrieveSearchedMoviesRunnable(pageNumber, query);

        final Future handler = AppExecutors.getInstance().getNetworkIO().submit(retrieveSearchedMoviesRunnable);

        AppExecutors.getInstance().getNetworkIO().schedule(new Runnable() {
            @Override
            public void run() {
                handler.cancel(true);
            }
        }, NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void mainMoviesApi(int pageNumber) {
        if (retrieveMainMoviesRunnable != null){
            retrieveMainMoviesRunnable = null;
        }
        retrieveMainMoviesRunnable = new RetrieveMainMoviesRunnable(pageNumber);

        final Future handler = AppExecutors.getInstance().getNetworkIO().submit(retrieveMainMoviesRunnable);

        AppExecutors.getInstance().getNetworkIO().schedule(new Runnable() {
            @Override
            public void run() {
                handler.cancel(true);
            }
        }, MAIN_NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void searchMoviesById(int id) {
        if (retrieveMovieRunnable != null){
            retrieveMovieRunnable = null;
        }
        retrieveMovieRunnable = new RetrieveMovieRunnable(id);

        final Future handler = AppExecutors.getInstance().getNetworkIO().submit(retrieveMovieRunnable);

        requestTimeout.setValue(false);
        AppExecutors.getInstance().getNetworkIO().schedule(new Runnable() {
            @Override
            public void run() {
                requestTimeout.postValue(true);
                handler.cancel(true);
            }
        }, NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    //Search movies runnable
    private class RetrieveSearchedMoviesRunnable implements Runnable {

        private String query;
        private int pageNumber;
        private boolean cancelRequest;

        public RetrieveSearchedMoviesRunnable(int pageNumber, String query) {
            this.query = query;
            this.pageNumber = pageNumber;
            cancelRequest = false;
        }

        @Override
        public void run() {

            try {
                Response response = getMovies(pageNumber, query).execute();
                if (cancelRequest) {
                    return;
                }
                if (response.code() == 200) {
                    List<Movie> movieList = new ArrayList<>(((MoviesResponse) response.body()).getResults());
                    if(pageNumber == 1){
                        movies.postValue(movieList);
                    } else {
                        List<Movie> currentMovies = movies.getValue();
                        currentMovies.addAll(movieList);
                        movies.postValue(currentMovies);
                    }

                } else {
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    movies.postValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                movies.postValue(null);
            }
        }

        private Call<MoviesResponse> getMovies(int pageNumber, String query) {
            return RetrofitClient.getMoviesApi().searchMovies(
                    API_KEY,
                    pageNumber,
                    query
            );
        }

        private void cancelSearchRequest() {
            cancelRequest = true;
        }
    }

    //Main feed movies runnable
    private class RetrieveMainMoviesRunnable implements Runnable {

        private int pageNumber;
        private boolean cancelRequest;

        public RetrieveMainMoviesRunnable(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        @Override
        public void run() {

            try {
                Response response = getMovies(pageNumber).execute();
                if (cancelRequest) {
                    return;
                }
                if (response.code() == 200) {
                    List<Movie> movieList = new ArrayList<>(((MoviesResponse) response.body()).getResults());
                    if(pageNumber == 1){
                        movies.postValue(movieList);
                    } else {
                        List<Movie> currentMovies = movies.getValue();
                        currentMovies.addAll(movieList);
                        movies.postValue(currentMovies);
                    }

                } else {
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    movies.postValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                movies.postValue(null);
            }
        }

        private Call<MoviesResponse> getMovies(int pageNumber) {
            return RetrofitClient.getMoviesApi().mainMovies(
                    API_KEY,
                    pageNumber
            );
        }

        private void cancelRequest() {
            cancelRequest = true;
        }
    }

    //Movie details runnable
    private class RetrieveMovieRunnable implements Runnable {

        private int id;
        private boolean cancelRequest;

        public RetrieveMovieRunnable(int id) {
            this.id = id;
            cancelRequest = false;
        }

        @Override
        public void run() {

            try {
                Response response = getMovie(id).execute();
                if (cancelRequest) {
                    return;
                }
                if (response.code() == 200) {
                    Movie selectedMovie = (Movie) response.body();
                    movie.postValue(selectedMovie);

                } else {
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    movie.postValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                movie.postValue(null);
            }
        }

        private Call<Movie> getMovie(int id) {
            return RetrofitClient.getMoviesApi().movieDetails(
                    id,
                    API_KEY
            );
        }

        private void cancelRequest() {
            cancelRequest = true;
        }
    }

    public void cancelRequest(){
        if (retrieveSearchedMoviesRunnable != null){
            retrieveSearchedMoviesRunnable.cancelSearchRequest();
        }
        if (retrieveMovieRunnable != null){
            retrieveMovieRunnable.cancelRequest();
        }
    }

}
