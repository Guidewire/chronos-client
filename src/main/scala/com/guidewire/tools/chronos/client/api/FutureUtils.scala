package com.guidewire.tools.chronos.client.api

import scala.util.{Failure, Success}
import scala.concurrent.{Future, ExecutionContext, ExecutionException}
import java.util.concurrent.{FutureTask, Callable}

import com.guidewire.tools.chronos.client._

/**
 *
 */
object FutureUtils {
  def toJavaFuture[TScalaType, TJavaType](f: Future[TScalaType])(map: (TScalaType => TJavaType))(implicit executor: ExecutionContext): java.util.concurrent.Future[TJavaType] = {
    val applied = f.map(map)

    val task = new FutureTask[TJavaType](new Callable[TJavaType] {
      def call(): TJavaType = applied.value.get match {
        case Success(v) => v
        case Failure(t) => throw new ExecutionException(s"Failure during execution", t)
        case _ => throw new ExecutionException(s"Failure during execution", null)
      }
    })

    applied.onComplete {
      case _ =>
        task.run()
    }

    task
  }

  def toJavaFutureWithValidation[TValidationType, TJavaType](f: Future[scalaz.Validation[Error, TValidationType]])(map: TValidationType => TJavaType)(implicit executor: ExecutionContext): java.util.concurrent.Future[TJavaType] =
    toJavaFuture(f) {
      case scalaz.Success(value) => map(value)
      case scalaz.Failure(fail) => throw new IllegalStateException(s"Error during execution: $fail", null)
    }

  def toJavaFutureWithValidationNoMap[TJavaType](f: Future[scalaz.Validation[Error, TJavaType]])(implicit executor: ExecutionContext): java.util.concurrent.Future[TJavaType] =
    toJavaFutureWithValidation[TJavaType, TJavaType](f)(x => x)(executor)
}
