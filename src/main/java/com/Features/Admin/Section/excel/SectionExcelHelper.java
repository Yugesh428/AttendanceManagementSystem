package com.Features.Admin.Section.excel;

import com.Features.Admin.Section.DTO.SectionDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SectionExcelHelper {

    private static final String[] HEADERS = {
            "ID", "Name", "Description", "Capacity", "Semester ID", "Created At"
    };

    // ── EXPORT ────────────────────────────────────────────────────────────────
    public static ByteArrayInputStream exportToExcel(List<SectionDTO> sections) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Sections");

            // Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
            }

            // Data
            int rowIdx = 1;
            for (SectionDTO dto : sections) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(
                        dto.getId() != null ? dto.getId().toString() : "");

                row.createCell(1).setCellValue(dto.getName());
                row.createCell(2).setCellValue(
                        dto.getDescription() != null ? dto.getDescription() : "");

                row.createCell(3).setCellValue(
                        dto.getCapacity() != null ? dto.getCapacity() : 0);

                row.createCell(4).setCellValue(
                        dto.getSemesterId() != null ? dto.getSemesterId().toString() : "");

                row.createCell(5).setCellValue(
                        dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : "");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export sections to Excel", e);
        }
    }

    // ── TEMPLATE ──────────────────────────────────────────────────────────────
    public static ByteArrayInputStream downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Template");

            Row headerRow = sheet.createRow(0);

            headerRow.createCell(0).setCellValue("Name *");
            headerRow.createCell(1).setCellValue("Description");
            headerRow.createCell(2).setCellValue("Capacity");
            headerRow.createCell(3).setCellValue("Semester ID *");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate template", e);
        }
    }

    // ── IMPORT ────────────────────────────────────────────────────────────────
    public static List<SectionDTO> importFromExcel(MultipartFile file) {
        List<SectionDTO> list = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;

            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header
                if (rowNumber++ == 0) continue;

                String name = getCellValue(currentRow.getCell(0));
                String description = getCellValue(currentRow.getCell(1));
                String capacityStr = getCellValue(currentRow.getCell(2));
                String semesterIdStr = getCellValue(currentRow.getCell(3));

                if (name.isEmpty()) {
                    throw new RuntimeException("Row " + rowNumber + ": Name is required");
                }

                if (semesterIdStr.isEmpty()) {
                    throw new RuntimeException("Row " + rowNumber + ": Semester ID is required");
                }

                SectionDTO dto = SectionDTO.builder()
                        .name(name)
                        .description(description)
                        .capacity(capacityStr.isEmpty() ? null : Integer.parseInt(capacityStr))
                        .semesterId(UUID.fromString(semesterIdStr))
                        .build();

                list.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import sections: " + e.getMessage(), e);
        }

        return list;
    }

    // ── UTIL ──────────────────────────────────────────────────────────────────
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}