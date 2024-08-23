package com.movieflix.movieAPI.service;

import com.movieflix.movieAPI.Responsitory.MovieResponsitory;
import com.movieflix.movieAPI.dto.MovieDto;
import com.movieflix.movieAPI.entity.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements  MovieService{

    private final MovieResponsitory movieResponsitory;

    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieResponsitory movieResponsitory, FileService fileService){
        this.movieResponsitory = movieResponsitory;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        //1.Upload the file
        String uploadedFileName =fileService.uploadFile(path, file);
        //2. set the value of field 'poster' as filename
        movieDto.setPoster(uploadedFileName);
        //3. map dto to Movie object
        Movie movie =  new Movie(
                movieDto.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        //4. save the Movie object
        Movie savedMovie = movieResponsitory.save(movie);
        //5. generate the postUrl
        String posterUrl = baseUrl + "/file/" + uploadedFileName;

        //6. map Movie object to DTP object and return it
        MovieDto response = new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
        return response;
    };

    @Override
    public MovieDto getMovie(Integer movieId){
        //1.check the data in DB and if exists, fetch the data of given ID
        Movie movie =  movieResponsitory.findById(movieId).orElseThrow(
                () -> new RuntimeException("Movie not found!")
        );

        //2. generate postUrl
        String posterUrl = baseUrl + "/file/" + movie.getPoster();

        //3. map to MovieID object and return it
        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return response;
    };

    @Override
    public List<MovieDto> getAllMovies(){
        //1. fetch all data from DB
        List<Movie> movies = movieResponsitory.findAll();

        List<MovieDto> movieDtos = new ArrayList<>();

        //2. iterate through the list, generate posterUrl for each movie obj
        //and map to MovieDto obj
        for(Movie movie: movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return movieDtos;
    };
}
