package com.emotion.mifrations.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.emotion.mifrations.domain.exception.DuplicatePostException;
import com.emotion.mifrations.presentation.api.GlobalExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    @Test
    void shouldMapDuplicateToConflict() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/a");
        var response = handler.handleDuplicate(new DuplicatePostException("дубль"), request);
        assertEquals(409, response.getStatusCode().value());
    }
}
