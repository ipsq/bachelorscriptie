package com.riemers.models

import breeze.linalg
import breeze.stats
import breeze.linalg.{DenseMatrix, DenseVector}

/**
 * Principal Component Analysis
 */
object PCA {

  def svd(matrix: DenseMatrix[Double], centered: Boolean): DenseVector[Double] = {
    val d = linalg.svd(matrix)
    d.U * d.S
  }

  def eig(matrix: DenseMatrix[Double], centered: Boolean) = ???

  def als(matrix: DenseMatrix[Double], centered: Boolean) = ???

}
