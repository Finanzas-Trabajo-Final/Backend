package com.grupo4.americash.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Hidden
@RestController
@RequestMapping({"/api/v1/documentation", "/docs"})
@Slf4j
public class DocumentationController {

    @GetMapping
    public void redirectToDocumentation(HttpServletResponse response) {
        try {
            response.sendRedirect("swagger-ui.html");
        } catch (IOException e) {
            log.error("Could not redirect to the documentation", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/html");
            String errorPage = String.format(
                "<html><head><title>Something went wrong</title></head>" +
                "<body><h1>Something went wrong</h1><p>%s</p>" +
                "<p>Could not redirect to the documentation</p></body></html>",
                e.getMessage() != null ? e.getMessage() : ""
            );
            try {
                response.getWriter().write(errorPage);
            } catch (IOException ioException) {
                log.error("Could not write error message to response", ioException);
            }
        }
    }
}