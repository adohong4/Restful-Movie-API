package com.movieflix.movieAPI.Responsitory;

import com.movieflix.movieAPI.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieResponsitory extends JpaRepository<Movie, Integer> {
}
