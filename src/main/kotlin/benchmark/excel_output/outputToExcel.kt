package benchmark.excel_output

import benchmark.BenchmarkResults
import java.io.FileOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook

fun outputToExcel(results: List<BenchmarkResults>) {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Sheet1")

    val currentIndex =
        sheetAddData(sheet, results, 0, "Prim's algorithm results") { it.algorithm == "Prim" }
    sheetAddData(sheet, results, currentIndex, "Dijkstra's algorithm results") {
        it.algorithm == "Dijkstra"
    }

    for (i in 0..results.size) sheet.autoSizeColumn(i)

    FileOutputStream("results.xlsx").use { fileOut -> workbook.write(fileOut) }
    workbook.close()
}
