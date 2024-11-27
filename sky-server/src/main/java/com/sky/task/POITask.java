package com.sky.task;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;

public class POITask {
    /** 写入 **/
    public static void write() throws IOException {
        // 内存中创建excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("info");
        // create a row,index from 0
        XSSFRow row = sheet.createRow(0);
        // write in a cell
        row.createCell(0).setCellValue("ID");
        sheet.createRow(2).createCell(0).setCellValue("1");
        // write in disk & real "write"
        FileOutputStream stream = new FileOutputStream(new File("C:\\info.xlsx"));
        workbook.write(stream);
        // close resource
        stream.close();
        workbook.close();
    }
    /** 读取 **/
    public static void read() throws IOException {
        // read
        XSSFWorkbook excel=new XSSFWorkbook(Files.newInputStream
                (new File("C:\\info.xlsx").toPath()));
        String s = excel.getSheet("info").getRow(0).getCell(0).getStringCellValue();
        System.out.println(s);
        excel.close();

    }
    public static void main(String[] args) throws IOException {
        write();
        read();
    }
}
