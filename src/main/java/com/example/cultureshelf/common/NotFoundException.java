package com.example.cultureshelf.common;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String what, Object id) {
        super(what + " introuvable : " + id);
    }
}
