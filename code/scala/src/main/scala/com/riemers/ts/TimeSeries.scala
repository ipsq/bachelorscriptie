package com.riemers.ts

import java.time.LocalDate

import breeze.linalg.DenseMatrix
import com.riemers.ts.TimeSeries.Resample

/**
 * A time series representation
 */
trait TimeSeries {

  def filterColumns(f: String => Boolean): TimeSeries

  def filterIndexes(f: LocalDate => Boolean): TimeSeries

  def resample(resample: Resample): TimeSeries

  def truncateIndexes(from: LocalDate, to: LocalDate): TimeSeries =
    filterIndexes(date => from.compareTo(date) <= 0 && to.compareTo(date) >= 0)

  def isEmpty: Boolean

  def toDenseMatrix: DenseMatrix[Double]

  def deepCopy(): TimeSeries

  /**
   * Get a vector of the indexes
   *
   * @return
   */
  def indexes: Vector[LocalDate]

  /**
   * Get a vector of the columns
   * @return
   */
  def columns: Vector[String]

  /**
   * Get the index at index
   *
   * @param index
   * @return
   */
  def index(index: Int): LocalDate

  /**
   * Get the column name at index
   *
   * @param index
   * @return
   */
  def column(index: Int): String

  /**
   * Number of observations
   *
   * @return
   */
  def observations: Int

  /**
   * Number of columns
   *
   * @return
   */
  def cols: Int

  def apply(row: Int, col: Int): Double

  def apply(index: LocalDate, column: String): Double

  def valueAt(row: Int, col: Int): Double = apply(row, col)

  def valueAt(index: LocalDate, column: String): Double = apply(index, column)

  override def toString: String = {
    val sb = new StringBuilder
    sb.append("date")
    var i = 0
    while (i < cols) {
      sb.append(",")
      sb.append(column(i))
      i += 1
    }
    sb.append(System.lineSeparator())
    var j = 0
    while (j < observations) {
      sb.append(index(j).toString)
      var k = 0
      while (k < cols) {
        sb.append(",")
        sb.append(valueAt(j, k))
        k += 1
      }
      sb.append(System.lineSeparator())
      j += 1
    }
    sb.result()
  }

}

object TimeSeries {

  sealed trait Resample
  case object Daily extends Resample
  case object Monthly extends Resample
  case object Yearly extends Resample

  def make(sample: Vector[LocalDate], columns: Vector[String], data: Array[Array[Double]]): TimeSeries = {
    require(columns.length == data.length, "The dimension of the column names must match the number of column vectors")
    var i = 0
    while (i < columns.length) {
      require(data(i).length == sample.length, "The number of observations must match with the sample")
      i += 1
    }
    new NestedArrayTimeSeries(sample, columns, data)
  }

}
