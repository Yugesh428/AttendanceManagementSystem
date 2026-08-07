package com.Features.Timetable.excel;

import com.Features.Timetable.dto.TimetableSlotDTO;
import com.Features.Timetable.model.DayOfWeek;
import com.exception.ExcelImportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Import columns (admin fills these):
 *   Teacher ID * | Subject ID * | Classroom ID * | Day * | Start Time * | End Time * |
 *   Effective From * | Effective To | Notes
 *
 * Time format: HH:mm  (e.g. 09:00, 14:30)
 * Date format: yyyy-MM-dd
 * Day values:  MONDAY / TUESDAY / WEDNESDAY / THURSDAY / FRIDAY / SATURDAY
 */
public class TimetableExcelHelper {

    public static final String SHEET = "Timetable";
    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String VALID_DAYS = Arrays.stream(DayOfWeek.values())
            .map(Enum::name).collect(Collectors.joining(", "));

    // ── Export ─────────────────────────────────────────────────────────────────
    public static ByteArrayInputStream export(List<TimetableSlotDTO> slots) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(SHEET);
            CellStyle hStyle = headerStyle(wb, IndexedColors.LIGHT_CORNFLOWER_BLUE);

            String[] headers = {
                "ID", "Teacher", "Subject", "Subject Code", "Classroom",
                "Day", "Start", "End", "Effective From", "Effective To", "Notes", "Created At"
            };
            Row hRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = hRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hStyle);
            }

            int idx = 1;
            for (TimetableSlotDTO s : slots) {
                Row row = sheet.createRow(idx++);
                row.createCell(0).setCellValue(s.getId() != null ? s.getId().toString() : "");
                row.createCell(1).setCellValue(s.getTeacherName() != null ? s.getTeacherName() : "");
                row.createCell(2).setCellValue(s.getSubjectName() != null ? s.getSubjectName() : "");
                row.createCell(3).setCellValue(s.getSubjectCode() != null ? s.getSubjectCode() : "");
                row.createCell(4).setCellValue(s.getClassroomName() != null ? s.getClassroomName() : "");
                row.createCell(5).setCellValue(s.getDayOfWeek() != null ? s.getDayOfWeek().name() : "");
                row.createCell(6).setCellValue(s.getStartTime() != null ? s.getStartTime().toString() : "");
                row.createCell(7).setCellValue(s.getEndTime() != null ? s.getEndTime().toString() : "");
                row.createCell(8).setCellValue(s.getEffectiveFrom() != null ? s.getEffectiveFrom().toString() : "");
                row.createCell(9).setCellValue(s.getEffectiveTo() != null ? s.getEffectiveTo().toString() : "");
                row.createCell(10).setCellValue(s.getNotes() != null ? s.getNotes() : "");
                row.createCell(11).setCellValue(s.getCreatedAt() != null ? s.getCreatedAt().toString() : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export timetable: " + e.getMessage(), e);
        }
    }

    // ── Template ───────────────────────────────────────────────────────────────
    public static ByteArrayInputStream template() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(SHEET);
            CellStyle hStyle = headerStyle(wb, IndexedColors.LIGHT_GREEN);

            String[] headers = {
                "Teacher ID *", "Subject ID *", "Classroom ID *",
                "Day * (" + VALID_DAYS + ")",
                "Start Time * (HH:mm)", "End Time * (HH:mm)",
                "Effective From * (yyyy-MM-dd)", "Effective To (yyyy-MM-dd)", "Notes"
            };
            Row hRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = hRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hStyle);
                sheet.setColumnWidth(i, 7000);
            }

            // Sample row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("<teacher-uuid>");
            sample.createCell(1).setCellValue("<subject-uuid>");
            sample.createCell(2).setCellValue("<classroom-uuid>");
            sample.createCell(3).setCellValue("MONDAY");
            sample.createCell(4).setCellValue("09:00");
            sample.createCell(5).setCellValue("10:00");
            sample.createCell(6).setCellValue("2026-01-01");
            sample.createCell(7).setCellValue("2026-06-30");
            sample.createCell(8).setCellValue("Optional notes");

            wb.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate timetable template: " + e.getMessage(), e);
        }
    }

    // ── Parse import ───────────────────────────────────────────────────────────
    public static boolean hasExcelFormat(MultipartFile file) {
        return CONTENT_TYPE.equals(file.getContentType());
    }

    public static List<TimetableSlotDTO> parseExcel(InputStream is) {
        try (Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheet(SHEET);
            if (sheet == null) sheet = wb.getSheetAt(0);

            List<TimetableSlotDTO> list = new ArrayList<>();
            Iterator<Row> rows = sheet.iterator();
            int rowNum = 0;

            while (rows.hasNext()) {
                Row row = rows.next();
                if (rowNum == 0) { rowNum++; continue; }
                if (isRowEmpty(row, 7)) { rowNum++; continue; }

                int r = rowNum + 1; // human-readable row number for error messages

                UUID teacherId   = parseUUID(getCellString(row, 0), r, "Teacher ID");
                UUID subjectId   = parseUUID(getCellString(row, 1), r, "Subject ID");
                UUID classroomId = parseUUID(getCellString(row, 2), r, "Classroom ID");
                DayOfWeek day    = parseDay(getCellString(row, 3), r);
                LocalTime start  = parseTime(getCellString(row, 4), r, "Start Time");
                LocalTime end    = parseTime(getCellString(row, 5), r, "End Time");
                LocalDate from   = parseDate(getCellString(row, 6), r, "Effective From");

                String toStr = getCellString(row, 7).trim();
                LocalDate to = toStr.isBlank() ? null : parseDate(toStr, r, "Effective To");

                if (!end.isAfter(start)) {
                    throw new ExcelImportException(
                            "Row " + r + ": End Time must be after Start Time.");
                }
                if (to != null && to.isBefore(from)) {
                    throw new ExcelImportException(
                            "Row " + r + ": Effective To must be on or after Effective From.");
                }

                list.add(TimetableSlotDTO.builder()
                        .teacherId(teacherId)
                        .subjectId(subjectId)
                        .classroomId(classroomId)
                        .dayOfWeek(day)
                        .startTime(start)
                        .endTime(end)
                        .effectiveFrom(from)
                        .effectiveTo(to)
                        .notes(getCellString(row, 8).trim())
                        .build());
                rowNum++;
            }

            if (list.isEmpty()) throw new ExcelImportException("The uploaded file contains no data rows.");
            return list;

        } catch (ExcelImportException e) { throw e; }
          catch (Exception e) { throw new ExcelImportException("Failed to parse Excel: " + e.getMessage()); }
    }

    // ── Parsing helpers ────────────────────────────────────────────────────────
    private static UUID parseUUID(String val, int row, String field) {
        if (val == null || val.isBlank())
            throw new ExcelImportException("Row " + row + ": '" + field + "' is required.");
        try { return UUID.fromString(val.trim()); }
        catch (IllegalArgumentException e) {
            throw new ExcelImportException("Row " + row + ": '" + field + "' is not a valid UUID → '" + val + "'");
        }
    }

    private static DayOfWeek parseDay(String val, int row) {
        if (val == null || val.isBlank())
            throw new ExcelImportException("Row " + row + ": 'Day' is required.");
        try { return DayOfWeek.valueOf(val.trim().toUpperCase()); }
        catch (IllegalArgumentException e) {
            throw new ExcelImportException(
                    "Row " + row + ": Invalid day '" + val + "'. Valid: " + VALID_DAYS);
        }
    }

    private static LocalTime parseTime(String val, int row, String field) {
        if (val == null || val.isBlank())
            throw new ExcelImportException("Row " + row + ": '" + field + "' is required.");
        try { return LocalTime.parse(val.trim()); }
        catch (DateTimeParseException e) {
            throw new ExcelImportException(
                    "Row " + row + ": '" + field + "' must be HH:mm format → '" + val + "'");
        }
    }

    private static LocalDate parseDate(String val, int row, String field) {
        try { return LocalDate.parse(val.trim()); }
        catch (DateTimeParseException e) {
            throw new ExcelImportException(
                    "Row " + row + ": '" + field + "' must be yyyy-MM-dd format → '" + val + "'");
        }
    }

    private static CellStyle headerStyle(Workbook wb, IndexedColors color) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont(); font.setBold(true);
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
