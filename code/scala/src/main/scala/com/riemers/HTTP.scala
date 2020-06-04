package com.riemers

import java.net.URL
import java.util.concurrent.TimeUnit

import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration.Duration
import zio.stream.{ZStream, ZTransducer}
import zio.{Has, Schedule, ZIO, ZLayer}

trait HTTP {
  def getBody(url: URL): ZStream[Any, Throwable, Byte]

  def getBodyAsString(url: URL): ZStream[Any, Throwable, String] =
    getBody(url).transduce(ZTransducer.utf8Decode)
}

object HTTP {

  val urlConnection: ZLayer[Blocking with Clock, Nothing, Has[HTTP]] = ZLayer.fromFunction[Blocking with Clock, HTTP] {
    blockingClock =>
      (url: URL) =>
        for {
          stream <- ZStream.bracket(ZIO.effect(url.openStream())
            .retry(Schedule.exponential(Duration(500, TimeUnit.MILLISECONDS))))(s => ZIO.effect(s.close()).ignore)
            .provide(blockingClock)
          bytes <- ZStream.fromInputStream(stream).provide(blockingClock)
        } yield bytes
  }

}
