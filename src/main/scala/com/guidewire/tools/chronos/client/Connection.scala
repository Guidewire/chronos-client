package com.guidewire.tools.chronos.client

/**
 * Describes a connection.
 */
sealed trait Connection {
  def host: String
  def port: Int
  def secure: Boolean

  def uri(path: String) =
    s"${ if (secure) "https" else "http" }://$host:$port$path"
}

object Connection {
  def apply(
      host: String
    , port: Int = DEFAULT_CHRONOS_PORT
    , secure: Boolean = DEFAULT_CHRONOS_SECURE
  ) =
    HttpConnection(host, port, secure)
}

sealed case class HttpConnection(
    host: String
  , port: Int = DEFAULT_CHRONOS_PORT
  , secure: Boolean = DEFAULT_CHRONOS_SECURE
) extends Connection
