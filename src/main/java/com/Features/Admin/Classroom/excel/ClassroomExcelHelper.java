package com.Features.Admin.Classroom.excel;

import com.Features.Admin.Classroom.DTO.ClassroomDTO;
import com.Features.Admin.Classroom.model.ClassType;
import com.exception.ExcelImportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Export columns:  ID | Name | Class Type | Building ID | Building Name | Created At
 * Import columns:  Name | Class Type (LECTURE/TUTORIAL/PRACTICAL) | Building ID
 */
public class ClassroomExcelHelper {

    public static final String SHEET = "Classrooms";
    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final String VALID_TYPES = Arrays.stream(ClassType.values())
            .map(Enum::name).collect(Collectors.joining(", "));

    // ── Export ─────────────────────────────────────────────────────────────────
    public static ByteArrayInputStream export(List<ClassroomDTO> classrooms) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);

            CellStyle headerStyle = buildHeaderStyle(workbook, IndexedColors.LIGHT_CORNFLOWER_BLUE);
            String[] headers = {"ID", "Name", "Class Type", "Building ID", "Building Name", "Created At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ClassroomDTO c : classrooms) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getId() != null ? c.getId().toString() : "");
                row.createCell(1).setCellValue(c.getName() != null ? c.getName() : "");
                row.createCell(2).setCellValue(c.getClassType() != null ? c.getClassType().name() : "");
                row.createCell(3).setCellValue(c.getBuildingId() != null ? c.getBuildingId().toString() : "");
                row.createCell(4).setCellValue(c.getBuildingName() != null ? c.getBuildingName() : "");
                row.createCell(5).setCellValue(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export classrooms to Excel: " + e.getMessage(), e);
        }
    }

    // ── Import template ────────────────────────────────────────────────────────
    public static ByteArrayInputStream template() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);
            CellStyle style = buildHeaderStyle(workbook, IndexedColors.LIGHT_GREEN);

            String[] headers = {"Name *", "Class Type * (LECTURE/TUTORIAL/PRACTICAL)", "Building ID *"};
            Row row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
                sheet.autoSizeColumn(i);
            }

            // Sample row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Lab A");
            sample.createCell(1).setCellValue("PRACTICAL");
            sample.createCell(2).setCellValue("<paste-building-uuid-here>");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate classroom template: " + e.getMessage(), e);
        }
    }

    // ── Import ─────────────────────────────────────────────────────────────────
    public static boolean hasExcelFormat(MultipartFile file) {
        return CONTENT_TYPE.equals(file.getContentType());
    }

    public static List<ClassroomDTO> parseExcel(InputStream is) {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(SHEET);
            if (sheet == null) sheet = workbook.getSheetAt(0);

            List<ClassroomDTO> list = new ArrayList<>();
            Iterator<Row> rows = sheet.iterator();
            int rowNum = 0;

            while (rows.hasNext()) {
                Row row = rows.next();
                if (rowNum == 0) { rowNum++; continue; }
                if (isRowEmpty(row, 3)) { rowNum++; continue; }

                // ── Name ──────────────────────────────────────────────────────
                String name = getCellString(row, 0).trim();
                if (name.isBlank()) {
                    throw new ExcelImportException("Row " + (rowNum + 1) + ": 'Name' is required.");
                }

                // ── ClassType ─────────────────────────────────────────────────
                String typeStr = getCellString(row, 1).trim().toUpperCase();
                ClassType classType;
                try {
                    classType = ClassType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    throw new ExcelImportException(
                            "Row " + (rowNum + 1) + ": Invalid class type '" + typeStr
                            + "'. Valid values: " + VALID_TYPES);
                }

                // ── Building ID ───────────────────────────────────────────────
                String buildingIdStr = getCellString(row, 2).trim();
                if (buildingIdStr.isBlank()) {
                    throw new ExcelImportException("Row " + (rowNum + 1) + ": 'Building ID' is required.");
                }
                UUID buildingId;
                try {
                    buildingId = UUID.fromString(buildingIdStr);
                } catch (IllegalArgumentException e) {
                    throw new ExcelImportException(
                            "Row " + (rowNum + 1) + ": 'Building ID' is not a valid UUID → '" + buildingIdStr + "'");
                }

                list.add(ClassroomDTO.builder()
                        .name(name)
                        .classType(classType)
                        .buildingId(buildingId)
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

    private static boolean isRowEmpty(Row row, int cols) {
        for (int i = 0; i < cols; i++) {
            Cell c = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c != null && c.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
