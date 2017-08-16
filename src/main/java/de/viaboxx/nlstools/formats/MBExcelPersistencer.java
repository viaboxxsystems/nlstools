package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

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
    private static final int STYLE_MISSING_REVIEW = 6;

    private HSSFWorkbook wb;
    private HSSFCreationHelper helper;
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
        style.setFillPattern(HSSFCellStyle.FINE_DOTS);
        style.setFillBackgroundColor(HSSFColor.BLUE_GREY.index);
        style.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
        style.setFont(font);
        styles.put(STYLE_MISSING_REVIEW, style);

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
        String[] headerCols = {"Key", "Aliases", "Description"};
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

            if (entry.getAliases() != null && !entry.getAliases().isEmpty()) {
                cell = row.createCell(1);
                StringBuilder buf = new StringBuilder();
                boolean comma = false;
                for (String each : entry.getAliases()) {
                    if (comma) buf.append(",");
                    else comma = each != null && each.length() > 0;
                    if (each != null) buf.append(each);
                }
                cell.setCellValue(new HSSFRichTextString(buf.toString()));
            }

            if (entry.getDescription() != null) {
                cell = row.createCell(2);
                cell.setCellValue(new HSSFRichTextString(entry.getDescription()));
            }

            int colNum = firstCol;
            for (String each : locales) {
                MBText text = entry.getText(each);
                if (text != null) {
                    cell = row.createCell(colNum);
                    cell.setCellValue(new HSSFRichTextString(text.getValue()));
                    if (text.getValue() == null || text.getValue().length() == 0) {
                        if (text.isReview()) {
                            comment(cell, each);
                            cell.setCellStyle(styles.get(STYLE_MISSING_REVIEW));
                        } else {
                            comment(cell, each);
                            cell.setCellStyle(styles.get(STYLE_MISSING));
                        }
                    } else if (text.isReview()) {
                        comment(cell, each);
                        cell.setCellStyle(styles.get(STYLE_REVIEW));
                    }
                }
                colNum++;
            }
        }
    }

    private Comment comment(HSSFCell cell, String text) {
        CreationHelper factory = wb.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        // When the comment box is visible, have it show in a 1x3 space
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 1);
        anchor.setRow1(cell.getRow().getRowNum());
        anchor.setRow2(cell.getRow().getRowNum() + 3);
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(text);
        comment.setString(str);
        cell.setCellComment(comment);
        return comment;
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

        String aliasOrDescriptionHeader = getStringValue(row.getCell(1)); // backward compatibility
        boolean aliasColumnAvailable = false;
        if (aliasOrDescriptionHeader != null && "Aliases".equals(aliasOrDescriptionHeader.trim())) {
            firstCol++;
            aliasColumnAvailable = true;
        }
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
                if (aliasColumnAvailable) { // backward compatibility
                    String aliasesCommaSeparated = getStringValue(row.getCell(1));
                    if (aliasesCommaSeparated != null) {
                        StringTokenizer tokens = new StringTokenizer(aliasesCommaSeparated, ", ");
                        List<String> aliases = new ArrayList<String>();
                        while (tokens.hasMoreTokens()) {
                            aliases.add(tokens.nextToken());
                        }
                        entry.setAliases(aliases);
                    }
                }
                if (row.getCell(firstCol - 1) != null) {
                    entry.setDescription(getStringValue(row.getCell(firstCol - 1)));
                }
                colNum = firstCol;
                for (String each : locales) {
                    cell = row.getCell(colNum++);
                    if (cell != null) {
                        final String svalue = getStringValue(cell);
                        if (StringUtils.isNotEmpty(svalue) ||
                            // detect STYLE_MISSING
                            cell.getCellStyle().getFillBackgroundColor() == HSSFColor.BLUE_GREY.index ||
                            cell.getCellStyle().getFillForegroundColor() == HSSFColor.BLUE_GREY.index ||
                            cell.getCellStyle().getFont(wb).getColor() == Font.COLOR_RED) {
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

    private HSSFCreationHelper getCreationHelper() {
        if (helper == null) {
            helper = wb.getCreationHelper();
        }
        return helper;
    }

    private String getStringValue(HSSFCell cell) {
        final Object value = getValue(cell);
        return (value == null || value instanceof String) ? (String) value : String.valueOf(value);
    }

    private Object getValue(HSSFCell cell) {
        return cell == null ? null : getValue(cell, cell.getCellType());
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
