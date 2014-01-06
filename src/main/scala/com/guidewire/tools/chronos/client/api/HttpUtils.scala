package com.guidewire.tools.chronos.client.api

import scala.concurrent.{Future, ExecutionContext}

import dispatch._
import play.api.libs.json._
import com.ning.http.client.Response

import com.guidewire.tools.chronos.client._

/**
 *
 */
object HttpUtils {
  //TODO: refactor this -- lots of duplicated code.

  /**
   * Performs an HTTP `GET` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   *
   * @param connection [[com.guidewire.tools.chronos.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.chronos.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpGet[TResult](connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => scalaz.Validation[Error, TResult])(implicit executor: ExecutionContext): Future[scalaz.Validation[Error, TResult]] = {
    val GET =
      url(endpoint(connection))
        .addHeader("Accept", "application/json")
        .GET

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(GET > processResponse _)
  }

  /**
   * Performs an HTTP `GET` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   *
   * @param connection [[com.guidewire.tools.chronos.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.chronos.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpGetPlainText[TResult](connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => scalaz.Validation[Error, TResult])(implicit executor: ExecutionContext): Future[scalaz.Validation[Error, TResult]] = {
    val GET =
      url(endpoint(connection))
        .addHeader("Accept", "text/plain")
        .GET

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(GET > processResponse _)
  }

  /**
   * Performs an HTTP `POST` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   *
   * @param obj instance that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @param connection [[com.guidewire.tools.chronos.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.chronos.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TObject type of object that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpPostEmpty[TObject, TResult](obj: TObject, connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => scalaz.Validation[Error, TResult])(implicit executor: ExecutionContext): Future[scalaz.Validation[Error, TResult]] = {
    val POST =
      url(endpoint(connection))
        .POST
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(POST > processResponse _)
  }

  /**
   * Performs an HTTP `POST` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   *
   * @param obj instance that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @param connection [[com.guidewire.tools.chronos.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.chronos.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param writer instance of [[play.api.libs.json.Writes]] that can serialize an instance of `TObject` into a [[play.api.libs.json.JsValue]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TObject type of object that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpPostAsJson[TObject, TResult](obj: TObject, connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => scalaz.Validation[Error, TResult])(implicit writer: Writes[TObject], executor: ExecutionContext): Future[scalaz.Validation[Error, TResult]] = {
    val POST =
      url(endpoint(connection))
        .POST
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")
        .setBody(Json.toJson(obj).toString().getBytes(DEFAULT_CHRONOS_CHARSET))

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(POST > processResponse _)
  }

  /**
   * Performs an HTTP `PUT` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   *
   * @param connection [[com.guidewire.tools.chronos.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.chronos.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpPut[TResult](connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => scalaz.Validation[Error, TResult])(implicit executor: ExecutionContext): Future[scalaz.Validation[Error, TResult]] = {
    val PUT =
      url(endpoint(connection))
        .PUT
        .addHeader("Accept", "application/json")

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(PUT > processResponse _)
  }

  /**
   * Performs an HTTP `POST` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   *
   * @param obj instance that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @param connection [[com.guidewire.tools.chronos.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.chronos.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param writer instance of [[play.api.libs.json.Writes]] that can serialize an instance of `TObject` into a [[play.api.libs.json.JsValue]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TObject type of object that will be serialized into JSON using the provided implicit [[play.api.libs.json.Writes]] instance
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpPutAsJson[TObject, TResult](obj: TObject, connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => scalaz.Validation[Error, TResult])(implicit writer: Writes[TObject], executor: ExecutionContext): Future[scalaz.Validation[Error, TResult]] = {
    val PUT =
      url(endpoint(connection))
        .PUT
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")
        .setBody(Json.toJson(obj).toString().getBytes(DEFAULT_CHRONOS_CHARSET))

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(PUT > processResponse _)
  }

  /**
   * Performs an HTTP `DELETE` using a URL derived from the provided `endpoint`. The result is given to the provided `resultProcessor`.
   *
   * @param connection [[com.guidewire.tools.chronos.client.Connection]] describing the host and port to connect to
   * @param endpoint function that given a [[com.guidewire.tools.chronos.client.Connection]] will provide a URL as a [[scala.Predef.String]]
   * @param resultProcessor function that process the server response and produces a [[scalaz.Validation]]
   * @param executor [[scala.concurrent.ExecutionContext]] that will execute the HTTP request
   * @tparam TResult type of result that the `resultProcessor` should produce and validate
   * @return instance of [[scala.concurrent.Future]] whose promise is fulfilled upon receipt of the server response
   */
  def httpDelete[TResult](connection: Connection)(endpoint: Connection => String)(resultProcessor: (Int, Array[Byte]) => scalaz.Validation[Error, TResult])(implicit executor: ExecutionContext): Future[scalaz.Validation[Error, TResult]] = {
    val DELETE =
      url(endpoint(connection))
        .DELETE
        .addHeader("Accept", "application/json")

    def processResponse(response: Response) = {
      val status_code = response.getStatusCode
      val status_code_class = status_code / 100

      status_code_class match {
        case 2 | 4 =>
          resultProcessor(status_code, response.getResponseBodyAsBytes)
        case _ =>
          throw StatusCode(status_code)
      }
    }

    Http(DELETE > processResponse _)
  }

  /**
   * Takes a [[play.api.libs.json.JsResult]] and maps it to a [[scalaz.Validation]].
   *
   * @param result the [[play.api.libs.json.JsResult]] to map
   * @tparam T type of [[play.api.libs.json.JsResult]] that will be mapped to a [[scalaz.Validation]]
   * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
   */
  def validateify[T](statusCode: Int, result: JsResult[T]): scalaz.Validation[Error, T] =
    result
      .map { p =>
        scalaz.Success(p)
      }
      .recoverTotal { e =>
        val (_, errors) = e.errors.head
        scalaz.Failure(HttpError(statusCode, errors.head.message))
      }

  def successIf204(statusCode: Int, response: Array[Byte], ignoreOn400: Boolean = false): scalaz.Validation[Error, Boolean] =
    if (statusCode == 204 || (ignoreOn400 && statusCode == 400))
      scalaz.Success(statusCode == 204)
    else
      scalaz.Failure(HttpError(statusCode, s"Unable to process request. Response: ${bytesToString(response)}"))

  /**
   * Takes `GET` responses that are not valid JSON but are valid boolean values like `true` or `false` and converts
   * them into a [[scalaz.Validation]] instance.
   *
   * @param response should be a single [[scala.Predef.String]] payload that will be parsed and evaluated
   * @return instance of [[scalaz.Validation]] with an error message if unable to decode the value or the return value
   *         if the `GET` was successful
   */
  def processSingleBooleanStringHttpGetResponse(statusCode: Int, response: Array[Byte]): scalaz.Validation[Error, Boolean] = {
    try {
      val parsed_response = bytesToString(response).toLowerCase
      scalaz.Success(parsed_response == "true")
    } catch {
      case t: Throwable => scalaz.Failure(HttpError(statusCode, t.getMessage))
    }
  }

  /**
   * Takes `GET` responses that are not valid JSON but are valid [[scala.Predef.String]] values and converts
   * them into a [[scalaz.Validation]] instance.
   *
   * @param response should be a single [[scala.Predef.String]] payload that will be parsed and evaluated
   * @return instance of [[scalaz.Validation]] with an error message if unable to decode the value or the return value
   *         if the `GET` was successful
   */
  def processSingleStringHttpGetResponse(statusCode: Int, response: Array[Byte]): scalaz.Validation[Error, String] = {
    try {
      if (statusCode / 100 == 2) {
        val parsed_response = bytesToString(response)
        scalaz.Success(parsed_response)
      } else {
        scalaz.Failure(HttpError(statusCode, s"Unable to process request. Response: ${bytesToString(response)}"))
      }
    } catch {
      case t: Throwable => scalaz.Failure(HttpError(statusCode, t.getMessage))
    }
  }

  def bytesToString(response: Array[Byte]): String =
    DEFAULT_CHRONOS_CHARSET.decode(java.nio.ByteBuffer.wrap(response)).toString

  /**
   * Validates that an HTTP status code is in the OK range.
   *
   * @param statusCode HTTP status code to evaluate
   * @return `true` if the HTTP status code is OK
   */
  def isOK(statusCode: Int): Boolean =
    statusCode / 100 == 2
}
