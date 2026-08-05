package com.Features.Admin.Building.excel;

import com.Features.Admin.Building.BuildingDTO.BuildingDTO;
import com.exception.ExcelImportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Static helper – no Spring bean, just pure POI logic.
 *
 * Export columns:  ID | Name | Location | Created At
 * Import columns:  Name | Location          (ID / CreatedAt are generated, not imported)
 */
public class BuildingExcelHelper {

    public static final String SHEET = "Buildings";
    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // ── Export ─────────────────────────────────────────────────────────────────
    public static ByteArrayInputStream export(List<BuildingDTO> buildings) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);

            // ── Header style ───────────────────────────────────────────────────
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // ── Header row ─────────────────────────────────────────────────────
            String[] headers = {"ID", "Name", "Location", "Created At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data rows ──────────────────────────────────────────────────────
            int rowIdx = 1;
            for (BuildingDTO b : buildings) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(b.getId() != null ? b.getId().toString() : "");
                row.createCell(1).setCellValue(b.getName() != null ? b.getName() : "");
                row.createCell(2).setCellValue(b.getLocation() != null ? b.getLocation() : "");
                row.createCell(3).setCellValue(b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export buildings to Excel: " + e.getMessage(), e);
        }
    }

    // ── Import template ────────────────────────────────────────────────────────
    public static ByteArrayInputStream template() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Name *", "Location"};
            Row row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
                sheet.autoSizeColumn(i);
            }

            // Sample row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Main Block");
            sample.createCell(1).setCellValue("North Campus");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate building template: " + e.getMessage(), e);
        }
    }

    // ── Import ─────────────────────────────────────────────────────────────────
    public static boolean hasExcelFormat(MultipartFile file) {
        return CONTENT_TYPE.equals(file.getContentType());
    }

    public static List<BuildingDTO> parseExcel(InputStream is) {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(SHEET);
            if (sheet == null) sheet = workbook.getSheetAt(0);

            List<BuildingDTO> list = new ArrayList<>();
            Iterator<Row> rows = sheet.iterator();

            int rowNum = 0;
            while (rows.hasNext()) {
                Row row = rows.next();
                if (rowNum == 0) { rowNum++; continue; } // skip header

                // Skip fully empty rows
                if (isRowEmpty(row)) { rowNum++; continue; }

                String name = getCellString(row, 0);
                if (name.isBlank()) {
                    throw new ExcelImportException(
                            "Row " + (rowNum + 1) + ": 'Name' is required and cannot be empty.");
                }

                list.add(BuildingDTO.builder()
                        .name(name.trim())
                        .location(getCellString(row, 1).trim())
                        .build());
                rowNum++;
            }

            if (list.isEmpty()) {
                throw new ExcelImportException("The uploaded file contains no data rows.");
            }
            return list;

        } catch (ExcelImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelImportException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    // ── Util ───────────────────────────────────────────────────────────────────
    private static String getCellString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private static boolean isRowEmpty(Row row) {
        for (int i = 0; i < 2; i++) {
            Cell c = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c != null && c.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
