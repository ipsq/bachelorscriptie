package com.riemers.csv

trait CSVParser {
  /**
   * Should parse the input or return with a MalformedCSVException
   *
   * @param input the input to parse
   * @return
   */
  def parse(input: String): Either[MalformedCSVException, Seq[String]]
}
