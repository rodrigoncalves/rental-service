package com.code.rental.exception;

import java.io.Serial;

@SuppressWarnings("rawtypes")
public class ResourceNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6998112684965466651L;

    public ResourceNotFoundException(final Class clazz, final Long id) {
        super(clazz.getSimpleName() + " not found with ID " + id);
    }

    public ResourceNotFoundException(final Class clazz, final String param) {
        super(clazz.getSimpleName() + " not found with param " + param);
    }
}
