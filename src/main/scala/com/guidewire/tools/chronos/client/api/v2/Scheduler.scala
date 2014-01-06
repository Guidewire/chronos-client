package com.guidewire.tools.chronos.client.api.v2

import scalaz._
import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json._

import org.joda.time._
import org.joda.time.format.{ISOPeriodFormat, ISODateTimeFormat}

import java.lang.{Iterable => JavaIterable}
import java.net.URI

import com.guidewire.tools.chronos.client._
import com.guidewire.tools.chronos.client.api._

sealed case class Schedule(recurrences: Long, starting: ReadableDateTime, period: ReadablePeriod) {
  import Schedule._

  require(recurrences >= -1L, s"Recurrences must be infinite (-1), 0 (none), or any number above that")
  require(starting ne null, s"Missing the starting date/time")
  require(period ne null, s"Missing the period")

  val repeatsForever = recurrences < 0
  def isRepeatingForever = repeatsForever

  override def toString = {
    val starting_as_string = s"${ISODateTimeFormat.date().print(starting)}${ISODateTimeFormat.tTime().print(starting)}"
    val period_as_string   = ISOPeriodFormat.standard.print(period)

    if (!repeatsForever)
      s"R$recurrences/$starting_as_string/$period_as_string"
    else
      s"R/$starting_as_string/$period_as_string"
  }
}

object Schedule {
  private[this] val ISO8601Expression = """(R[0-9]*)/(.*)/(P.*)?""".r

  def canParse(input: String): Boolean =
    ISO8601Expression.pattern.matcher(input).matches()

  def apply(input: String): Schedule = {
    require(canParse(input), s"Invalid schedule: $input")

    val ISO8601Expression(repetitions_as_string, starting_as_string, period_as_string) = input

    val period = ISOPeriodFormat.standard.parsePeriod(period_as_string)
    val start = DateTime.parse(starting_as_string)
    val repetitions =
      if (repetitions_as_string.length <= 1)
        -1L
      else
        repetitions_as_string.substring(1).toLong

    Schedule(repetitions, start, period)
  }
}

sealed case class Job(
    name                  : String
  , command               : String
  , epsilon               : ReadablePeriod      = Minutes.minutes(5).toPeriod
  , successCount          : Long                = 0L
  , errorCount            : Long                = 0L
  , executor              : String              = ""
  , executorFlags         : String              = ""
  , retries               : Int                 = 2
  , owner                 : String              = ""
  , lastSuccess           : OptionalDateTime    = "".toOptionalDateTime
  , lastError             : OptionalDateTime    = "".toOptionalDateTime
  , async                 : Boolean             = false
  , cpus                  : FrequencyUnit       = 0.0D.asMHz
  , disk                  : DataUnit            = 0.asMB
  , mem                   : DataUnit            = 0.asMB
  , disabled              : Boolean             = false
  , uris                  : Seq[URI]            = Seq()
  , errorsSinceLastSuccess: Option[Long]        = Some(0L)
  , parents               : Option[Set[String]] = None
  , schedule              : Option[Schedule]    = None
) {
  def isScheduled: Boolean = schedule.isDefined && parents.isEmpty
  def isDependent: Boolean = parents.isDefined && schedule.isEmpty
}

object Jobs {
  import scala.collection.JavaConversions._
  def scheduled(name: String, command: String, schedule: Schedule): Job =
    Job(name, command, schedule = Some(schedule))

  def scheduled(name: String, command: String, retries: Int, cpus: FrequencyUnit, disk: DataUnit, mem: DataUnit, disabled: Boolean, schedule: Schedule): Job =
    Job(name, command, schedule = Some(schedule), retries = retries, cpus = cpus, disk = disk, mem = mem, disabled = disabled)

  def dependent(name: String, command: String, parents: TraversableOnce[String]): Job =
    Job(name, command, parents = Some(parents.toSet))

  def dependent(name: String, command: String, retries: Int, cpus: FrequencyUnit, disk: DataUnit, mem: DataUnit, disabled: Boolean, parents: TraversableOnce[String]): Job =
    Job(name, command, parents = Some(parents.toSet), retries = retries, cpus = cpus, disk = disk, mem = mem, disabled = disabled)

  def dependent(name: String, command: String, parents: JavaIterable[String]): Job =
    Job(name, command, parents = Some(parents.toSet))

  def dependent(name: String, command: String, retries: Int, cpus: FrequencyUnit, disk: DataUnit, mem: DataUnit, disabled: Boolean, parents: JavaIterable[String]): Job =
    Job(name, command, parents = Some(parents.toSet), retries = retries, cpus = cpus, disk = disk, mem = mem, disabled = disabled)
}

sealed case class TaskCompleted(
    statusCode: Int = 0
)

/**
 *
 */
object Scheduler {
  import JsonUtils._
  import HttpUtils._

  object jobs {
    /**
     * Constructs a [[scala.Predef.String]] representing the URI for this resource.
     *
     * @param connection used to construct the full URI
     * @return a [[scala.Predef.String]] representing the URI for this resource
     */
    def uriList(connection: Connection): String = {
      require(connection ne null, s"Missing connection")
      connection.uri(s"/scheduler/jobs")
    }

    /**
     * Makes the equivalent call to `GET /scheduler/jobs` and provides the response at a future time.
     *
     * @param connection used to construct the full URI
     * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
     * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
     *         the request or an error
     */
    def apply(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, Traversable[Job]]] =
      list(connection, executor)

    /**
     * Makes the equivalent call to `GET /scheduler/jobs` and provides the response at a future time.
     *
     * @param connection used to construct the full URI
     * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
     * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
     *         the request or an error
     */
    def list(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, Traversable[Job]]] =
      httpGet[Traversable[Job]](connection)(uriList)(processList)

    /**
     * Performs the actual parsing and validation of a JSON payload representing the payload from a call
     * to `GET /scheduler/jobs`.
     *
     * @param response JSON contents to parse and map to a [[scala.collection.Traversable]]
     *                 of [[com.guidewire.tools.chronos.client.api.v2.Job]]
     * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
     */
    def processList(statusCode: Int, response: Array[Byte]): Validation[Error, Traversable[Job]] =
      validateify(statusCode, Json.parse(response).validate[Seq[Job]])

    /**
     * Constructs a [[scala.Predef.String]] representing the URI for this resource.
     *
     * @param connection used to construct the full URI
     * @return a [[scala.Predef.String]] representing the URI for this resource
     */
    def uriAddScheduled(connection: Connection): String = {
      require(connection ne null, s"Missing connection")
      connection.uri(s"/scheduler/iso8601")
    }

    /**
     * Makes the equivalent call to `POST /scheduler/iso8601` and provides the response at a future time.
     *
     * @param job details of the job to run
     * @param connection used to construct the full URI
     * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
     * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
     *         the request or an error
     */
    def addScheduled(job: Job)(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, Boolean]] = {
      require(job.parents.isEmpty && job.schedule.isDefined, s"A schedule must be defined for this job and must not define any parents")
      httpPostAsJson[Job, Boolean](job, connection)(uriAddScheduled)(processAddScheduled)
    }

    /**
     * Performs the response processing from a call to `POST /scheduler/iso8601`.
     *
     * @param response should be empty if `statusCode` is a 204 (indicating success)
     * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
     */
    def processAddScheduled(statusCode: Int, response: Array[Byte]): Validation[Error, Boolean] =
      successIf204(statusCode, response)

    /**
     * Constructs a [[scala.Predef.String]] representing the URI for this resource.
     *
     * @param connection used to construct the full URI
     * @return a [[scala.Predef.String]] representing the URI for this resource
     */
    def uriAddDependent(connection: Connection): String = {
      require(connection ne null, s"Missing connection")
      connection.uri(s"/scheduler/dependency")
    }

    /**
     * Makes the equivalent call to `POST /scheduler/dependency` and provides the response at a future time.
     *
     * @param job details of the job to run
     * @param connection used to construct the full URI
     * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
     * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
     *         the request or an error
     */
    def addDependent(job: Job)(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, Boolean]] = {
      require(job.schedule.isEmpty && job.parents.isDefined && !job.parents.get.isEmpty, s"A parent must be defined for this job and must not define a schedule")
      httpPostAsJson[Job, Boolean](job, connection)(uriAddDependent)(processAddDependent)
    }

    /**
     * Performs the response processing from a call to `POST /scheduler/dependency`.
     *
     * @param response should be empty if `statusCode` is a 204 (indicating success)
     * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
     */
    def processAddDependent(statusCode: Int, response: Array[Byte]): Validation[Error, Boolean] =
      successIf204(statusCode, response)

    /**
     * Constructs a [[scala.Predef.String]] representing the URI for this resource.
     *
     * @param jobName name of a job
     * @param connection used to construct the full URI
     * @return a [[scala.Predef.String]] representing the URI for this resource
     */
    def uriDelete(jobName: String)(connection: Connection): String = {
      require(connection ne null, s"Missing connection")
      connection.uri(s"/scheduler/job/$jobName")
    }

    /**
     * Makes the equivalent call to `DELETE /scheduler/job/&lt;jobName&gt;` and provides the response at a future time.
     *
     * @param jobName name of a job
     * @param ignoreIfMissing consider a missing job to be a success
     * @param connection used to construct the full URI
     * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
     * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
     *         the request or an error
     */
    def delete(jobName: String, ignoreIfMissing: Boolean = false)(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, Boolean]] =
      httpDelete[Boolean](connection)(uriDelete(jobName))(processDelete(ignoreIfMissing))

    /**
     * Performs the response processing from a call to `DELETE /scheduler/job/&lt;jobName&gt;`.
     *
     * @param ignoreIfMissing consider a missing job to be a success
     * @param response should be empty if `statusCode` is a 204 (indicating success)
     * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
     */
    def processDelete(ignoreIfMissing: Boolean)(statusCode: Int, response: Array[Byte]): Validation[Error, Boolean] =
      successIf204(statusCode, response, ignoreOn400 = ignoreIfMissing)

    /**
     * Constructs a [[scala.Predef.String]] representing the URI for this resource.
     *
     * @param jobName name of a job
     * @param connection used to construct the full URI
     * @return a [[scala.Predef.String]] representing the URI for this resource
     */
    def uriStart(jobName: String)(connection: Connection): String = {
      require(connection ne null, s"Missing connection")
      connection.uri(s"/scheduler/job/$jobName")
    }

    /**
     * Makes the equivalent call to `PUT /scheduler/job/&lt;jobName&gt;` and provides the response at a future time.
     *
     * @param jobName name of a job
     * @param connection used to construct the full URI
     * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
     * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
     *         the request or an error
     */
    def start(jobName: String)(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, Boolean]] =
      httpPutAsJson[String, Boolean]("", connection)(uriStart(jobName))(processStart)

    /**
     * Performs the response processing from a call to `PUT /scheduler/job/&lt;jobName&gt;`.
     *
     * @param response should be empty if `statusCode` is a 204 (indicating success)
     * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
     */
    def processStart(statusCode: Int, response: Array[Byte]): Validation[Error, Boolean] =
      successIf204(statusCode, response)
  }

  object tasks {
    /**
     * Constructs a [[scala.Predef.String]] representing the URI for this resource.
     *
     * @param jobName name of a job
     * @param connection used to construct the full URI
     * @return a [[scala.Predef.String]] representing the URI for this resource
     */
    def uriKillAll(jobName: String)(connection: Connection): String = {
      require(connection ne null, s"Missing connection")
      connection.uri(s"/scheduler/task/kill/$jobName")
    }

    /**
     * Makes the equivalent call to `GET /scheduler/task/kill/&lt;jobName&gt;` and provides the response at a future time.
     *
     * @param jobName name of a job
     * @param connection used to construct the full URI
     * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
     * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
     *         the request or an error
     */
    def killAll(jobName: String)(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, Boolean]] =
      httpDelete[Boolean](connection)(uriKillAll(jobName))(processKillAll)

    /**
     * Performs the response processing from a call to `DELETE /scheduler/task/kill/&lt;jobName&gt;`.
     *
     * @param response should be empty if `statusCode` is a 204 (indicating success)
     * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
     */
    def processKillAll(statusCode: Int, response: Array[Byte]): Validation[Error, Boolean] =
      successIf204(statusCode, response)

    /**
     * Constructs a [[scala.Predef.String]] representing the URI for this resource.
     *
     * @param taskID Mesos task ID usually provided in the environment as `\$mesos_task_id`
     * @param connection used to construct the full URI
     * @return a [[scala.Predef.String]] representing the URI for this resource
     */
    def uriCompleted(taskID: String)(connection: Connection): String = {
      require(connection ne null, s"Missing connection")
      connection.uri(s"/scheduler/task/$taskID")
    }

    /**
     * Makes the equivalent call to `POST /scheduler/task/&lt;task id&gt;` and provides the response at a future time.
     *
     * @param taskID Mesos task ID usually provided in the environment as `\$mesos_task_id`
     * @param statusCode the completion status of the task
     * @param connection used to construct the full URI
     * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
     * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
     *         the request or an error
     */
    def completed(taskID: String, statusCode: Int)(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, Boolean]] =
      httpPutAsJson[TaskCompleted, Boolean](TaskCompleted(statusCode), connection)(uriCompleted(taskID))(processCompleted)

    /**
     * Performs the response processing from a call to `POST /scheduler/task/&lt;task id&gt;`.
     *
     * @param response should be empty if `statusCode` is a 204 (indicating success)
     * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
     */
    def processCompleted(statusCode: Int, response: Array[Byte]): Validation[Error, Boolean] =
      successIf204(statusCode, response)
  }

  object graphs {
    /**
     * Constructs a [[scala.Predef.String]] representing the URI for this resource.
     *
     * @param connection used to construct the full URI
     * @return a [[scala.Predef.String]] representing the URI for this resource
     */
    def uriDot(connection: Connection): String = {
      require(connection ne null, s"Missing connection")
      connection.uri(s"/scheduler/graph/dot")
    }

    /**
     * Makes the equivalent call to `GET /scheduler/graph/dot` and provides the response at a future time.
     *
     * @param connection used to construct the full URI
     * @param executor the [[scala.concurrent.ExecutionContext]] used to process the request
     * @return A [[scala.concurrent.Future]] with a scalaz [[scalaz.Validation]] object providing the results of
     *         the request or an error
     */
    def dot(implicit connection: Connection, executor: ExecutionContext = ExecutionContext.Implicits.global): Future[Validation[Error, String]] =
      httpGetPlainText[String](connection)(uriDot)(processDot)

    /**
     * Performs the processing of the payload from a call to `GET /scheduler/graph/dot`.
     *
     * @param response Plain text contents of a dot file representing the job dependency graph
     * @return a [[scalaz.Validation]] that can be composed using normal scalaz methods
     */
    def processDot(statusCode: Int, response: Array[Byte]): Validation[Error, String] =
      processSingleStringHttpGetResponse(statusCode, response)
  }
}

