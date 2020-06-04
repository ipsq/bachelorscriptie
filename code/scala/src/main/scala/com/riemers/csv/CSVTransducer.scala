package com.riemers.csv

import zio.stream.ZTransducer
import zio.{Chunk, IO, ZIO, ZRef}

object CSVTransducer {

  def withHeaders(parser: CSVParser): ZTransducer[Any, MalformedCSVException, String, Map[String, String]] = {
    def mapHeaders(in: Chunk[String], headers: Seq[String]): IO[MalformedCSVException, Chunk[Map[String, String]]] =
      in.mapM { line =>
        for {
          parsed <- ZIO.fromEither(parser.parse(line))
        } yield headers.zip(parsed).toMap
      }

    ZTransducer {
      ZRef.makeManaged[Option[Seq[String]]](None).map { state =>
        {
          case None =>
            ZIO.succeed(Chunk.empty)
          case Some(in) =>
            state.get.flatMap {
              case Some(headers) => mapHeaders(in, headers)
              case None =>
                ZIO.fromOption(in.headOption).foldM(
                  _ => ZIO.succeed(Chunk.empty),
                  head => for {
                    headers <- ZIO.fromEither(parser.parse(head))
                    _ <- state.set(Some(headers))
                    lines <- mapHeaders(in.drop(1), headers)
                  } yield lines
                )
            }
        }
      }
    }
  }

  def withoutHeaders(parser: CSVParser): ZTransducer[Any, MalformedCSVException, String, Seq[String]] =
    ZTransducer[String].mapM(line => ZIO.fromEither(parser.parse(line)))

}
