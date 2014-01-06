package com.guidewire.tools.chronos.client.api

import play.api.libs.json._
import org.joda.time.{ReadablePeriod, Period}
import org.joda.time.format.ISOPeriodFormat

/**
 *
 */
object JsonUtils {
/**
   * Takes a [[play.api.libs.json.Format]] and maps between a more general representation to a version-specific representation.
   * This is necessary because [[play.api.libs.json.Writes]] is contravariant.
   *
   * @param specific existing [[play.api.libs.json.Format]] that will be mapped
   * @tparam TGeneral type that will be mapped to
   * @tparam TSpecific type that will be mapped from
   * @return new [[play.api.libs.json.Format]] that reads and writes the `TGeneral` type
   */
  def mapJsonFormat[TGeneral, TSpecific <: TGeneral](specific: Format[TSpecific]): Format[TGeneral] = new Format[TGeneral] {
    def reads(json: JsValue): JsResult[TGeneral] = specific.reads(json)
    def writes(o: TGeneral) = specific.writes(o.asInstanceOf[TSpecific])
  }

  /**
   * Constructs a [[play.api.libs.json.Format]] instance from deserialize and serialize functions that read/write to
   * a [[scala.Predef.String]].
   *
   * @param errorMessage message that will be used if a provided JSON value is not a [[scala.Predef.String]]
   * @param deserialize function that accepts a [[scala.Predef.String]] and produces an instance of type `TTarget`
   * @param serialize function that accepts an instance of type `TTarget` and produces a [[scala.Predef.String]]
   * @tparam TTarget type of object that will be serialized and deserialized
   * @return new [[play.api.libs.json.Format]] that reads and writes the `TTarget` type
   */
  def createJsonFormatFromString[TTarget](errorMessage: String)(deserialize: (String => TTarget))(serialize: (TTarget => String)): Format[TTarget] = new Format[TTarget] {
    def reads(json: JsValue): JsResult[TTarget] =
      try {
        json match {
          case JsString(s) => JsSuccess(deserialize(s))
          case _ => JsError(errorMessage)
        }
      } catch {
        case t: Throwable => JsError(t.getMessage)
      }
    def writes(o: TTarget) =
      JsString(serialize(o))
  }

  /**
   * Constructs a [[play.api.libs.json.Format]] instance from deserialize and serialize functions that read/write to
   * a [[scala.BigDecimal]].
   *
   * @param errorMessage message that will be used if a provided JSON value is not a [[scala.BigDecimal]]
   * @param deserialize function that accepts a [[scala.BigDecimal]] and produces an instance of type `TTarget`
   * @param serialize function that accepts an instance of type `TTarget` and produces a [[scala.BigDecimal]]
   * @tparam TTarget type of object that will be serialized and deserialized
   * @return new [[play.api.libs.json.Format]] that reads and writes the `TTarget` type
   */
  def createJsonFormatFromNumber[TTarget](errorMessage: String)(deserialize: (BigDecimal => TTarget))(serialize: (TTarget => BigDecimal)): Format[TTarget] = new Format[TTarget] {
    def reads(json: JsValue): JsResult[TTarget] =
      try {
        json match {
          case JsNumber(s) => JsSuccess(deserialize(s))
          case _ => JsError(errorMessage)
        }
      } catch {
        case t: Throwable => JsError(t.getMessage)
      }
    def writes(o: TTarget) =
      JsNumber(serialize(o))
  }
}
