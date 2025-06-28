package com.congen.controllers

import com.congen.exceptions.InvalidResultException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@RestController
public class ExceptionHandlingController {
  // Exception handling methods

  // Convert a predefined exception to an HTTP Status code
  @ResponseStatus(value=HttpStatus.CONFLICT,
                  reason="Data integrity violation")  // 409
  @ExceptionHandler(InvalidResultException::class)
  fun conflict() {
    // Nothing to do except to maybe log the exception.
  }
}
