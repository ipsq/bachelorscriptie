package com.riemers.data

import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import breeze.linalg.DenseMatrix
import com.riemers.HTTP
import com.riemers.csv.{CSVTransducer, SimpleCSVParser}
import spire.math.Number
import zio.stream.{ZStream, ZTransducer}
import zio.{Has, ZIO, ZLayer}

import scala.util.Try

trait FREDMD {
  def getMacroeconomicVariables: ZStream[Any, Throwable, (LocalDate, Map[String, Option[Number]])]

  def getMacroeconomicVariablesMatrix: ZIO[Any, Throwable, DenseMatrix[Double]]
}

object FREDMD {

  class Live(http: HTTP) extends FREDMD {
    val url = new URL("https://s3.amazonaws.com/files.fred.stlouisfed.org/fred-md/monthly/current.csv")
    val format: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")

    override def getMacroeconomicVariables: ZStream[Any, Throwable, (LocalDate, Map[String, Option[Number]])] = {
      val lines = http.getBodyAsString(url).transduce(ZTransducer.splitLines)

      (lines.take(1) ++ lines.drop(2)) // Dropping the transform column
        .aggregate(CSVTransducer.withHeaders(SimpleCSVParser))
        .mapM { row =>
          for {
            dateField <- ZIO.fromOption(row.get("sasdate")).mapError(_ => new MissingFieldException("sasdate"))
            date <- ZIO.effect(LocalDate.parse(dateField, format))
          } yield (date, row.removed("sasdate").view.mapValues(v => Try(Number(v)).toOption).toMap)
        }
    }

    override def getMacroeconomicVariablesMatrix: ZIO[Any, Throwable, DenseMatrix[Double]] = ???
  }

  val live: ZLayer[Has[HTTP], Nothing, Has[FREDMD]] = ZLayer.fromService[HTTP, FREDMD] { http =>
    new Live(http)
  }

  val getMacroeconomicVariables: ZStream[Has[FREDMD], Throwable, (LocalDate, Map[String, Option[Number]])] =
    ZStream.accessStream(_.get.getMacroeconomicVariables)

}
