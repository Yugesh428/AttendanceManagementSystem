package com.Features.Admin.Semester.excel;

import com.Features.Admin.Semester.DTO.SemesterDTO;
import com.exception.ExcelImportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

public class SemesterExcelHelper {

    public static final String SHEET = "Semesters";
    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // ── EXPORT ───────────────────────────────────────────────────────────
    public static ByteArrayInputStream export(List<SemesterDTO> semesters) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);

            String[] headers = {"ID", "Name", "Created At"};
            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = buildHeaderStyle(workbook, IndexedColors.LIGHT_CORNFLOWER_BLUE);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (SemesterDTO s : semesters) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(s.getId() != null ? s.getId().toString() : "");
                row.createCell(1).setCellValue(s.getName() != null ? s.getName() : "");
                row.createCell(2).setCellValue(s.getCreatedAt() != null ? s.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export semesters: " + e.getMessage(), e);
        }
    }

    // ── TEMPLATE ─────────────────────────────────────────────────────────
    public static ByteArrayInputStream template() {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);

            String[] headers = {"Name *"};
            Row row = sheet.createRow(0);

            CellStyle style = buildHeaderStyle(workbook, IndexedColors.LIGHT_GREEN);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
                sheet.autoSizeColumn(i);
            }

            // Sample row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Semester 1");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to create template: " + e.getMessage(), e);
        }
    }

    // ── VALIDATE FILE ────────────────────────────────────────────────────
    public static boolean hasExcelFormat(MultipartFile file) {
        return CONTENT_TYPE.equals(file.getContentType());
    }

    // ── IMPORT ───────────────────────────────────────────────────────────
    public static List<SemesterDTO> parseExcel(InputStream is) {

        try (Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheet(SHEET);
            if (sheet == null) sheet = workbook.getSheetAt(0);

            List<SemesterDTO> list = new ArrayList<>();
            Iterator<Row> rows = sheet.iterator();

            int rowNum = 0;

            while (rows.hasNext()) {
                Row row = rows.next();

                if (rowNum == 0) { // skip header
                    rowNum++;
                    continue;
                }

                if (isRowEmpty(row, 1)) {
                    rowNum++;
                    continue;
                }

                String name = getCellString(row, 0).trim();

                if (name.isBlank()) {
                    throw new ExcelImportException("Row " + (rowNum + 1) + ": Name is required");
                }

                list.add(SemesterDTO.builder()
                        .name(name)
                        .build());

                rowNum++;
            }

            if (list.isEmpty()) {
                throw new ExcelImportException("Excel file has no data");
            }

            return list;

        } catch (ExcelImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelImportException("Error reading Excel: " + e.getMessage());
        }
    }

    // ── UTIL ─────────────────────────────────────────────────────────────
    private static CellStyle buildHeaderStyle(Workbook wb, IndexedColors color) {

        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();

        font.setBold(true);
        style.setFont(font);

        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderBottom(BorderStyle.THIN);

        return style;
    }

    private static String getCellString(Row row, int col) {

        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static boolean isRowEmpty(Row row, int cols) {

        for (int i = 0; i < cols; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}