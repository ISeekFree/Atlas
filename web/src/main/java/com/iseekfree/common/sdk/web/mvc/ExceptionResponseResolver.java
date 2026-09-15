package com.iseekfree.common.sdk.web.mvc;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Business hook that maps an otherwise unhandled exception onto the unified
 * {@code {code,msg,data}} envelope returned to the frontend.
 *
 * <p>Resolvers are consulted in {@link org.springframework.core.annotation.Order}
 * order by {@link GlobalExceptionHandler}. Return {@code null} to let the next
 * resolver, and finally the SDK default ({@code code = -90}), decide. Register
 * one or more beans to customise what a caller receives for specific
 * exceptions; {@link ExceptionResponse#of(int, int, String)} also lets a
 * resolver keep an infrastructural HTTP status.</p>
 */
@FunctionalInterface
public interface ExceptionResponseResolver {

    ExceptionResponse resolve(Throwable error, HttpServletRequest request);
}
