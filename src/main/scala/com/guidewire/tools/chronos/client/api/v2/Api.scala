package com.guidewire.tools.chronos.client.api.v2

import java.lang.{Boolean => JavaBoolean}

import com.guidewire.tools.chronos.client._
import com.guidewire.tools.chronos.client.api._

/**
 * Java-accessible API.
 */
object NonScalaApi {
  import java.util.{List => JavaList}
  import java.util.concurrent.{Future => JavaFuture}
  import scala.collection.JavaConverters._
  import scala.concurrent.ExecutionContext.Implicits._
  import FutureUtils._

  val scheduler = SchedulerApi
  val scheduler_jobs   = SchedulerApi.JobsApi
  val scheduler_tasks  = SchedulerApi.TasksApi
  val scheduler_graphs = SchedulerApi.GraphsApi

  val metrics = MetricsApi

  val debug = DebugApi

  object DebugApi {
    def ping(host: String): JavaFuture[JavaBoolean] =
      ping(host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

    def ping(host: String, port: Int): JavaFuture[JavaBoolean] =
      ping(host, port, DEFAULT_CHRONOS_SECURE)

    def ping(host: String, port: Int, secure: Boolean): JavaFuture[JavaBoolean] =
      toJavaFutureWithValidation(Chronos.debug.ping(Connection(host, port, secure)))(x => x)
  }

  object MetricsApi {
    def full(host: String): JavaFuture[ServerMetrics] =
      full(host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

    def full(host: String, port: Int): JavaFuture[ServerMetrics] =
      full(host, port, DEFAULT_CHRONOS_SECURE)

    def full(host: String, port: Int, secure: Boolean): JavaFuture[ServerMetrics] =
      toJavaFutureWithValidation(Chronos.metrics.full(Connection(host, port, secure)))(x => x)
  }

  object SchedulerApi {
    object JobsApi {
      def list(host: String): JavaFuture[JavaList[Job]] =
        list(host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

      def list(host: String, port: Int): JavaFuture[JavaList[Job]] =
        list(host, port, DEFAULT_CHRONOS_SECURE)

      def list(host: String, port: Int, secure: Boolean): JavaFuture[JavaList[Job]] =
        toJavaFutureWithValidation(Chronos.scheduler.jobs.list(Connection(host, port, secure)))(_.toSeq.asJava)

      def addScheduled(job: Job, host: String): JavaFuture[JavaBoolean] =
        addScheduled(job, host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

      def addScheduled(job: Job, host: String, port: Int): JavaFuture[JavaBoolean] =
        addScheduled(job, host, port, DEFAULT_CHRONOS_SECURE)

      def addScheduled(job: Job, host: String, port: Int, secure: Boolean): JavaFuture[JavaBoolean] =
        toJavaFutureWithValidation(Chronos.scheduler.jobs.addScheduled(job)(Connection(host, port, secure)))(x => x)

      def addDependent(job: Job, host: String): JavaFuture[JavaBoolean] =
        addDependent(job, host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

      def addDependent(job: Job, host: String, port: Int): JavaFuture[JavaBoolean] =
        addDependent(job, host, port, DEFAULT_CHRONOS_SECURE)

      def addDependent(job: Job, host: String, port: Int, secure: Boolean): JavaFuture[JavaBoolean] =
        toJavaFutureWithValidation(Chronos.scheduler.jobs.addDependent(job)(Connection(host, port, secure)))(x => x)

      def delete(jobName: String, host: String): JavaFuture[JavaBoolean] =
        delete(jobName, host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

      def delete(jobName: String, host: String, port: Int): JavaFuture[JavaBoolean] =
        delete(jobName, host, port, DEFAULT_CHRONOS_SECURE)

      def delete(jobName: String, host: String, port: Int, secure: Boolean): JavaFuture[JavaBoolean] =
        toJavaFutureWithValidation(Chronos.scheduler.jobs.delete(jobName)(Connection(host, port, secure)))(x => x)

      def start(jobName: String, host: String): JavaFuture[JavaBoolean] =
        start(jobName, host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

      def start(jobName: String, host: String, port: Int): JavaFuture[JavaBoolean] =
        start(jobName, host, port, DEFAULT_CHRONOS_SECURE)

      def start(jobName: String, host: String, port: Int, secure: Boolean): JavaFuture[JavaBoolean] =
        toJavaFutureWithValidation(Chronos.scheduler.jobs.start(jobName)(Connection(host, port, secure)))(x => x)
    }

    object TasksApi {
      def killAll(jobName: String, host: String): JavaFuture[JavaBoolean] =
        killAll(jobName, host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

      def killAll(jobName: String, host: String, port: Int): JavaFuture[JavaBoolean] =
        killAll(jobName, host, port, DEFAULT_CHRONOS_SECURE)

      def killAll(jobName: String, host: String, port: Int, secure: Boolean): JavaFuture[JavaBoolean] =
        toJavaFutureWithValidation(Chronos.scheduler.tasks.killAll(jobName)(Connection(host, port, secure)))(x => x)

      def completed(taskID: String, statusCode: Int, host: String): JavaFuture[JavaBoolean] =
        completed(taskID, statusCode, host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

      def completed(taskID: String, statusCode: Int, host: String, port: Int): JavaFuture[JavaBoolean] =
        completed(taskID, statusCode, host, port, DEFAULT_CHRONOS_SECURE)

      def completed(taskID: String, statusCode: Int, host: String, port: Int, secure: Boolean): JavaFuture[JavaBoolean] =
        toJavaFutureWithValidation(Chronos.scheduler.tasks.completed(taskID, statusCode)(Connection(host, port, secure)))(x => x)
    }

    object GraphsApi {
      def dot(host: String): JavaFuture[String] =
        dot(host, DEFAULT_CHRONOS_PORT, DEFAULT_CHRONOS_SECURE)

      def dot(host: String, port: Int): JavaFuture[String] =
        dot(host, port, DEFAULT_CHRONOS_SECURE)

      def dot(host: String, port: Int, secure: Boolean): JavaFuture[String] =
        toJavaFutureWithValidation(Chronos.scheduler.graphs.dot(Connection(host, port, secure)))(x => x)
    }
  }
}
