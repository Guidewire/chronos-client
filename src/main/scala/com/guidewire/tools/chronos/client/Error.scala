package com.guidewire.tools.chronos.client

/**
 *
 */
sealed trait Error {
  def message: String
}

sealed case class HttpError(statusCode: Int, message: String) extends Error
