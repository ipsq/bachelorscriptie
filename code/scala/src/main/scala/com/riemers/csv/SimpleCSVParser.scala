package com.riemers.csv

import cats.syntax.either._

import scala.collection.immutable.ArraySeq

object SimpleCSVParser extends CSVParser {
  /**
   * Should parse the input or return with a MalformedCSVException
   *
   * @param input the input to parse
   * @return
   */
  override def parse(input: String): Either[MalformedCSVException, Seq[String]] =
    ArraySeq.unsafeWrapArray(input.split(',')).asRight
}
