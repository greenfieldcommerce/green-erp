package com.greenfieldcommerce.greenerp.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.greenfieldcommerce.greenerp.exceptions.BusinessException;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;
import com.greenfieldcommerce.greenerp.records.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler
{

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiError> handleBusinessException(EntityNotFoundException ex, HttpServletRequest request)
	{
		final ApiError error = new ApiError(
			HttpStatus.NOT_FOUND.value(),
			HttpStatus.NOT_FOUND.getReasonPhrase(),
			ex.getCode(),
			ex.getMessage(),
			request.getRequestURI(),
			new HashMap<>());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
		final ApiError error = new ApiError(
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			ex.getCode(),
			ex.getMessage(),
			request.getRequestURI(), new HashMap<>());
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request)
	{
		Map<String, Object> details = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(err -> details.put(err.getField(), err.getDefaultMessage()));

		final ApiError error = new ApiError(
			HttpStatus.UNPROCESSABLE_ENTITY.value(),
			HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
			"VALIDATION_ERROR",
			"One or more fields are invalid.",
			request.getRequestURI(),
			details
		);
		return ResponseEntity.unprocessableEntity().body(error);
	}
}
