package com.movieflix.movieAPI.service;

import com.movieflix.movieAPI.Responsitory.MovieResponsitory;
import com.movieflix.movieAPI.dto.MovieDto;
import com.movieflix.movieAPI.dto.MoviePageResponse;
import com.movieflix.movieAPI.entity.Movie;
import com.movieflix.movieAPI.exeption.FileExistsException;
import com.movieflix.movieAPI.exeption.MovieNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        if(Files.exists(Paths.get(path + File.separator + file.getOriginalFilename())))
            throw new FileExistsException("File already exists! Please enter another file name!");

        String uploadedFileName =fileService.uploadFile(path, file);
        //2. set the value of field 'poster' as filename
        movieDto.setPoster(uploadedFileName);
        //3. map dto to Movie object
        Movie movie =  new Movie(
                null,
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
                () -> new MovieNotFoundException("Movie not found with id ="+ movieId)
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

    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException{
        //1. check if movie object exists with given movieId
        Movie mv =  movieResponsitory.findById(movieId).orElseThrow(
                () -> new MovieNotFoundException("Movie not found with id ="+ movieId)
        );

        //2. if file is null, do nothing
        //if file is not null, then delete existing file associated with the record
        //and upload the new file
        String fileName = mv.getPoster();
        if(file != null) {
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }

        //3.set movieDtp's poster value, acccording to Step 2
        movieDto.setPoster(fileName);
        //4. map it to Movie object
        Movie movie = new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        //5. save the movie object -> return saved movie object
        Movie updateMovie = movieResponsitory.save(movie);

        //6. Generate posterUrl for it
        String posterUrl = baseUrl + "/file/" +  fileName;

        //7. map to Dto and return it
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

    public String deleteMovie(Integer movieId) throws IOException{

        //1. check if movie object exists in DB
        Movie mv =  movieResponsitory.findById(movieId).orElseThrow(
                () -> new MovieNotFoundException("Movie not found with id ="+ movieId)
        );
        Integer id =  mv.getMovieId();
        //2. delete the file associated with this object
        Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));

        //3. delete the movie object
        movieResponsitory.delete(mv);

        return "Movie deleted with id = " + id;
    };

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Movie> moviePages = movieResponsitory.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

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

        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements() ,moviePages.getTotalPages() , moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {

        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Movie> moviePages = movieResponsitory.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

        // 2. iterate through the list, generate posterUrl for each movie obj,
        // and map to MovieDto obj
        for(Movie movie : movies) {
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


        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
    }
}
