package com.guidewire.tools.chronos.client

/**
 *
 */
import org.scalatest.junit.JUnitRunner
import org.scalatest.{ParallelTestExecution, BeforeAndAfterAll, SeveredStackTraces, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers

import dispatch._, Defaults._

import play.api.libs.json._
import play.api.libs.functional._

import scalaz._
import com.guidewire.tools.chronos.client.api.v2.{Jobs, Schedule, Job, Chronos}
import org.joda.time.DateTime

@RunWith(classOf[JUnitRunner])
class BasicFunctionalitySuite extends FunSuite
                     with ParallelTestExecution
                     with ShouldMatchers
                     with SeveredStackTraces {
  import ClientScalaTest._

  test("Can connect to running instance") (ignoreIfHostNotUp { (host, port, secure) =>
    val jobs = blockAndValidateSuccess {
      Chronos.scheduler.jobs(Connection(host, port, secure))
    }

    for(job <- jobs) {
      job should not be null
      job.name should not be ""
      //println(s"${job.schedule.get.period}")
      //println(s"$job")
    }
  })

  test("Can ping running instance") (ignoreIfHostNotUp { (host, port, secure) =>
    val result = blockAndValidateSuccess {
      Chronos.debug.ping(Connection(host, port))
    }

    result should be (true)
  })

  test("Can request metrics") (ignoreIfHostNotUp { (host, port, secure) =>
    val metrics = blockAndValidateSuccess {
      Chronos.metrics.full(Connection(host, port, secure))
    }

    for(gauge <- metrics.gauges) {
      gauge should not be null
      gauge.name should not be ""
      //println(s"$gauge")
    }

    for(counter <- metrics.counters) {
      counter should not be null
      counter.name should not be ""
      //println(s"$counter")
    }

    for(histogram <- metrics.histograms) {
      histogram should not be null
      histogram.name should not be ""
      //println(s"$histogram")
    }

    for(meter <- metrics.meters) {
      meter should not be null
      meter.name should not be ""
      //println(s"$meter")
    }

    for(timer <- metrics.timers) {
      timer should not be null
      timer.name should not be ""
      //println(s"$timer")
    }
  })

  test("Can request dot file") (ignoreIfHostNotUp { (host, port, secure) =>
    val dot = blockAndValidateSuccess {
      Chronos.scheduler.graphs.dot(Connection(host, port, secure))
    }

    dot should not be null
    //println(s"$dot")
  })

  test("Can add simple scheduled job") (ignoreIfHostNotUp { (host, port, secure) =>
    val List(_, added, cleanup) = blockAndValidateSuccess {
      implicit val cn = Connection(host, port, secure)
      for {
         ensure <- Chronos.scheduler.jobs.delete("scalatest-scheduler-job-addScheduled", ignoreIfMissing = true)
          added <- Chronos.scheduler.jobs.addScheduled(Jobs.scheduled(
                       name     = s"scalatest-scheduler-job-addScheduled"
                     , command  = s"echo 'scalatest-scheduler-job-addScheduled' >> /tmp/chronos-client-scala-test.txt"
                     , schedule = Schedule(5L, DateTime.now, "PT10S".toPeriod)
                   ))
        cleanup <- Chronos.scheduler.jobs.delete("scalatest-scheduler-job-addScheduled")
      } yield List(ensure, added, cleanup)
    }

    withClue(s"[CLEANUP REQUIRED] Unable to fully process test on <$host:$port>: ") {
      added should be (true)
      cleanup should be (true)
    }
  })

  test("Can add simple dependent job") (ignoreIfHostNotUp { (host, port, secure) =>
    val List(_, _, added1, added2, cleanup1, cleanup2) = blockAndValidateSuccess {
      implicit val cn = Connection(host, port, secure)
      for {
        ensure1 <- Chronos.scheduler.jobs.delete("scalatest-scheduler-job-adddependent-001", ignoreIfMissing = true)
        ensure2 <- Chronos.scheduler.jobs.delete("scalatest-scheduler-job-adddependent", ignoreIfMissing = true)
         added1 <- Chronos.scheduler.jobs.addScheduled(Jobs.scheduled(
                       name     = s"scalatest-scheduler-job-adddependent"
                     , command  = s"echo 'scalatest-scheduler-job-adddependent' >> /tmp/chronos-client-scala-test.txt"
                     , schedule = Schedule(5L, DateTime.now, "PT10S".toPeriod)
                   ))
         added2 <- Chronos.scheduler.jobs.addDependent(Jobs.dependent(
                       name     = s"scalatest-scheduler-job-adddependent-001"
                     , command  = s"echo 'scalatest-scheduler-job-adddependent-001' >> /tmp/chronos-client-scala-test.txt"
                     , parents  = Set("scalatest-scheduler-job-adddependent")
                   ))
        cleanup1 <- Chronos.scheduler.jobs.delete("scalatest-scheduler-job-adddependent-001")
        cleanup2 <- Chronos.scheduler.jobs.delete("scalatest-scheduler-job-adddependent")
      } yield List(ensure1, ensure2, added1, added2, cleanup1, cleanup2)
    }

    withClue(s"[CLEANUP REQUIRED] Unable to fully process test on <$host:$port>: ") {
      added1 should be (true)
      added2 should be (true)
      cleanup1 should be (true)
      cleanup2 should be (true)
    }
  })

  test("Can delete all tasks for a simple job") (ignoreIfHostNotUp { (host, port, secure) =>
    val List(_, added, deleted, cleanup) = blockAndValidateSuccess {
      implicit val cn = Connection(host, port, secure)
      for {
         ensure <- Chronos.scheduler.jobs.delete("scalatest-scheduler-tasks-killAll", ignoreIfMissing = true)
          added <- Chronos.scheduler.jobs.addScheduled(Jobs.scheduled(
                       name     = s"scalatest-scheduler-tasks-killAll"
                     , command  = s"echo 'scalatest-scheduler-tasks-killAll'"
                     , schedule = Schedule(5L, DateTime.now, "PT10S".toPeriod)
                   ))
        deleted <- Chronos.scheduler.tasks.killAll("scalatest-scheduler-tasks-killAll")
        cleanup <- Chronos.scheduler.jobs.delete("scalatest-scheduler-tasks-killAll")
      } yield List(ensure, added, deleted, cleanup)
    }

    withClue(s"[CLEANUP REQUIRED] Unable to fully process test on <$host:$port>: ") {
      added should be (true)
      deleted should be (true)
      cleanup should be (true)
    }
  })

  test("Can manually start a job") (ignoreIfHostNotUp { (host, port, secure) =>
    val List(_, added, started, cleanup) = blockAndValidateSuccess {
      implicit val cn = Connection(host, port, secure)
      for {
         ensure <- Chronos.scheduler.jobs.delete("scalatest-scheduler-jobs-start", ignoreIfMissing = true)
          added <- Chronos.scheduler.jobs.addScheduled(Job(
                       name     = s"scalatest-scheduler-jobs-start"
                     , command  = s"echo 'scalatest-scheduler-jobs-start'"
                     , schedule = Schedule(5L, DateTime.now, "PT10S".toPeriod)
                   ))
        started <- Chronos.scheduler.jobs.start("scalatest-scheduler-jobs-start")
        cleanup <- Chronos.scheduler.jobs.delete("scalatest-scheduler-jobs-start")
      } yield List(ensure, added, started, cleanup)
    }

    withClue(s"[CLEANUP REQUIRED] Unable to fully process test on <$host:$port>: ") {
      added should be (true)
      started should be (true)
      cleanup should be (true)
    }
  })

  for(i <- 1 to 1)
    test(f"Can deserialize simple scheduler jobs (/chronos-scheduler-jobs-${i}%03d.json)")(validateResourceParse(f"/chronos-scheduler-jobs-${i}%03d.json")(api.v2.Scheduler.jobs.processList))

  for(i <- 1 to 1)
    test(f"Can deserialize simple metrics (/chronos-metrics-${i}%03d.json)")(validateResourceParse(f"/chronos-metrics-${i}%03d.json")(api.v2.Chronos.metrics.processFull))
}
