package com.movieflix.movieAPI.reponsitory;

import com.movieflix.movieAPI.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieResponsitory extends JpaRepository<Movie, Integer> {
}
