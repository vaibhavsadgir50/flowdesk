package com.flowdesk.service;

public class NotFoundException extends RuntimeException {

    public NotFoundException(Long id) {
        super("Work item " + id + " not found");
    }
}
