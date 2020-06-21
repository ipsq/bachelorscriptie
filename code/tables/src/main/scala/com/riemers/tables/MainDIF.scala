package com.riemers.tables

object MainDIF {

  val header: String =
    """\begin{table}[H]
      |\centering
      |\sisetup{
      |round-mode=places,
      |round-precision=3,
      |table-number-alignment=left,
      |table-figures-integer=1,
      |table-figures-decimal=3
      |}
      |\caption{}
      |\label{tab:}
      |\begin{tabular}{llSSSSS}
      |\toprule
      |\multicolumn{2}{c}{Models} & \multicolumn{5}{c}{DM-test ($p$)} \\ \midrule
      |\multicolumn{2}{r}{Maturity} & \multicolumn{1}{l}{1 year} & \multicolumn{1}{l}{2 years} & \multicolumn{1}{l}{3 years}& \multicolumn{1}{l}{5 years} & \multicolumn{1}{l}{10 years} \\ \midrule""".stripMargin

  val footer: String =
    """\end{tabular}
      |\end{table}""".stripMargin

  val models: Vector[(String, String)] = Vector(
    ("DIF(1)", "DIF(1)+FB1(PCA)"),
    ("DIF(2)", "DIF(2)+FB1(PCA)"),
    ("DIF(3)", "DIF(3)+FB1(PCA)"),
    ("DIF(1)", "DIF(1)+FB2(PCA)"),
    ("DIF(2)", "DIF(2)+FB2(PCA)"),
    ("DIF(3)", "DIF(3)+FB2(PCA)"),
    ("DIF(1)", "DIF(1)+FB1(SPCA)"),
    ("DIF(2)", "DIF(2)+FB1(SPCA)"),
    ("DIF(3)", "DIF(3)+FB1(SPCA)"),
    ("DIF(1)", "DIF(1)+FB2(SPCA)"),
    ("DIF(2)", "DIF(2)+FB2(SPCA)"),
    ("DIF(3)", "DIF(3)+FB2(SPCA)"),
  )

  val data: String =
    """0.0758722346693216	0.406964546193230	0.597161527280640	0.708517970204481	0.834234335198092
      |0.0978017435645164	0.407650543787275	0.606044454581772	0.725725975819519	0.837399115616135
      |0.0107611090199382	0.152949441789593	0.329422989962075	0.455940830730177	0.521421140294575
      |0.0904254294196798	0.197857076788661	0.297921647691452	0.434664332093595	0.610197422245937
      |0.112778619717193	0.205880120917529	0.302613132041104	0.447487880745176	0.621069688933869
      |0.0563002270458819	0.111596246116463	0.160304924084739	0.227096263188580	0.386848621466922
      |0.131316734853822	0.417846146006645	0.582208916558231	0.702223786217046	0.799742741276072
      |0.152695525570449	0.396036488290417	0.557373891531642	0.676393564057329	0.756600594591534
      |0.0169949410591861	0.132141361873685	0.281709518239285	0.420596799059386	0.475005074184907
      |0.313468158338830	0.389903475375189	0.435340128199107	0.485139441132739	0.571873177020612
      |0.318416074253148	0.380239003798004	0.417293002441850	0.454769286765446	0.518694918126499
      |0.168619954240025	0.204777286939392	0.207283466810392	0.199368838199898	0.317518393315013""".stripMargin

  def main(args: Array[String]): Unit = {
    val sb = new StringBuilder
    sb.append(header)
    sb.append("\n")
    sb.append(models.zip(data.split("\n")).map {
      case ((a, b), str1) =>
        a + " & " + b + " & " + str1.split("\t").map(_.trim).mkString(" & ")
    }.mkString(" \\\\ \n"))
    sb.append(" \\\\ \\bottomrule \n")
    sb.append(footer)
    println(sb.toString())
  }

}
