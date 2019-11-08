package com.abdelrahmman.movieshows.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abdelrahmman.movieshows.BaseActivity;
import com.abdelrahmman.movieshows.R;
import com.abdelrahmman.movieshows.adapters.MovieRecyclerAdapter;
import com.abdelrahmman.movieshows.adapters.OnMovieClickListener;
import com.abdelrahmman.movieshows.models.Movie;
import com.abdelrahmman.movieshows.viewmodels.MoviesViewModel;
import com.abdelrahmman.movieshows.util.SpacingItemDecorator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class MoviesActivity extends BaseActivity implements OnMovieClickListener {

    private MoviesViewModel moviesViewModel;
    private RecyclerView recyclerView;
    private MovieRecyclerAdapter adapter;
    private ProgressBar progressBar;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        searchView = findViewById(R.id.search_view);

        moviesViewModel = ViewModelProviders.of(this).get(MoviesViewModel.class);

        initRecyclerView();
        subscribeObservers();
        initSearchView();
        if (!moviesViewModel.isViewingSearchedMovies()) {
            displayMainMovies();
        }
    }

    private void subscribeObservers() {
        moviesViewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (movies != null) {
                    moviesViewModel.setIsPerformingQuery(false);
                    adapter.setMovies(movies);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        moviesViewModel.isQueryExhausted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    adapter.setQueryExhausted();
                }
            }
        });
    }

    private void initRecyclerView() {
        adapter = new MovieRecyclerAdapter(this, initGlide());
        SpacingItemDecorator itemDecorator = new SpacingItemDecorator(20, 10);
        recyclerView.addItemDecoration(itemDecorator);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView1, int newState) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (moviesViewModel.isViewingSearchedMovies()) {
                        moviesViewModel.searchNextPage();
                    } else {
                        moviesViewModel.mainNextPage();
                    }
                }
            }
        });
    }

    private RequestManager initGlide() {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.black_background)
                .error(R.drawable.black_background);

        return Glide.with(this).setDefaultRequestOptions(requestOptions);
    }

    private void initSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                progressBar.setVisibility(View.VISIBLE);
                moviesViewModel.searchMoviesApi(query, 1);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onMovieClick(int position) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("movie", adapter.getSelectedMovie(position));
        startActivity(intent);
    }

    private void displayMainMovies() {
        moviesViewModel.setIsViewingSearchedMovies(false);
        moviesViewModel.mainMoviesApi(1);
    }

    @Override
    public void onBackPressed() {
        if (moviesViewModel.onBackPressed()) {
            super.onBackPressed();
        } else {
            displayMainMovies();
        }
    }
}
