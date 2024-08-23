package com.movieflix.movieAPI.exeption;

public class FileExistsException extends RuntimeException{
    public  FileExistsException(String message){
        super(message);
    }
}
