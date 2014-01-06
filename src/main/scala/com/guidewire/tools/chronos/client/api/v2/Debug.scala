package com.guidewire.tools.chronos.client.api.v2

import scalaz._
import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json._

import com.guidewire.tools.chronos.client._
import com.guidewire.tools.chronos.client.api._

/**
 *
 */
object Debug {
  import JsonUtils._
  import HttpUtils._

  /**
   * Constructs a [[scala.Predef.String]] representing the URI for this resource.
   *
   * @param connection used to construct the full URI
   * @return a [[scala.Predef.String]] representing the URI for this resource
   */
  def uriPing(connection: Connection): String = {
    require(connection ne null, s"Missing connection")
    connection.uri(s"/ping")
  }

  /**
   * Makes the equivalent call to `GET /ping` and provides the response at a future time.
   *
   * @param connection used to construct the full URI
   * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
   * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
   *         the request or an error
   */
  def ping(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, Boolean]] =
    httpGet[Boolean](connection)(uriPing)(processPing)

  /**
   * Performs the processing of the payload from a call to `GET /ping`.
   *
   * @param response `true` if the returned payload is the string `pong`
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def processPing(statusCode: Int, response: Array[Byte]): Validation[Error, Boolean] =
    processSingleStringHttpGetResponse(statusCode, response).map(x => (x ne null) && x.trim() == "pong")
}
