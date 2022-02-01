package org.galatea.starter.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadArgumentsException extends RuntimeException {
  /**
   * A small error handler specifically for Bad Requests.
   */
  public BadArgumentsException(final String message) {

  }
}
