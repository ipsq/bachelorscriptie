package com.riemers.ts

import java.time.LocalDate

import breeze.linalg.DenseMatrix

import scala.collection.mutable

/**
 * Vector backed sample and column names and array backed data. Special care
 * is taken in order to optimize for speed with loss of immutability. Hence
 * the name. Additional methods will be added when the requirements for this
 * class change.
 *
 * TODO: Rewrite to make it row major, better csv performance
 *
 * @param sample  The sample for the time series
 * @param columns The column names for the time series
 * @param data    The data array for the time series
 */
//noinspection DuplicatedCode
//class NestedArrayTimeSeries private[ts](private var sample: Vector[LocalDate],
//                                        private var columns: Vector[String],
//                                        private var data: Array[Array[Double]]) extends TimeSeries {
//
//  override def filterColumns(f: String => Boolean): TimeSeries = {
//    if (isEmpty) this
//    else {
//      val buffer = mutable.Buffer.empty[Int]
//      val builder = Vector.newBuilder[String]
//      var i = 0
//      while (i < columns.length) {
//        val column = columns(i)
//        if (f(column)) {
//          buffer.addOne(i)
//          builder.addOne(column)
//        }
//        i += 1
//      }
//
//      val newArray = new Array[Array[Double]](columns.length - buffer.length)
//      var j = 0
//      var k = 0
//      while (j < columns.length && k < buffer.length) {
//        if (buffer(k) == j) {
//          newArray(k) = data(j)
//          k += 1
//        }
//        j += 1
//      }
//
//      new NestedArrayTimeSeries(sample, builder.result(), newArray)
//    }
//  }
//
//  override def mapColumns(f: String => String): TimeSeries = {
//    new NestedArrayTimeSeries(sample, columns.map(f), data)
//  }
//
//  override def filterSample(f: LocalDate => Boolean): TimeSeries = {
//    if (isEmpty) this
//    else {
//      val buffer = mutable.Buffer.empty[Int]
//      val builder = Vector.newBuilder[LocalDate]
//      var i = 0
//      while (i < sample.length) {
//        val date = sample(i)
//        if (f(date)) {
//          buffer.addOne(i)
//          builder.addOne(date)
//        }
//        i += 1
//      }
//
//      var j = 0
//      while (j < columns.length) {
//        val oldArray = data(j)
//        val newArray = new Array[Double](buffer.length)
//        var k = 0
//        var l = 0
//        while (k < sample.length && l < buffer.length) {
//          if (buffer(l) == k) {
//            newArray(l) = oldArray(k)
//            l += 1
//          }
//          k += 1
//        }
//        data(j) = newArray
//        j += 1
//      }
//
//      new NestedArrayTimeSeries(builder.result(), columns, data)
//    }
//  }
//
//  override def resampleMonthly: TimeSeries = {
//    if (isEmpty) this
//    else {
//      val buffer = mutable.Buffer.empty[Int]
//      var i = sample.length - 1
//      buffer.addOne(i)
//      while (i > 0) {
//        val current = sample(i)
//        val previous = sample(i - 1)
//        if (current.getMonthValue != previous.getMonthValue) {
//          buffer.addOne(i - 1)
//        }
//        i -= 1
//      }
//
//      var j = 0
//      val builder = Vector.newBuilder[LocalDate]
//      while (j < columns.length) {
//        val oldArray = data(j)
//        val newArray = new Array[Double](buffer.length)
//        var k = 0
//        var m = 0
//        while (k < sample.length && m < buffer.length) {
//          val n = buffer.length - 1 - m
//          if (buffer(n) == k) {
//            newArray(m) = oldArray(k)
//            if (j == 0) builder.addOne(sample(k))
//            m += 1
//          }
//          k += 1
//        }
//
//        data(j) = newArray
//        j += 1
//      }
//
//      new NestedArrayTimeSeries(builder.result(), columns, data)
//    }
//  }
//
//  override def truncateSample(from: LocalDate, to: LocalDate): TimeSeries = {
//    filterSample(date => from.compareTo(date) <= 0 && to.compareTo(date) >= 0)
//  }
//
//  override def toDenseMatrix: DenseMatrix[Double] = {
//    val cols = columns.length
//    val newArray = new Array[Double](sample.length * cols)
//    var i = 0
//    while (i < cols) {
//      System.arraycopy(data(i), 0, newArray, i * cols, cols)
//      i += 1
//    }
//    DenseMatrix.create[Double](sample.length, columns.length, newArray)
//  }
//
//  override def isEmpty: Boolean =
//    sample.isEmpty || columns.isEmpty
//
//  override def deepCopy(): TimeSeries = {
//    val newArray = Array.ofDim[Double](columns.length, sample.length)
//    var i = 0
//    while (i < columns.length) {
//      System.arraycopy(data(i), 0, newArray(i), 0, sample.length)
//      i += 1
//    }
//    new NestedArrayTimeSeries(sample, columns, newArray)
//  }
//}
