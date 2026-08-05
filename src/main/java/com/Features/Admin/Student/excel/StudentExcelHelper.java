package com.Features.Admin.Student.excel;

import com.Features.Admin.Student.DTO.StudentDTO;
import com.exception.ExcelImportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

public class StudentExcelHelper {

    public static final String SHEET = "Students";

    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // ── Export ────────────────────────────────────────────────────────────────
    public static ByteArrayInputStream export(List<StudentDTO> students) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);

            String[] headers = {
                    "ID", "First Name", "Last Name", "Email", "Phone",
                    "Father Name", "Father Phone",
                    "Mother Name", "Mother Phone",
                    "Guardian Name", "Guardian Phone",
                    "Created At"
            };

            CellStyle headerStyle = buildHeaderStyle(workbook, IndexedColors.LIGHT_CORNFLOWER_BLUE);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (StudentDTO s : students) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(s.getId() != null ? s.getId().toString() : "");
                row.createCell(1).setCellValue(s.getFirstName());
                row.createCell(2).setCellValue(s.getLastName());
                row.createCell(3).setCellValue(s.getEmail());
                row.createCell(4).setCellValue(s.getPhone());

                row.createCell(5).setCellValue(s.getFatherName());
                row.createCell(6).setCellValue(s.getFatherPhone());

                row.createCell(7).setCellValue(s.getMotherName());
                row.createCell(8).setCellValue(s.getMotherPhone());

                row.createCell(9).setCellValue(s.getGuardianName());
                row.createCell(10).setCellValue(s.getGuardianPhone());

                row.createCell(11).setCellValue(
                        s.getCreatedAt() != null ? s.getCreatedAt().toString() : ""
                );
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export students: " + e.getMessage(), e);
        }
    }

    // ── Template ──────────────────────────────────────────────────────────────
    public static ByteArrayInputStream template() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET);

            String[] headers = {
                    "First Name *", "Last Name",
                    "Email *", "Phone",
                    "Father Name", "Father Phone",
                    "Mother Name", "Mother Phone",
                    "Guardian Name", "Guardian Phone"
            };

            CellStyle style = buildHeaderStyle(workbook, IndexedColors.LIGHT_GREEN);

            Row row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
                sheet.autoSizeColumn(i);
            }

            // Sample row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("John");
            sample.createCell(1).setCellValue("Doe");
            sample.createCell(2).setCellValue("john@example.com");
            sample.createCell(3).setCellValue("9876543210");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate template: " + e.getMessage(), e);
        }
    }

    // ── Import ────────────────────────────────────────────────────────────────
    public static boolean hasExcelFormat(MultipartFile file) {
        return CONTENT_TYPE.equals(file.getContentType());
    }

    public static List<StudentDTO> parseExcel(InputStream is) {
        try (Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheet(SHEET);
            if (sheet == null) sheet = workbook.getSheetAt(0);

            List<StudentDTO> list = new ArrayList<>();
            Iterator<Row> rows = sheet.iterator();

            int rowNum = 0;

            while (rows.hasNext()) {
                Row row = rows.next();

                if (rowNum == 0) { rowNum++; continue; }
                if (isRowEmpty(row, 10)) { rowNum++; continue; }

                String firstName = getCellString(row, 0).trim();
                String email = getCellString(row, 2).trim();

                if (firstName.isBlank()) {
                    throw new ExcelImportException("Row " + (rowNum + 1) + ": First Name is required");
                }

                if (email.isBlank()) {
                    throw new ExcelImportException("Row " + (rowNum + 1) + ": Email is required");
                }

                StudentDTO dto = StudentDTO.builder()
                        .firstName(firstName)
                        .lastName(getCellString(row, 1))
                        .email(email)
                        .phone(getCellString(row, 3))
                        .fatherName(getCellString(row, 4))
                        .fatherPhone(getCellString(row, 5))
                        .motherName(getCellString(row, 6))
                        .motherPhone(getCellString(row, 7))
                        .guardianName(getCellString(row, 8))
                        .guardianPhone(getCellString(row, 9))
                        .build();

                list.add(dto);
                rowNum++;
            }

            if (list.isEmpty()) {
                throw new ExcelImportException("Excel file has no data");
            }

            return list;

        } catch (ExcelImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelImportException("Failed to parse Excel: " + e.getMessage());
        }
    }

    // ── Utils ─────────────────────────────────────────────────────────────────
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
            Cell c = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (c != null && c.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}