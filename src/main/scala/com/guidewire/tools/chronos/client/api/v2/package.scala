package com.guidewire.tools.chronos.client.api

import org.joda.time.ReadablePeriod
import org.joda.time.format.ISOPeriodFormat
import play.api.libs.json._

/**
 *
 */
package object v2 {
  import JsonUtils._
  import scala.language.implicitConversions

  /** Serializes and deserializes instances of `org.joda.time.ReadablePeriod` for use with play's JSON API. */
  implicit val PeriodFormat =
    createJsonFormatFromString[ReadablePeriod](s"Period must be a JSON String in ISO8601 format")
      { s => ISOPeriodFormat.standard.parsePeriod(s) }
      { o => o.toString }

  /** Serializes and deserializes instances of [[com.guidewire.tools.chronos.client.api.v2.Schedule]] for use with play's JSON API. */
  implicit val ScheduleFormat =
    createJsonFormatFromString[Schedule](s"Schedule must be a JSON String in ISO8601 expression format")
      { s => Schedule(s) }
      { o => o.toString }

  /** Serializes and deserializes instances of [[com.guidewire.tools.chronos.client.api.v2.Job]] for use with play's JSON API. */
  implicit val JobFormat =
    Json.format[Job]

  /** Serializes and deserializes instances of [[com.guidewire.tools.chronos.client.api.v2.TaskCompleted]] for use with play's JSON API. */
  implicit val TaskCompletedFormat =
    Json.format[TaskCompleted]

  implicit def schedule2Option(schedule: Schedule): Option[Schedule] =
    Option(schedule)

  /**
   * Takes objects of the form
   *
   * <pre>
   *   "com.airbnb.scheduler.jobs.TaskManager.queueSize": {
   *     "value": 0
   *   },
   *   "org.eclipse.jetty.servlet.ServletContextHandler.percent-4xx-15m": {
   *     "value": 0.0024705335221899927
   *   },
   *   "org.eclipse.jetty.servlet.ServletContextHandler.percent-4xx-1m": {
   *     "value": 2.344873328164291e-9
   *   }
   * </pre>
   *
   * and converts them into a [[scala.collection.Seq]] of [[com.guidewire.tools.chronos.client.api.v2.Gauge]]
   * instances.
   */
  implicit val SeqGaugesFormat = new Format[Seq[Gauge]] {
    def reads(json: JsValue): JsResult[Seq[Gauge]] = json match {
      case JsObject(fields) =>
        JsSuccess {
          for((name, obj) <- fields)
            yield Gauge(name, (obj \ "value").as[BigDecimal])
        }
      case _ =>
        JsError("JSON object expected")
    }

    def writes(values: Seq[Gauge]): JsValue = JsObject {
      for(o <- values)
        yield (o.name, JsObject(Seq(("value", JsNumber(o.value)))))
    }
  }

  /**
   * Takes objects of the form
   *
   * <pre>
   *   "jobs.run.failure.1f81d2d6-7e7d-4716-a92f-f8118e98412a": {
   *     "count": 0
   *   },
   *   "jobs.run.failure.21a36042-87e0-429e-bdc2-8f5f092fe0b7": {
   *     "count": 0
   *   },
   *   "jobs.run.failure.3c0bfafc-7cad-41ba-b1d5-e574e668cacb": {
   *     "count": 0
   *   }
   * </pre>
   *
   * and converts them into a [[scala.collection.Seq]] of [[com.guidewire.tools.chronos.client.api.v2.Counter]]
   * instances.
   */
  implicit val SeqCountersFormat = new Format[Seq[Counter]] {
    def reads(json: JsValue): JsResult[Seq[Counter]] = json match {
      case JsObject(fields) =>
        JsSuccess {
          for((name, obj) <- fields)
            yield Counter(name, (obj \ "count").as[BigDecimal])
        }
      case _ =>
        JsError("JSON object expected")
    }

    def writes(values: Seq[Counter]): JsValue = JsObject {
      for(o <- values)
        yield (o.name, JsObject(Seq(("count", JsNumber(o.count)))))
    }
  }

  /**
   * Takes objects of the form
   *
   * <pre>
   *   "jobs.run.time.1f81d2d6-7e7d-4716-a92f-f8118e98412a": {
   *     "count": 5,
   *     "max": 4345,
   *     "mean": 2739.6,
   *     "min": 1327,
   *     "p50": 2335,
   *     "p75": 4340.5,
   *     "p95": 4345,
   *     "p98": 4345,
   *     "p99": 4345,
   *     "p999": 4345,
   *     "stddev": 1516.7448038480302
   *   },
   *   "jobs.run.time.21a36042-87e0-429e-bdc2-8f5f092fe0b7": {
   *     "count": 5,
   *     "max": 5759,
   *     "mean": 4354.2,
   *     "min": 1762,
   *     "p50": 4763,
   *     "p75": 5755.5,
   *     "p95": 5759,
   *     "p98": 5759,
   *     "p99": 5759,
   *     "p999": 5759,
   *     "stddev": 1673.1209460167547
   *   }
   * </pre>
   *
   * and converts them into a [[scala.collection.Seq]] of [[com.guidewire.tools.chronos.client.api.v2.Histogram]]
   * instances.
   */
  implicit val SeqHistogramsFormat = new Format[Seq[Histogram]] {
    def reads(json: JsValue): JsResult[Seq[Histogram]] = json match {
      case JsObject(fields) =>
        JsSuccess {
          for((name, obj) <- fields)
            yield Histogram(
              name,
              (obj \ "count").as[BigDecimal],
              (obj \ "max").as[BigDecimal],
              (obj \ "mean").as[BigDecimal],
              (obj \ "min").as[BigDecimal],
              (obj \ "p50").as[BigDecimal],
              (obj \ "p75").as[BigDecimal],
              (obj \ "p95").as[BigDecimal],
              (obj \ "p98").as[BigDecimal],
              (obj \ "p99").as[BigDecimal],
              (obj \ "p999").as[BigDecimal],
              (obj \ "stddev").as[BigDecimal]
            )
        }
      case _ =>
        JsError("JSON object expected")
    }

    def writes(values: Seq[Histogram]): JsValue = JsObject {
      for(o <- values)
        yield (o.name, JsObject(Seq(
          ("count", JsNumber(o.count)),
          ("max", JsNumber(o.max)),
          ("mean", JsNumber(o.mean)),
          ("min", JsNumber(o.min)),
          ("p50", JsNumber(o.p50)),
          ("p75", JsNumber(o.p75)),
          ("p95", JsNumber(o.p95)),
          ("p98", JsNumber(o.p98)),
          ("p99", JsNumber(o.p99)),
          ("p999", JsNumber(o.p999)),
          ("stddev", JsNumber(o.stddev))
        )))
    }
  }

  /**
   * Takes objects of the form
   *
   * <pre>
   *   "org.eclipse.jetty.servlet.ServletContextHandler.1xx-responses": {
   *     "count": 0,
   *     "m15_rate": 0,
   *     "m1_rate": 0,
   *     "m5_rate": 0,
   *     "mean_rate": 0,
   *     "units": "events/second"
   *   },
   *   "org.eclipse.jetty.servlet.ServletContextHandler.2xx-responses": {
   *     "count": 150448,
   *     "m15_rate": 0.40003332116933116,
   *     "m1_rate": 0.40708949330864214,
   *     "m5_rate": 0.40554015224827816,
   *     "mean_rate": 0.10333925669798737,
   *     "units": "events/second"
   *   }
   * </pre>
   *
   * and converts them into a [[scala.collection.Seq]] of [[com.guidewire.tools.chronos.client.api.v2.Meter]]
   * instances.
   */
  implicit val SeqMetersFormat = new Format[Seq[Meter]] {
    def reads(json: JsValue): JsResult[Seq[Meter]] = json match {
      case JsObject(fields) =>
        JsSuccess {
          for((name, obj) <- fields)
            yield Meter(
              name,
              (obj \ "count").as[BigDecimal],
              (obj \ "m15_rate").as[BigDecimal],
              (obj \ "m1_rate").as[BigDecimal],
              (obj \ "m5_rate").as[BigDecimal],
              (obj \ "mean_rate").as[BigDecimal],
              (obj \ "units").as[String]
            )
        }
      case _ =>
        JsError("JSON object expected")
    }

    def writes(values: Seq[Meter]): JsValue = JsObject {
      for(o <- values)
        yield (o.name, JsObject(Seq(
          ("count", JsNumber(o.count)),
          ("m15_rate", JsNumber(o.m15_rate)),
          ("m1_rate", JsNumber(o.m1_rate)),
          ("m5_rate", JsNumber(o.m5_rate)),
          ("mean_rate", JsNumber(o.mean_rate)),
          ("units", JsString(o.units))
        )))
    }
  }

  /**
   * Takes objects of the form
   *
   * <pre>
   *   "com.airbnb.scheduler.api.DependentJobResource.post": {
   *     "count": 0,
   *     "max": 0,
   *     "mean": 0,
   *     "min": 0,
   *     "p50": 0,
   *     "p75": 0,
   *     "p95": 0,
   *     "p98": 0,
   *     "p99": 0,
   *     "p999": 0,
   *     "stddev": 0,
   *     "m15_rate": 0,
   *     "m1_rate": 0,
   *     "m5_rate": 0,
   *     "mean_rate": 0,
   *     "duration_units": "seconds",
   *     "rate_units": "calls/second"
   *   },
   *   "com.airbnb.scheduler.api.DependentJobResource.put": {
   *     "count": 0,
   *     "max": 0,
   *     "mean": 0,
   *     "min": 0,
   *     "p50": 0,
   *     "p75": 0,
   *     "p95": 0,
   *     "p98": 0,
   *     "p99": 0,
   *     "p999": 0,
   *     "stddev": 0,
   *     "m15_rate": 0,
   *     "m1_rate": 0,
   *     "m5_rate": 0,
   *     "mean_rate": 0,
   *     "duration_units": "seconds",
   *     "rate_units": "calls/second"
   *   }
   * </pre>
   *
   * and converts them into a [[scala.collection.Seq]] of [[com.guidewire.tools.chronos.client.api.v2.Timer]]
   * instances.
   */
  implicit val SeqTimersFormat = new Format[Seq[Timer]] {
    def reads(json: JsValue): JsResult[Seq[Timer]] = json match {
      case JsObject(fields) =>
        JsSuccess {
          for((name, obj) <- fields)
            yield Timer(
              name,
              (obj \ "count").as[BigDecimal],
              (obj \ "max").as[BigDecimal],
              (obj \ "mean").as[BigDecimal],
              (obj \ "min").as[BigDecimal],
              (obj \ "p50").as[BigDecimal],
              (obj \ "p75").as[BigDecimal],
              (obj \ "p95").as[BigDecimal],
              (obj \ "p98").as[BigDecimal],
              (obj \ "p99").as[BigDecimal],
              (obj \ "p999").as[BigDecimal],
              (obj \ "stddev").as[BigDecimal],
              (obj \ "m15_rate").as[BigDecimal],
              (obj \ "m1_rate").as[BigDecimal],
              (obj \ "m5_rate").as[BigDecimal],
              (obj \ "mean_rate").as[BigDecimal],
              (obj \ "duration_units").as[String],
              (obj \ "rate_units").as[String]
            )
        }
      case _ =>
        JsError("JSON object expected")
    }

    def writes(values: Seq[Timer]): JsValue = JsObject {
      for(o <- values)
        yield (o.name, JsObject(Seq(
          ("count", JsNumber(o.count)),
          ("max", JsNumber(o.max)),
          ("mean", JsNumber(o.mean)),
          ("min", JsNumber(o.min)),
          ("p50", JsNumber(o.p50)),
          ("p75", JsNumber(o.p75)),
          ("p95", JsNumber(o.p95)),
          ("p98", JsNumber(o.p98)),
          ("p99", JsNumber(o.p99)),
          ("p999", JsNumber(o.p999)),
          ("stddev", JsNumber(o.stddev)),
          ("m15_rate", JsNumber(o.m15_rate)),
          ("m1_rate", JsNumber(o.m1_rate)),
          ("m5_rate", JsNumber(o.m5_rate)),
          ("mean_rate", JsNumber(o.mean_rate)),
          ("duration_units", JsString(o.duration_units)),
          ("rate_units", JsString(o.rate_units))
        )))
    }
  }

  /** Serializes and deserializes instances of [[com.guidewire.tools.chronos.client.api.v2.ServerMetrics]] for use with play's JSON API. */
  implicit val ServerMetricsFormat = Json.format[ServerMetrics]
}
