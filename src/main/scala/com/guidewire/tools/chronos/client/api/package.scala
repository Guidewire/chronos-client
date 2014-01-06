package com.guidewire.tools.chronos.client

import java.net.URI

/**
 *
 */
package object api {
  import JsonUtils._

  /** Serializes and deserializes instances of `java.net.URI` for use with play's JSON API. */
  implicit val URIFormat =
    createJsonFormatFromString[URI](s"URI must be a JSON string")
      { s => new URI(s) }
      { u => u.toString }

  /** Serializes and deserializes instances of [[com.guidewire.tools.chronos.client.OptionalDateTime]] for use with play's JSON API. */
  implicit val OptionalDateTimeFormat =
    createJsonFormatFromString[OptionalDateTime](s"OptionalDateTime must be a JSON string")
      {  s => s.toOptionalDateTime }
      { dt => dt.asString }

  /** Serializes and deserializes instances of [[com.guidewire.tools.chronos.client.DataUnit]] for use with play's JSON API. */
  implicit val DataUnitFormat =
    createJsonFormatFromNumber[DataUnit](s"DataUnit must be a JSON number")
      { d => d.asMB }
      { v => v.value }

  /** Serializes and deserializes instances of [[com.guidewire.tools.chronos.client.FrequencyUnit]] for use with play's JSON API. */
  implicit val FrequencyUnitFormat =
    createJsonFormatFromNumber[FrequencyUnit](s"FrequencyUnit must be a JSON number")
      { d => d.asMHz }
      { v => v.value }
}
