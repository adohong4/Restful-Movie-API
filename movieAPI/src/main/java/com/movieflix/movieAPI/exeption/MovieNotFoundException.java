package com.movieflix.movieAPI.exeption;

public class MovieNotFoundException extends  RuntimeException{
    public MovieNotFoundException(String message){
        super(message);
    }

}
