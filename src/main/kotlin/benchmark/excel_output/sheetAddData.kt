package benchmark.excel_output

import benchmark.BenchmarkResults
import org.apache.poi.xssf.usermodel.XSSFSheet

fun sheetAddData(
    sheet: XSSFSheet,
    data: List<BenchmarkResults>,
    startingIndex: Int,
    title: String,
    condition: (BenchmarkResults) -> Boolean
): Int {
    var rowIndex = startingIndex
    val titleRow = sheet.createRow(rowIndex++)
    titleRow.createCell(0).setCellValue(title)

    val headerRow = sheet.createRow(rowIndex++)
    val headers = listOf("n", "p", "heap kind", "runtime")
    headers.forEachIndexed { index, header ->
        val cell = headerRow.createCell(index + 1)
        cell.setCellValue(header)
    }

    data.forEach { rowData ->
        if (condition(rowData)) {
            val row = sheet.createRow(rowIndex)
            rowData.toList().forEachIndexed { colIndex, cellData ->
                val cell = row.createCell(colIndex + 1)
                cell.setCellValue(cellData.toString())
            }
            rowIndex++
        }
    }

    return rowIndex + 1
}
