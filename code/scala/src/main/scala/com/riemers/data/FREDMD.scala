package com.riemers.data

import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import breeze.linalg.DenseMatrix
import com.riemers.HTTP
import zio._
import zio.stream.{ZStream, ZTransducer}

trait FREDMD {
  def getData: ZStream[Any, Throwable, (LocalDate, Map[String, Double])]

  def getMacroeconomicVariables: ZStream[Any, Throwable, (LocalDate, Array[Double])]

  def getMacroeconomicVariablesMatrix: ZIO[Any, Throwable, DenseMatrix[Double]]
}

/**
 * TODO: Create some kind of Time Series class containing sample, headers, and values backed by stream
 *    with parsing ability for columns
 */
object FREDMD {

  class Live(http: HTTP) extends FREDMD {
    val variableKeys = IndexedSeq("RPI", "W875RX1", "DPCERA3M086SBEA", "CMRMTSPLx", "RETAILx", "INDPRO", "IPFPNSS", "IPFINAL",
      "IPCONGD", "IPDCONGD", "IPNCONGD", "IPBUSEQ", "IPMAT", "IPDMAT", "IPNMAT", "IPMANSICS", "IPB51222S", "IPFUELS",
      "CUMFNS", "HWI", "HWIURATIO", "CLF16OV", "CE16OV", "UNRATE", "UEMPMEAN", "UEMPLT5", "UEMP5TO14", "UEMP15OV",
      "UEMP15T26", "UEMP27OV", "CLAIMSx", "PAYEMS", "USGOOD", "CES1021000001", "USCONS", "MANEMP", "DMANEMP",
      "NDMANEMP", "SRVPRD", "USTPU", "USWTRADE", "USTRADE", "USFIRE", "USGOVT", "CES0600000007", "AWOTMAN", "AWHMAN",
      "HOUST", "HOUSTNE", "HOUSTMW", "HOUSTS", "HOUSTW", "PERMIT", "PERMITNE", "PERMITMW", "PERMITS", "PERMITW",
      "ACOGNO", "AMDMNOx", "ANDENOx", "AMDMUOx", "BUSINVx", "ISRATIOx", "M1SL", "M2SL", "M2REAL", "BOGMBASE",
      "TOTRESNS", "NONBORRES", "BUSLOANS", "REALLN", "NONREVSL", "CONSPI", "S&P 500", "S&P: indust", "S&P div yield",
      "S&P PE ratio", "FEDFUNDS", "CP3Mx", "TB3MS", "TB6MS", "GS1", "GS5", "GS10", "AAA", "BAA", "COMPAPFFx",
      "TB3SMFFM", "TB6SMFFM", "T1YFFM", "T5YFFM", "T10YFFM", "AAAFFM", "BAAFFM", "TWEXAFEGSMTHx", "EXSZUSx", "EXJPUSx",
      "EXUSUKx", "EXCAUSx", "WPSFD49207", "WPSFD49502", "WPSID61", "WPSID62", "OILPRICEx", "PPICMM", "CPIAUCSL",
      "CPIAPPSL", "CPITRNSL", "CPIMEDSL", "CUSR0000SAC", "CUSR0000SAD", "CUSR0000SAS", "CPIULFSL", "CUSR0000SA0L2",
      "CUSR0000SA0L5", "PCEPI", "DDURRG3M086SBEA", "DNDGRG3M086SBEA", "DSERRG3M086SBEA", "CES0600000008",
      "CES2000000008", "CES3000000008", "UMCSENTx", "MZMSL", "DTCOLNVHFNM", "DTCTHFNM", "INVEST", "VXOCLSx")
    val url = new URL("https://s3.amazonaws.com/files.fred.stlouisfed.org/fred-md/monthly/current.csv")
    val format: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")

    override def getData: ZStream[Any, Throwable, (LocalDate, Map[String, Double])] = {
      http.getBodyAsString(url)
        .transduce(ZTransducer.splitLines)
        .zipWithIndex
        .filter {
          case (_, l) =>
            l != 1
        }
        .map(_._1)
        .transduce(dataWithDateTransducer(format))
    }

    override def getMacroeconomicVariables: ZStream[Any, Throwable, (LocalDate, Array[Double])] =
      mapToArrayWithKeys(variableKeys, getData)

    override def getMacroeconomicVariablesMatrix: ZIO[Any, Throwable, DenseMatrix[Double]] =
      sampleFilter(getMacroeconomicVariables)
        .run(denseMatrixSink(variableKeys.length))
  }

  val live: ZLayer[Has[HTTP], Nothing, Has[FREDMD]] = ZLayer.fromService[HTTP, FREDMD] { http =>
    new Live(http)
  }

  def getData: ZStream[Has[FREDMD], Throwable, (LocalDate, Map[String, Double])] =
    ZStream.accessStream(_.get.getData)

  def getMacroeconomicVariables: ZStream[Has[FREDMD], Throwable, (LocalDate, Array[Double])] =
    ZStream.accessStream(_.get.getMacroeconomicVariables)

  def getMacroeconomicVariablesMatrix: ZIO[Has[FREDMD], Throwable, DenseMatrix[Double]] =
    ZIO.accessM(_.get.getMacroeconomicVariablesMatrix)

}
