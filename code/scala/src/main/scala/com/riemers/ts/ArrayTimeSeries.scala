package com.riemers.ts

import java.time.LocalDate

import breeze.linalg.DenseMatrix

import scala.collection.mutable

/**
 * TODO: Add more checks
 * TODO: Add @inline
 *
 * @param indexes
 * @param columns
 * @param data
 * @param offset
 * @param majorStride
 * @param columnMajor
 */
class ArrayTimeSeries (val indexes: Vector[LocalDate], val columns: Vector[String], data: Array[Double],
                                  offset: Int, majorStride: Int, columnMajor: Boolean) extends TimeSeries {

  private def linearIndex(row: Int, col: Int): Int =
    if (columnMajor) offset + row + col * majorStride
    else offset + col + row * majorStride

  private def indexOf(date: LocalDate): Int = indexes.indexOf(date)

  private def indexOf(column: String): Int = column.indexOf(column)

  override def apply(row: Int, col: Int): Double =
    data(linearIndex(row, col))

  override def apply(date: LocalDate, column: String): Double =
    apply(indexOf(date), indexOf(column))

  override def filterColumns(f: String => Boolean): TimeSeries = {
    val buffer = mutable.Buffer.empty[Int]
    var i = 0
    while (i < cols) {
      val c = column(i)
      if (f(c)) {
        buffer.addOne(i)
      }
      i += 1
    }

    val newArray = new Array[Double](buffer.length * observations)
    var j = 0
    while (j < buffer.length) {
      if (columnMajor) System.arraycopy(data, linearIndex(0, buffer(j)), newArray, j * observations, observations)
      else {
        var k = 0
        while (k < observations) {
          newArray(j * observations + k) = data(linearIndex(k, buffer(j)))
          k += 1
        }
      }
      j += 1
    }
    new ArrayTimeSeries(indexes, columns, newArray, 0, majorStride, columnMajor)
  }

  override def filterIndexes(f: LocalDate => Boolean): TimeSeries = {
    val buffer = mutable.Buffer.empty[Int]
    var i = 0
    while (i < observations) {
      val in = index(i)
      if (f(in)) {
        buffer.addOne(i)
      }
      i += 1
    }

    val newArray = new Array[Double](buffer.length * cols)
    var j = 0
    while (j < buffer.length) {
      if (columnMajor) {
        var k = 0
        while (k < cols) {
          newArray(j * cols + k) = data(linearIndex(buffer(j), k))
          k += 1
        }
      } else System.arraycopy(data, linearIndex(buffer(j), 0), newArray, j * cols, cols)
      j += 1
    }
    new ArrayTimeSeries(indexes, columns, newArray, 0, majorStride, columnMajor)
  }

  override def resample(resample: TimeSeries.Resample): TimeSeries = ???

  override def isEmpty: Boolean = data.isEmpty

  override def toDenseMatrix: DenseMatrix[Double] =
    DenseMatrix.create(observations, cols, data, offset, majorStride, !columnMajor)

  override def deepCopy(): TimeSeries = {
    val newArray = Array.ofDim[Double](data.length)
    System.arraycopy(data, 0, newArray, 0, data.length)
    new ArrayTimeSeries(indexes, columns, newArray, offset, majorStride, columnMajor)
  }

  override def index(index: Int): LocalDate = indexes(index)

  override def column(index: Int): String = columns(index)

  override def observations: Int = indexes.length

  override def cols: Int = columns.length
}
