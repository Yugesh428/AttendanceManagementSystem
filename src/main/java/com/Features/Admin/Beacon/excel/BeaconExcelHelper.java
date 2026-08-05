package com.Features.Admin.Beacon.excel;

import com.Features.Admin.Beacon.DTO.BeaconDTO;
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
import java.util.UUID;

/**
 * Export columns:  ID | UUID | Major | Minor | Classroom ID | Classroom Name | Created At
 * Import columns:  UUID | Major | Minor | Classroom ID
 */
public class BeaconExcelHelper {

    public static final String SHEET = "Beacons";
    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // ── Export ─────────────────────────────────────────────────────────────────
    public static ByteArrayInputStream export(List<BeaconDTO> beacons) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);
            CellStyle headerStyle = buildHeaderStyle(workbook, IndexedColors.LIGHT_CORNFLOWER_BLUE);

            String[] headers = {"ID", "UUID", "Major", "Minor", "Classroom ID", "Classroom Name", "Created At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (BeaconDTO b : beacons) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(b.getId() != null ? b.getId().toString() : "");
                row.createCell(1).setCellValue(b.getUuid() != null ? b.getUuid() : "");
                row.createCell(2).setCellValue(b.getMajor() != null ? b.getMajor() : 0);
                row.createCell(3).setCellValue(b.getMinor() != null ? b.getMinor() : 0);
                row.createCell(4).setCellValue(b.getClassroomId() != null ? b.getClassroomId().toString() : "");
                row.createCell(5).setCellValue(b.getClassroomName() != null ? b.getClassroomName() : "");
                row.createCell(6).setCellValue(b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export beacons to Excel: " + e.getMessage(), e);
        }
    }

    // ── Import template ────────────────────────────────────────────────────────
    public static ByteArrayInputStream template() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);
            CellStyle style = buildHeaderStyle(workbook, IndexedColors.LIGHT_GREEN);

            String[] headers = {"UUID *", "Major *", "Minor *", "Classroom ID *"};
            Row row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
                sheet.autoSizeColumn(i);
            }

            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("A1B2C3D4-E5F6-7890-ABCD-EF1234567890");
            sample.createCell(1).setCellValue(1);
            sample.createCell(2).setCellValue(101);
            sample.createCell(3).setCellValue("<paste-classroom-uuid-here>");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate beacon template: " + e.getMessage(), e);
        }
    }

    // ── Import ─────────────────────────────────────────────────────────────────
    public static boolean hasExcelFormat(MultipartFile file) {
        return CONTENT_TYPE.equals(file.getContentType());
    }

    public static List<BeaconDTO> parseExcel(InputStream is) {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(SHEET);
            if (sheet == null) sheet = workbook.getSheetAt(0);

            List<BeaconDTO> list = new ArrayList<>();
            Iterator<Row> rows = sheet.iterator();
            int rowNum = 0;

            while (rows.hasNext()) {
                Row row = rows.next();
                if (rowNum == 0) { rowNum++; continue; }
                if (isRowEmpty(row, 4)) { rowNum++; continue; }

                // ── UUID ──────────────────────────────────────────────────────
                String uuid = getCellString(row, 0).trim();
                if (uuid.isBlank()) {
                    throw new ExcelImportException("Row " + (rowNum + 1) + ": 'UUID' is required.");
                }

                // ── Major ─────────────────────────────────────────────────────
                Integer major = getCellInt(row, 1, rowNum + 1, "Major");

                // ── Minor ─────────────────────────────────────────────────────
                Integer minor = getCellInt(row, 2, rowNum + 1, "Minor");

                // ── Classroom ID ──────────────────────────────────────────────
                String classroomIdStr = getCellString(row, 3).trim();
                if (classroomIdStr.isBlank()) {
                    throw new ExcelImportException("Row " + (rowNum + 1) + ": 'Classroom ID' is required.");
                }
                UUID classroomId;
                try {
                    classroomId = UUID.fromString(classroomIdStr);
                } catch (IllegalArgumentException e) {
                    throw new ExcelImportException(
                            "Row " + (rowNum + 1) + ": 'Classroom ID' is not a valid UUID → '" + classroomIdStr + "'");
                }

                list.add(BeaconDTO.builder()
                        .uuid(uuid)
                        .major(major)
                        .minor(minor)
                        .classroomId(classroomId)
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
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private static Integer getCellInt(Row row, int col, int rowNum, String fieldName) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            throw new ExcelImportException("Row " + rowNum + ": '" + fieldName + "' is required.");
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        String str = cell.getStringCellValue().trim();
        if (str.isBlank()) {
            throw new ExcelImportException("Row " + rowNum + ": '" + fieldName + "' is required.");
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new ExcelImportException(
                    "Row " + rowNum + ": '" + fieldName + "' must be an integer, got '" + str + "'");
        }
    }

    private static boolean isRowEmpty(Row row, int cols) {
        for (int i = 0; i < cols; i++) {
            Cell c = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c != null && c.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
