package com.guidewire.tools.chronos.client.api.v2

import scalaz._
import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json._

import com.guidewire.tools.chronos.client._
import com.guidewire.tools.chronos.client.api._

case class Gauge(
    name : String
  , value: BigDecimal
)

case class Counter(
    name : String
  , count: BigDecimal
)

case class Histogram(
    name  : String
  , count : BigDecimal
  , max   : BigDecimal
  , mean  : BigDecimal
  , min   : BigDecimal
  , p50   : BigDecimal
  , p75   : BigDecimal
  , p95   : BigDecimal
  , p98   : BigDecimal
  , p99   : BigDecimal
  , p999  : BigDecimal
  , stddev: BigDecimal
)

case class Meter(
    name     : String
  , count    : BigDecimal
  , m15_rate : BigDecimal
  , m1_rate  : BigDecimal
  , m5_rate  : BigDecimal
  , mean_rate: BigDecimal
  , units    : String
)

case class Timer(
    name          : String
  , count         : BigDecimal
  , max           : BigDecimal
  , mean          : BigDecimal
  , min           : BigDecimal
  , p50           : BigDecimal
  , p75           : BigDecimal
  , p95           : BigDecimal
  , p98           : BigDecimal
  , p99           : BigDecimal
  , p999          : BigDecimal
  , stddev        : BigDecimal
  , m15_rate      : BigDecimal
  , m1_rate       : BigDecimal
  , m5_rate       : BigDecimal
  , mean_rate     : BigDecimal
  , duration_units: String
  , rate_units    : String
)

case class ServerMetrics(
    version   : String
  , gauges    : Seq[Gauge]
  , counters  : Seq[Counter]
  , histograms: Seq[Histogram]
  , meters    : Seq[Meter]
  , timers    : Seq[Timer]
)

object Metrics {
  import JsonUtils._
  import HttpUtils._

  /**
   * Constructs a [[scala.Predef.String]] representing the URI for this resource.
   *
   * @param connection used to construct the full URI
   * @return a [[scala.Predef.String]] representing the URI for this resource
   */
  def uriFull(connection: Connection): String = {
    require(connection ne null, s"Missing connection")
    connection.uri(s"/metrics")
  }

  /**
   * Makes the equivalent call to `GET /metrics` and provides the response at a future time.
   *
   * @param connection used to construct the full URI
   * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
   * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
   *         the request or an error
   */
  def apply(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, ServerMetrics]] =
    full(connection, executor)

  /**
   * Makes the equivalent call to `GET /metrics` and provides the response at a future time.
   *
   * @param connection used to construct the full URI
   * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
   * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
   *         the request or an error
   */
  def full(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, ServerMetrics]] =
    httpGet[ServerMetrics](connection)(uriFull)(processFull)

  /**
   * Performs the processing of the payload from a call to `GET /metrics`.
   *
   * @param response
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def processFull(statusCode: Int, response: Array[Byte]): Validation[Error, ServerMetrics] =
    validateify(statusCode, Json.parse(response).validate[ServerMetrics])
}