package com.example.catchcompass.user;

public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException() {
        super("That username is already taken");
    }
}
