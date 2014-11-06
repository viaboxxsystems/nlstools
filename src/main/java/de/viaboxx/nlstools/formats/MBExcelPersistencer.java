package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.io.*;
import java.util.*;

/**
 * Description: Load from and save bundles to an excel file <br>
 * User: roman.stumm<br>
 * Date: 29.12.2010<br>
 * Time: 14:44:37<br>
 * License: Apache 2.02010
 */
public class MBExcelPersistencer extends MBPersistencer {
    private static final int STYLE_BOLD = 1;
    private static final int STYLE_ITALIC = 2;
    private static final int STYLE_REVIEW = 3;
    private static final int STYLE_MISSING = 4;
    private static final int STYLE_DATETIME = 5;

    private HSSFWorkbook wb;
    private HSSFSheet sheet;
    private int rowNum = 0;
    private final Map<Integer, CellStyle> styles = new HashMap<Integer, CellStyle>();
    private BundleWriterExcel bundleWriter;

    public MBExcelPersistencer() {
    }

    private void initStyles(HSSFWorkbook wb) {
        // cache styles used to write text into cells
        HSSFCellStyle style = wb.createCellStyle();
        HSSFFont font = wb.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        styles.put(STYLE_BOLD, style);

        style = wb.createCellStyle();
        font = wb.createFont();
        font.setItalic(true);
        style.setFont(font);
        styles.put(STYLE_ITALIC, style);


        style = wb.createCellStyle();
        font = wb.createFont();
        font.setItalic(true);
        font.setColor(Font.COLOR_RED);
        style.setFont(font);
        styles.put(STYLE_REVIEW, style);

        style = wb.createCellStyle();
        style.setFillPattern(HSSFCellStyle.FINE_DOTS);
        style.setFillBackgroundColor(HSSFColor.BLUE_GREY.index);
        style.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
        styles.put(STYLE_MISSING, style);

        style = wb.createCellStyle();
        HSSFCreationHelper createHelper = wb.getCreationHelper();
        style.setDataFormat(
            createHelper.createDataFormat().getFormat("yyyy-dd-mm hh:mm"));
        styles.put(STYLE_DATETIME, style);
    }

    private int writeHeaders(MBBundle bundle) throws IOException {
        HSSFRow headerRow = createRow();
        HSSFCell cell = headerRow.createCell(0);
        cell.setCellStyle(styles.get(STYLE_BOLD));
        cell.setCellValue("Bundle:");

        cell = headerRow.createCell(1);
        cell.setCellStyle(styles.get(STYLE_BOLD));
        cell.setCellValue(bundle.getBaseName());

        cell = headerRow.createCell(3);
        cell.setCellValue("Created: ");
        cell = headerRow.createCell(4);
        cell.setCellValue(new Date());
        cell.setCellStyle(styles.get(STYLE_DATETIME));

        headerRow = createRow();
        if (null != bundle.getInterfaceName()) {
            cell = headerRow.createCell(0);
            cell.setCellStyle(styles.get(STYLE_ITALIC));
            cell.setCellValue("Interface:");

            cell = headerRow.createCell(1);
            cell.setCellStyle(styles.get(STYLE_ITALIC));
            cell.setCellValue(bundle.getInterfaceName());
        }

        if (null != bundle.getSqldomain()) {
            cell = headerRow.createCell(2);
            cell.setCellStyle(styles.get(STYLE_ITALIC));
            cell.setCellValue("SQLDomain:");

            cell = headerRow.createCell(3);
            cell.setCellStyle(styles.get(STYLE_ITALIC));
            cell.setCellValue(bundle.getSqldomain());
        }

        rowNum++; // empty row
        headerRow = createRow();
        String[] headerCols = {"Key", "Description"};
        for (int i = 0; i < headerCols.length; i++) {
            HSSFCell headerCell = headerRow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headerCols[i]);
            headerCell.setCellStyle(styles.get(STYLE_BOLD));
            headerCell.setCellValue(text);
        }
        int colNum = headerCols.length;
        int firstCol = colNum;

        List<String> locales = bundleWriter.getLocalesUsed();
        for (String each : locales) {
            HSSFCell headerCell = headerRow.createCell(colNum++);
            HSSFRichTextString text = new HSSFRichTextString(each);
            headerCell.setCellStyle(styles.get(STYLE_BOLD));
            headerCell.setCellValue(text);
        }
        return firstCol;
    }

    private void writeRows(MBBundle bundle, int firstCol) throws IOException {
        List<String> locales = bundleWriter.getLocalesUsed();
        for (MBEntry entry : bundle.getEntries()) {
            HSSFRow row = createRow();

            HSSFCell cell = row.createCell(0);
            cell.setCellValue(new HSSFRichTextString(entry.getKey()));

            if (entry.getDescription() != null) {
                cell = row.createCell(1);
                cell.setCellValue(new HSSFRichTextString(entry.getDescription()));
            }

            int colNum = firstCol;
            for (String each : locales) {
                MBText text = entry.getText(each);
                if (text != null) {
                    cell = row.createCell(colNum);
                    cell.setCellValue(new HSSFRichTextString(text.getValue()));
                    if (text.isReview()) {
                        cell.setCellStyle(styles.get(STYLE_REVIEW));
                    } else if (text.getValue() == null || text.getValue().length() == 0) {
                        cell.setCellStyle(styles.get(STYLE_MISSING));
                    }
                }
                colNum++;
            }
        }
    }

    private HSSFRow createRow() {
        HSSFRow row = sheet.createRow(rowNum);
        rowNum++;
        return row;
    }

    public void save(MBBundles obj, File target) throws IOException {
        mkdirs(target);
        OutputStream out = new FileOutputStream(target);
        try {
            wb = new HSSFWorkbook();
            initStyles(wb);
            for (MBBundle bundle : obj.getBundles()) {
                bundleWriter = new BundleWriterExcel(bundle);
                rowNum = 0; // FIX for Issue 2: Row numbering is not reset if an xls file has many tabs
                sheet = wb.createSheet(
                    bundle.getBaseName().replace('/', '.'));  //  '/' not allowed by excel in sheet name
                writeRows(bundle, writeHeaders(bundle));
            }
            wb.write(out);
        } finally {
            rowNum = 0;
            out.close();
        }
    }

    public MBBundles load(File source) throws IOException, ClassNotFoundException {
        InputStream in = new FileInputStream(source);
        try {
            return load(in);
        } finally {
            in.close();
        }
    }

    public MBBundles load(InputStream in) throws IOException, ClassNotFoundException {
        try {
            wb = new HSSFWorkbook(in);
            MBBundles bundles = new MBBundles();
            int sheetIdx = 0;
            sheet = wb.getSheetAt(sheetIdx++);
            while (sheet != null) {
                MBBundle bundle = new MBBundle();
                if (readSheet(bundle)) {
                    bundles.getBundles().add(bundle);
                }
                if (wb.getNumberOfSheets() > sheetIdx) {
                    sheet = wb.getSheetAt(sheetIdx++);
                } else {
                    sheet = null;
                }
            }
            return bundles;
        } finally {
            rowNum = 0;
        }
    }

    private boolean readSheet(MBBundle bundle) {
        if (sheet.getLastRowNum() == 0) return false;

        HSSFRow row = sheet.getRow(0);
        if (row.getLastCellNum() < 1 || row.getCell(1) == null) return false;
        bundle.setBaseName(getStringValue(row.getCell(1)));

        row = sheet.getRow(1);
        if (row != null) {
            if (row.getCell(1) != null) {
                bundle.setInterfaceName(getStringValue(row.getCell(1)));
            }

            if (row.getCell(3) != null) {
                bundle.setSqldomain(getStringValue(row.getCell(3)));
            }
        }
        int firstCol = 2;

        rowNum = 3;
        row = sheet.getRow(rowNum++); // read locales
        int colNum = firstCol;

        List<String> locales = new ArrayList<String>();

        HSSFCell cell = row.getCell(colNum++);
        while (colNum <= row.getLastCellNum()) {
            if (cell != null) {
                locales.add(getStringValue(cell));
            }
            if (row.getLastCellNum() >= colNum) {
                cell = row.getCell(colNum++);
            } else {
                cell = null;
            }
        }

        row = sheet.getRow(rowNum++);
        while (row != null) {
            if (row.getCell(0) != null) {
                MBEntry entry = new MBEntry();
                bundle.getEntries().add(entry);
                entry.setKey(getStringValue(row.getCell(0)));
                if (row.getCell(1) != null) {
                    entry.setDescription(getStringValue(row.getCell(1)));
                }
                colNum = firstCol;
                for (String each : locales) {
                    cell = row.getCell(colNum++);
                    if (cell != null) {
                        final String svalue = getStringValue(cell);
                        if (StringUtils.isNotEmpty(svalue) ||
                            // detect STYLE_MISSING
                            cell.getCellStyle().getFillBackgroundColor() == HSSFColor.BLUE_GREY.index ||
                            cell.getCellStyle().getFillForegroundColor() == HSSFColor.BLUE_GREY.index) {
                            MBText text = new MBText();
                            text.setLocale(each);
                            text.setValue(svalue);
                            text.setReview(cell.getCellStyle().getFont(wb).getColor() == Font.COLOR_RED);
                            entry.getTexts().add(text);
                        }
                    }
                }
            }
            row = sheet.getRow(rowNum++);
        }
        return true;
    }

    private String getStringValue(HSSFCell cell) {
        final Object value = getValue(cell);
        return (value == null || value instanceof String) ? (String) value : String.valueOf(value);
    }

    private Object getValue(HSSFCell cell) {
        return getValue(cell, cell.getCellType());
    }

    private Object getValue(HSSFCell cell, int cellType) {
        switch (cellType) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case HSSFCell.CELL_TYPE_FORMULA:
                return getValue(cell, cell.getCachedFormulaResultType());
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case HSSFCell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case HSSFCell.CELL_TYPE_ERROR:
                return cell.getErrorCellValue();
            default:
                return null;
            // do not handle Formular, Error, Blank, ...
        }
    }

}
