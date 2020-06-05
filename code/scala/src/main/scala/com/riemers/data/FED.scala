package com.riemers.data

import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import breeze.linalg.DenseMatrix
import com.riemers.HTTP
import com.riemers.ts.{CsvToTimeSeriesParser, TimeSeries}
import zio._
import zio.console.Console
import zio.stream.{ZStream, ZTransducer}

trait FED {
  def getTimeSeries: ZIO[Any, Throwable, TimeSeries]

  def getData: ZStream[Any, Throwable, (LocalDate, Map[String, Double])]

  def getYields: ZStream[Any, Throwable, (LocalDate, Array[Double])]

  def getMonthlyYields: ZStream[Any, Throwable, (LocalDate, Array[Double])]

  def getMonthlyYieldsMatrix: ZIO[Any, Throwable, DenseMatrix[Double]]
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

    /**
     * Gets the full data set from the Federal Reserve.
     */
    override def getData: ZStream[Any, Throwable, (LocalDate, Map[String, Double])] = {
      http.getBodyAsString(url)
        .transduce(ZTransducer.splitLines)
        .drop(9)
        .transduce(dataWithDateTransducer(format))
    }

    /**
     * Filters the original data set to only contain the keys we care about.
     */
    override def getYields: ZStream[Any, Throwable, (LocalDate, Array[Double])] =
      mapToArrayWithKeys(yields, getData)

    /**
     * Limits the original sample to a monthly sample selecting the last observation
     * of each month.
     */
    override def getMonthlyYields: ZStream[Any, Throwable, (LocalDate, Array[Double])] = {
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

    /**
     * Folds the Array[Double] into a row major dense matrix.
     */
    override def getMonthlyYieldsMatrix: ZIO[Any, Throwable, DenseMatrix[Double]] =
      sampleFilter(getMonthlyYields)
        .run(denseMatrixSink(yields.length))

    override def getTimeSeries: ZIO[Any, Throwable, TimeSeries] = http.getBodyAsString(url)
      .transduce(ZTransducer.splitLines)
      .drop(9)
      .run(CsvToTimeSeriesParser.sink(format))
  }

  val live: ZLayer[Has[HTTP] with Console, Nothing, Has[FED]] = ZLayer.fromService[HTTP, FED] { http =>
    new Live(http)
  }

  def getData: ZStream[Has[FED], Throwable, (LocalDate, Map[String, Double])] =
    ZStream.accessStream(_.get.getData)

  def getYields: ZStream[Has[FED], Throwable, (LocalDate, Array[Double])] =
    ZStream.accessStream(_.get.getYields)

  def getMonthlyYields: ZStream[Has[FED], Throwable, (LocalDate, Array[Double])] =
    ZStream.accessStream(_.get.getMonthlyYields)

  def getMonthlyYieldsMatrix: ZIO[Has[FED], Throwable, DenseMatrix[Double]] =
    ZIO.accessM(_.get.getMonthlyYieldsMatrix)

  def getTimeSeries: ZIO[Has[FED], Throwable, TimeSeries] =
    ZIO.accessM(_.get.getTimeSeries)

}
