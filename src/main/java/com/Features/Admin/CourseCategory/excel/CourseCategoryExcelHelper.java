package com.Features.Admin.CourseCategory.excel;

import com.Features.Admin.CourseCategory.DTO.CourseDTO;
import com.exception.ExcelImportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

public class CourseCategoryExcelHelper {


    public static final String SHEET = "CourseCategories";

    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";



    // ── EXPORT ───────────────────────────────────────────────────────────
    public static ByteArrayInputStream export(List<CourseDTO> courses) {


        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {


            Sheet sheet = workbook.createSheet(SHEET);


            String[] headers = {
                    "ID",
                    "Course Name",
                    "Course Code",
                    "Description",
                    "Created At",
                    "Updated At"
            };


            Row headerRow = sheet.createRow(0);


            CellStyle headerStyle =
                    buildHeaderStyle(
                            workbook,
                            IndexedColors.LIGHT_CORNFLOWER_BLUE
                    );



            for (int i = 0; i < headers.length; i++) {

                Cell cell = headerRow.createCell(i);

                cell.setCellValue(headers[i]);

                cell.setCellStyle(headerStyle);
            }




            int rowIdx = 1;


            for (CourseDTO c : courses) {


                Row row = sheet.createRow(rowIdx++);


                row.createCell(0)
                        .setCellValue(
                                c.getId() != null
                                        ? c.getId().toString()
                                        : ""
                        );


                row.createCell(1)
                        .setCellValue(
                                c.getCourseName() != null
                                        ? c.getCourseName()
                                        : ""
                        );


                row.createCell(2)
                        .setCellValue(
                                c.getCourseCode() != null
                                        ? c.getCourseCode()
                                        : ""
                        );


                row.createCell(3)
                        .setCellValue(
                                c.getDescription() != null
                                        ? c.getDescription()
                                        : ""
                        );


                row.createCell(4)
                        .setCellValue(
                                c.getCreatedAt() != null
                                        ? c.getCreatedAt().toString()
                                        : ""
                        );


                row.createCell(5)
                        .setCellValue(
                                c.getUpdatedAt() != null
                                        ? c.getUpdatedAt().toString()
                                        : ""
                        );

            }



            for (int i = 0; i < headers.length; i++) {

                sheet.autoSizeColumn(i);

            }



            workbook.write(out);


            return new ByteArrayInputStream(
                    out.toByteArray()
            );



        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to export course categories: "
                            + e.getMessage(),
                    e
            );
        }
    }





    // ── TEMPLATE ─────────────────────────────────────────────────────────
    public static ByteArrayInputStream template() {


        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {


            Sheet sheet = workbook.createSheet(SHEET);



            String[] headers = {
                    "Course Name *",
                    "Course Code *",
                    "Description"
            };



            Row row = sheet.createRow(0);



            CellStyle style =
                    buildHeaderStyle(
                            workbook,
                            IndexedColors.LIGHT_GREEN
                    );



            for (int i = 0; i < headers.length; i++) {


                Cell cell = row.createCell(i);

                cell.setCellValue(headers[i]);

                cell.setCellStyle(style);

                sheet.autoSizeColumn(i);
            }




            // Sample Row

            Row sample = sheet.createRow(1);


            sample.createCell(0)
                    .setCellValue(
                            "Bachelor of Computer Science"
                    );


            sample.createCell(1)
                    .setCellValue(
                            "BCS"
                    );


            sample.createCell(2)
                    .setCellValue(
                            "Computer Science related course"
                    );



            workbook.write(out);


            return new ByteArrayInputStream(
                    out.toByteArray()
            );



        } catch (IOException e) {


            throw new RuntimeException(
                    "Failed to create course template: "
                            + e.getMessage(),
                    e
            );
        }
    }





    // ── VALIDATE FILE ────────────────────────────────────────────────────

    public static boolean hasExcelFormat(MultipartFile file) {

        return CONTENT_TYPE.equals(
                file.getContentType()
        );
    }





    // ── IMPORT ───────────────────────────────────────────────────────────

    public static List<CourseDTO> parseExcel(InputStream is) {


        try (Workbook workbook = new XSSFWorkbook(is)) {


            Sheet sheet = workbook.getSheet(SHEET);


            if (sheet == null) {

                sheet = workbook.getSheetAt(0);

            }



            List<CourseDTO> list = new ArrayList<>();


            Iterator<Row> rows = sheet.iterator();


            int rowNum = 0;



            while (rows.hasNext()) {


                Row row = rows.next();



                if (rowNum == 0) {

                    rowNum++;

                    continue;
                }




                if (isRowEmpty(row, 3)) {

                    rowNum++;

                    continue;
                }




                String courseName =
                        getCellString(row, 0).trim();


                String courseCode =
                        getCellString(row, 1).trim();




                if (courseName.isBlank()) {


                    throw new ExcelImportException(
                            "Row "
                                    + (rowNum + 1)
                                    + ": Course Name is required"
                    );
                }




                if (courseCode.isBlank()) {


                    throw new ExcelImportException(
                            "Row "
                                    + (rowNum + 1)
                                    + ": Course Code is required"
                    );
                }





                list.add(
                        CourseDTO.builder()

                                .courseName(courseName)

                                .courseCode(courseCode)

                                .description(
                                        getCellString(row, 2)
                                                .trim()
                                )

                                .build()
                );



                rowNum++;

            }





            if (list.isEmpty()) {


                throw new ExcelImportException(
                        "Excel file has no data"
                );
            }




            return list;




        } catch (ExcelImportException e) {


            throw e;



        } catch (Exception e) {


            throw new ExcelImportException(
                    "Error reading Excel: "
                            + e.getMessage()
            );
        }
    }





    // ── UTIL ─────────────────────────────────────────────────────────────

    private static CellStyle buildHeaderStyle(
            Workbook wb,
            IndexedColors color
    ) {


        CellStyle style = wb.createCellStyle();


        Font font = wb.createFont();


        font.setBold(true);


        style.setFont(font);


        style.setFillForegroundColor(
                color.getIndex()
        );


        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );


        style.setBorderBottom(
                BorderStyle.THIN
        );


        return style;
    }





    private static String getCellString(Row row, int col) {


        Cell cell =
                row.getCell(
                        col,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
                );



        if (cell == null) return "";



        return switch (cell.getCellType()) {


            case STRING ->
                    cell.getStringCellValue();


            case NUMERIC ->
                    String.valueOf(
                            (long) cell.getNumericCellValue()
                    );


            case BOOLEAN ->
                    String.valueOf(
                            cell.getBooleanCellValue()
                    );


            default ->
                    "";
        };
    }





    private static boolean isRowEmpty(Row row, int cols) {


        for (int i = 0; i < cols; i++) {


            Cell cell =
                    row.getCell(
                            i,
                            Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
                    );



            if (cell != null &&
                    cell.getCellType() != CellType.BLANK) {


                return false;
            }
        }


        return true;
    }
}