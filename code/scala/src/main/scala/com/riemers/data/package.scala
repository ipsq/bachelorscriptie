package com.riemers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import breeze.linalg.DenseMatrix
import zio.{Chunk, ZIO, ZRef}
import zio.stream.{ZSink, ZStream, ZTransducer}

import scala.collection.immutable.ArraySeq
import scala.collection.mutable

package object data {

  val sample: (LocalDate, LocalDate) =
    (LocalDate.of(1971, 8, 1), LocalDate.of(2020, 4, 30))

  def sampleFilter[R, E](stream: ZStream[R, E, (LocalDate, Array[Double])]): ZStream[R, E, (LocalDate, Array[Double])] =
    stream.filter {
      case (date, _) =>
        sample match {
          case (min, max) =>
            (date.compareTo(min) >= 0) && (date.compareTo(max) <= 0)
        }
    }

  def mapToArrayWithKeys[R, E](keys: IndexedSeq[String],
                               stream: ZStream[R, E, (LocalDate, Map[String, Double])]): ZStream[R, E, (LocalDate, Array[Double])] =
    stream.map {
      case (date, map) =>
        date -> {
          val arr = new Array[Double](keys.length)
          var i = 0
          keys.foreach { key =>
            if (!map.contains(key)) {
              println(key)
              println(map)
            }
            arr(i) = map(key)
            i += 1
          }
          arr
        }
    }

  def denseMatrixSink(cols: Int): ZSink[Any, Nothing, (LocalDate, Array[Double]), DenseMatrix[Double]] =
    ZSink.foldLeft[(LocalDate, Array[Double]), mutable.ArrayBuilder.ofDouble](new mutable.ArrayBuilder.ofDouble) {
      case (builder, (_, values)) =>
        builder.addAll(values)
    }.map { builder =>
      val doubles = builder.result()
      val rows = doubles.length / cols

      DenseMatrix.create(rows, cols, doubles, 0, cols, isTranspose = true)
    }

  def dataWithDateTransducer(format: DateTimeFormatter): ZTransducer[Any, Throwable, String, (LocalDate, Map[String, Double])] = {
    def process(chunk: Chunk[String], headers: Seq[String]): ZIO[Any, Throwable, Chunk[(LocalDate, Map[String, Double])]] =
      chunk.mapM { s =>
        s.split(',') match {
          case Array(dateField, rest@_*) => for {
            date <- ZIO.effect(LocalDate.parse(dateField, format))
            rest <- ZIO.effect(headers.zipAll(rest.map(_.toDoubleOption.getOrElse(Double.NaN)), "very weird", Double.NaN).toMap)
          } yield (date, rest)
        }
      }

    ZTransducer {
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
          case None => ZIO.effectTotal(Chunk.empty)
        }
      } yield push
    }
  }

}
