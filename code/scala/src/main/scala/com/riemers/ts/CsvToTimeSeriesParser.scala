package com.riemers.ts

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import zio.{Chunk, Semaphore, ZIO, ZManaged, ZRef}
import zio.stream.ZSink
import zio.stream.ZSink.Push

import scala.collection.mutable

class CsvToTimeSeriesParser(format: DateTimeFormatter) {

  private val sample = Vector.newBuilder[LocalDate]
  private val headers = Vector.newBuilder[String]
  private var data: Array[mutable.ArrayBuilder[Double]] = _
  private var nHeaders = 0

  def parseHeaderLine(line: String): Unit = {
    var i = 0
    var start = 0
    var droppedFirst = false
    while (i < line.length) {
      val char = line.charAt(i)
      if (char == ',' || i == line.length - 1) {
        if (i == line.length - 1) i += 1
        if (droppedFirst) {
          headers.addOne(line.substring(start, i))
          nHeaders += 1
        }
        else droppedFirst = true
        start = i + 1
      }
      i += 1
    }
    data = Array.fill(nHeaders)(mutable.ArrayBuilder.make[Double])
  }

  def parseLine(line: String): Unit = {
    var i = 0
    var j = 0
    var start = 0
    var parsedDate = false
    while (i < line.length) {
      val char = line.charAt(i)
      if (char == ',' || i == line.length - 1) {
        if (i == line.length - 1) i += 1
        val str = line.substring(start, i)
        if (parsedDate) {
          val double = try {
            java.lang.Double.parseDouble(str)
          } catch {
            case _: Throwable => Double.NaN
          }
          data(j).addOne(double)
          j += 1
        } else {
          sample.addOne(LocalDate.parse(str, format))
          parsedDate = true
        }
        start = i + 1
      }
      i += 1
    }
  }

  def result(): TimeSeries = {
    val s = sample.result()
    val h = headers.result()
    val array = new Array[Array[Double]](h.length)
    var i = 0
    while (i < h.length) {
      array(i) = data(i).result()
      i += 1
    }
    new NestedArrayTimeSeries(s, h, array)
  }

}

object CsvToTimeSeriesParser {

  def sink(format: DateTimeFormatter): ZSink[Any, Nothing, String, TimeSeries] = ZSink {
    def parse(chunk: Chunk[String], semaphore: Semaphore, parser: CsvToTimeSeriesParser) =
      semaphore.withPermit(
        ZIO.foreach(chunk.tail) { line =>
          ZIO.effectTotal(parser.parseLine(line))
        }
      ) *> Push.more

    for {
      parser <- ZManaged.succeed(new CsvToTimeSeriesParser(format))
      semaphore <- Semaphore.make(1).toManaged_
      parsedHeaders <- ZRef.makeManaged(false)
      push = (in: Option[Chunk[String]]) => in match {
        case Some(chunk) =>
          parsedHeaders.get.flatMap {
            if (_)
              parse(chunk, semaphore, parser)
            else
              for {
                _ <- semaphore.withPermit(ZIO.effectTotal(parser.parseHeaderLine(chunk.head)))
                _ <- parsedHeaders.set(true)
                p <- parse(chunk.tail, semaphore, parser)
              } yield p
          }
        case None =>
          Push.emit(parser.result())
      }
    } yield push
  }

}
