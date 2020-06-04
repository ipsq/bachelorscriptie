package com.riemers.data

import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import breeze.linalg.DenseMatrix
import com.riemers.HTTP
import zio._
import zio.console.Console
import zio.stream.{ZStream, ZTransducer}

import scala.collection.immutable.ArraySeq
import scala.collection.mutable

trait FED {
  val getData: ZStream[Any, Throwable, (LocalDate, Map[String, Double])]

  val getYields: ZStream[Any, Throwable, (LocalDate, Array[Double])]

  val getMonthlyYields: ZStream[Any, Throwable, (LocalDate, Array[Double])]

  val getMonthlyYieldsMatrix: ZIO[Any, Throwable, DenseMatrix[Double]]
}

object FED {

  /**
   * TODO: Correct sample
   *
   * @param http HTTP Service
   */
  class Live(http: HTTP) extends FED {
    val yields: IndexedSeq[String] = (1 to 10).map("SVENY%02d".format(_))
    val url = new URL("https://www.federalreserve.gov/data/yield-curve-tables/feds200628.csv")
    val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override val getData: ZStream[Any, Throwable, (LocalDate, Map[String, Double])] = {
      def process(chunk: Chunk[String], headers: Seq[String]): ZIO[Any, Throwable, Chunk[(LocalDate, Map[String, Double])]] =
        chunk.mapM { s =>
          s.split(',') match {
            case Array(dateField, rest@_*) => for {
              date <- ZIO.effect(LocalDate.parse(dateField, format))
              rest <- ZIO.effect(headers.zip(rest.map(_.toDoubleOption.getOrElse(Double.NaN))).toMap)
            } yield (date, rest)
          }
        }

      val transducer: ZTransducer[Any, Throwable, String, (LocalDate, Map[String, Double])] = ZTransducer {
        for {
          ref <- ZRef.makeManaged[Option[Seq[String]]](None)
          push = (in: Option[Chunk[String]]) => in match {
            case Some(chunk) =>
              ref.get.flatMap {
                case Some(headers) => process(chunk, headers)
                case None =>
                  val headers = ArraySeq.unsafeWrapArray(chunk.head.split(',').drop(1))
                  for {
                    _ <- ref.set(Some(headers))
                    c <- process(chunk.tail, headers)
                  } yield c
              }
            case None => UIO(Chunk.empty)
          }
        } yield push
      }

      http.getBodyAsString(url)
        .transduce(ZTransducer.splitLines)
        .drop(9)
        .transduce(transducer)
    }

    override val getYields: ZStream[Any, Throwable, (LocalDate, Array[Double])] =
      getData.map {
        case (date, map) =>
          date -> {
            val arr = new Array[Double](yields.length)
            var i = 0
            yields.foreach { key =>
              arr(i) = map(key)
              i += 1
            }
            arr
          }
      }

    override val getMonthlyYields: ZStream[Any, Throwable, (LocalDate, Array[Double])] = {
      type P = (LocalDate, Array[Double])
      def process(chunk: Chunk[P], default: Option[P] = None): (Chunk[P], Option[P]) = {
        chunk.foldLeft[(Chunk[P], Option[P])]((Chunk.empty, default)) {
          case ((compressed, Some(lastP @ (lastDate, _))), p @ (date, _)) =>
            if (lastDate.getMonth.equals(date.getMonth)) {
              (compressed, Some(p))
            } else {
              (compressed + lastP, Some(p))
            }
          case ((compressed, None), p) =>
            (compressed, Some(p))
        }
      }

      val transducer: ZTransducer[Any, Nothing, P, P] = ZTransducer {
        for {
          ref <- ZRef.makeManaged[Option[(LocalDate, Array[Double])]](None)
          push = (in: Option[Chunk[(LocalDate, Array[Double])]]) => in match {
            case Some(chunk) =>
              ref.get.flatMap {
                case Some(last) =>
                  val (c, l) = process(chunk, Some(last))
                  ref.set(l) *> ZIO.succeed(c)
                case None =>
                  val (c, l) = process(chunk)
                  ref.set(l) *> ZIO.succeed(c)
              }
            case None =>
              ref.get.map {
                case Some(value) =>
                  Chunk.single(value)
                case None =>
                  Chunk.empty
              }
          }
        } yield push
      }

      getYields.filter {
        case (_, doubles) => doubles.forall(!_.isNaN)
      }.transduce(transducer)
    }

    override val getMonthlyYieldsMatrix: ZIO[Any, Throwable, DenseMatrix[Double]] =
      getMonthlyYields.fold(new mutable.ArrayBuilder.ofDouble) {
        case (builder, (_, values)) =>
          builder.addAll(values)
      }.map { builder =>
        val doubles = builder.result()
        val rows = doubles.length / yields.length
        val cols = yields.length

        DenseMatrix.create(rows, cols, doubles, 0, cols, isTranspose = true)
      }
  }

  val live: ZLayer[Has[HTTP] with Console, Nothing, Has[FED]] = ZLayer.fromService[HTTP, FED] { http =>
    new Live(http)
  }

  val getData: ZStream[Has[FED], Throwable, (LocalDate, Map[String, Double])] =
    ZStream.accessStream(_.get.getData)

  val getYields: ZStream[Has[FED], Throwable, (LocalDate, Array[Double])] =
    ZStream.accessStream(_.get.getYields)

  val getMonthlyYields: ZStream[Has[FED], Throwable, (LocalDate, Array[Double])] =
    ZStream.accessStream(_.get.getMonthlyYields)

  val getMonthlyYieldsMatrix: ZIO[Has[FED], Throwable, DenseMatrix[Double]] =
    ZIO.accessM(_.get.getMonthlyYieldsMatrix)

}
