package com.guidewire.tools.chronos

import java.nio.charset.Charset
import org.joda.time.{Period, DateTime, ReadableDateTime}
import org.joda.time.format.DateTimeFormatter

/**
 *
 */
package object client {
  val DEFAULT_CHRONOS_CHARSET = Charset.forName("UTF-8")
  val DEFAULT_CHRONOS_PORT = 80
  val DEFAULT_CHRONOS_SECURE = false


  import scala.math._
  import scala.language.implicitConversions

  final class OptionalDateTime(val value: ReadableDateTime) extends AnyVal {
    @inline def asString: String = if (value eq null) "" else value.toString
    override def toString: String = s"OptionalDateTime(${asString})"
  }

  object OptionalDateTime {
    def apply(value: ReadableDateTime) = new OptionalDateTime(value)
    def apply(value: String) = new OptionalDateTime(DateTime.parse(value))
    def apply(value: String, formatter: DateTimeFormatter) = new OptionalDateTime(DateTime.parse(value, formatter))
  }

  implicit def optionalDateTime2DateTime(dt: OptionalDateTime): ReadableDateTime =
    dt.value

  implicit final class StringExtensions(val value: String) extends AnyVal {
    @inline def toOptionalDateTime: OptionalDateTime = {
      if ((value ne null) && "" != value.trim)
        OptionalDateTime(value)
      else
        OptionalDateTime(null:ReadableDateTime)
    }

    @inline def toDateTime: DateTime = {
      require((value ne null) && "" != value.trim, s"Unable to parse a date/time from an empty string")
      DateTime.parse(value)
    }

    @inline def toPeriod: Period = {
      require((value ne null) && "" != value.trim, s"Unable to parse a period from an empty string")
      Period.parse(value)
    }
  }

  /**
   * Value class that represents a [[scala.BigDecimal]] as a value indicating an amount
   * of data such as a megabyte (MB), gigabyte (GB), terabyte (TB), petabyte (PB), etc.
   *
   * All values are converted and calculated in terms of megabytes (MB).
   *
   * @param value value in megabytes (MB)
   */
  final class DataUnit(val value: BigDecimal) extends AnyVal {
    @inline def +(v: DataUnit): DataUnit = new DataUnit(value + v.value.asMB.value)
    @inline def -(v: DataUnit): DataUnit = new DataUnit(value - v.value.asMB.value)
    @inline def *(v: DataUnit): DataUnit = new DataUnit(value * v.value.asMB.value)
    @inline def /(v: DataUnit): DataUnit = new DataUnit(value / v.value.asMB.value)
    @inline def %(v: DataUnit): DataUnit = new DataUnit(value % v.value.asMB.value)
    @inline def <(v: DataUnit): Boolean = value < v.value
    @inline def >(v: DataUnit): Boolean = value > v.value
    @inline def <=(v: DataUnit): Boolean = value <= v.value
    @inline def >=(v: DataUnit): Boolean = value >= v.value
    override def toString: String = s"DataUnit(${value.toString()} MB)"
  }

  object DataUnit {
    val BYTES_TO_MEGABYTES      = BigDecimal(1.0D / pow(1024.0D, 2.0D))
    val KILOBYTES_TO_MEGABYTES  = BigDecimal(1024.0D / pow(1024.0D, 1.0D))
    val MEGABYTES_TO_MEGABYTES  = BigDecimal(1.0D)
    val TERABYTES_TO_MEGABYTES  = BigDecimal(pow(1024.0D, 2.0D))
    val PETABYTES_TO_MEGABYTES  = BigDecimal(pow(1024.0D, 3.0D))
    val EXABYTES_TO_MEGABYTES   = BigDecimal(pow(1024.0D, 4.0D))
    val ZETTABYTES_TO_MEGABYTES = BigDecimal(pow(1024.0D, 5.0D))
    val YOTTABYTES_TO_MEGABYTES = BigDecimal(pow(1024.0D, 6.0D))

    implicit def asBigDecimal(value: Int) = BigDecimal(value)
    def apply(value: BigDecimal) = new DataUnit(value)
  }

  /**
   * Value class that represents a [[scala.BigDecimal]] as a value indicating a
   * frequency such as megahertz (MHz), gigahertz (GHz), etc.
   *
   * All values are converted and calculated in terms of megahertz (MHz).
   *
   * @param value value in megahertz (MHz)
   */
  final class FrequencyUnit(val value: BigDecimal) extends AnyVal {
    @inline def +(v: FrequencyUnit): FrequencyUnit = new FrequencyUnit(value + v.value.asMB.value)
    @inline def -(v: FrequencyUnit): FrequencyUnit = new FrequencyUnit(value - v.value.asMB.value)
    @inline def *(v: FrequencyUnit): FrequencyUnit = new FrequencyUnit(value * v.value.asMB.value)
    @inline def /(v: FrequencyUnit): FrequencyUnit = new FrequencyUnit(value / v.value.asMB.value)
    @inline def %(v: FrequencyUnit): FrequencyUnit = new FrequencyUnit(value % v.value.asMB.value)
    @inline def <(v: FrequencyUnit): Boolean = value < v.value
    @inline def >(v: FrequencyUnit): Boolean = value > v.value
    @inline def <=(v: FrequencyUnit): Boolean = value <= v.value
    @inline def >=(v: FrequencyUnit): Boolean = value >= v.value
    override def toString: String = s"FrequencyUnit(${value.toString()} MHz)"
  }

  object FrequencyUnit {
    val MEGAHERTZ_TO_MEGAHERTZ  = BigDecimal(1.0D)
    val GIGAHERTZ_TO_MEGAHERTZ  = BigDecimal(pow(1000.0D, 1.0D))
    val TERAHERTZ_TO_MEGAHERTZ  = BigDecimal(pow(1000.0D, 2.0D))
    val PETAHERTZ_TO_MEGAHERTZ  = BigDecimal(pow(1000.0D, 3.0D))
    val EXAHERTZ_TO_MEGAHERTZ   = BigDecimal(pow(1000.0D, 4.0D))
    val ZETTAHERTZ_TO_MEGAHERTZ = BigDecimal(pow(1000.0D, 5.0D))
    val YOTTAHERTZ_TO_MEGAHERTZ = BigDecimal(pow(1000.0D, 6.0D))

    implicit def asBigDecimal(value: Int) = BigDecimal(value)
    def apply(value: BigDecimal) = new FrequencyUnit(value)
  }

  implicit final class BigDecimalExtensions(val underlying: BigDecimal) extends AnyVal {
    import DataUnit._
    import FrequencyUnit._

    @inline def asBytes = DataUnit(underlying * BYTES_TO_MEGABYTES)
    @inline def asKB    = DataUnit(underlying * KILOBYTES_TO_MEGABYTES)
    @inline def asMB    = DataUnit(underlying * MEGABYTES_TO_MEGABYTES)
    @inline def asTB    = DataUnit(underlying * TERABYTES_TO_MEGABYTES)
    @inline def asPB    = DataUnit(underlying * PETABYTES_TO_MEGABYTES)
    @inline def asEB    = DataUnit(underlying * EXABYTES_TO_MEGABYTES)
    @inline def asZB    = DataUnit(underlying * ZETTABYTES_TO_MEGABYTES)
    @inline def asYB    = DataUnit(underlying * YOTTABYTES_TO_MEGABYTES)

    @inline def asMHz   = FrequencyUnit(underlying * MEGAHERTZ_TO_MEGAHERTZ)
    @inline def asGHz   = FrequencyUnit(underlying * GIGAHERTZ_TO_MEGAHERTZ)
    @inline def asTHz   = FrequencyUnit(underlying * TERAHERTZ_TO_MEGAHERTZ)
    @inline def asPHz   = FrequencyUnit(underlying * PETAHERTZ_TO_MEGAHERTZ)
    @inline def asEHz   = FrequencyUnit(underlying * EXAHERTZ_TO_MEGAHERTZ)
    @inline def asZHz   = FrequencyUnit(underlying * ZETTAHERTZ_TO_MEGAHERTZ)
    @inline def asYHz   = FrequencyUnit(underlying * YOTTAHERTZ_TO_MEGAHERTZ)

  }

  implicit final class IntExtensions(val underlying: Int) extends AnyVal {
    @inline def asBigDecimal = BigDecimal(underlying)

    @inline def asBytes = asBigDecimal.asBytes
    @inline def asKB    = asBigDecimal.asKB
    @inline def asMB    = asBigDecimal.asMB
    @inline def asTB    = asBigDecimal.asTB
    @inline def asPB    = asBigDecimal.asPB
    @inline def asEB    = asBigDecimal.asEB
    @inline def asZB    = asBigDecimal.asZB
    @inline def asYB    = asBigDecimal.asYB

    @inline def asMHz   = asBigDecimal.asMHz
    @inline def asGHz   = asBigDecimal.asGHz
    @inline def asTHz   = asBigDecimal.asTHz
    @inline def asPHz   = asBigDecimal.asPHz
    @inline def asEHz   = asBigDecimal.asEHz
    @inline def asZHz   = asBigDecimal.asZHz
    @inline def asYHz   = asBigDecimal.asYHz
  }

  implicit final class DoubleExtensions(val underlying: Double) extends AnyVal {
    @inline def asBigDecimal = BigDecimal(underlying)

    @inline def asBytes = asBigDecimal.asBytes
    @inline def asKB    = asBigDecimal.asKB
    @inline def asMB    = asBigDecimal.asMB
    @inline def asTB    = asBigDecimal.asTB
    @inline def asPB    = asBigDecimal.asPB
    @inline def asEB    = asBigDecimal.asEB
    @inline def asZB    = asBigDecimal.asZB
    @inline def asYB    = asBigDecimal.asYB

    @inline def asMHz   = asBigDecimal.asMHz
    @inline def asGHz   = asBigDecimal.asGHz
    @inline def asTHz   = asBigDecimal.asTHz
    @inline def asPHz   = asBigDecimal.asPHz
    @inline def asEHz   = asBigDecimal.asEHz
    @inline def asZHz   = asBigDecimal.asZHz
    @inline def asYHz   = asBigDecimal.asYHz
  }
}
