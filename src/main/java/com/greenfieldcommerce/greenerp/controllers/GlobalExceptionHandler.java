package com.greenfieldcommerce.greenerp.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.greenfieldcommerce.greenerp.exceptions.BusinessException;
import com.greenfieldcommerce.greenerp.records.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler
{

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
		final ApiError error = new ApiError(
			HttpStatus.UNPROCESSABLE_ENTITY.value(),
			HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
			ex.getCode(),
			ex.getMessage(),
			request.getRequestURI(), new HashMap<>());
		return ResponseEntity.unprocessableEntity().body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request)
	{
		Map<String, Object> details = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(err -> details.put(err.getField(), err.getDefaultMessage()));

		final ApiError error = new ApiError(
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"VALIDATION_ERROR",
			"One or more fields are invalid.",
			request.getRequestURI(),
			details
		);
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
		final ApiError error = new ApiError(
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
			"INTERNAL_ERROR",
			ex.getMessage(),
			request.getRequestURI(),
			new HashMap<>()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}
