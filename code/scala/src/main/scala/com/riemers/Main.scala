package com.riemers

import com.riemers.data.FED
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.{ExitCode, ZIO, console}

object Main extends zio.App {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] = {
    val layer = Console.live >+>
      Clock.live >+>
      Blocking.live >+>
      HTTP.urlConnection >+>
      FED.live

    (for {
      ts <- FED.getTimeSeries
      _ <- console.putStrLn(ts.resampleMonthly.toString)
    } yield ())
      .exitCode
      .provideLayer(layer)
  }
}
