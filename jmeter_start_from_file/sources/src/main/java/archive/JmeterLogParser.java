package archive;

import helpers.NTFileHelper;
import helpers.NTLogHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmeterLogParser {
    private NTLogHelper logger = new NTLogHelper(JmeterLogParser.class);

    private static final String REGEXP_PATTERN = "^(\\d{12,15}),";
    private static final String REGEXP_FILE_EXTENSION = "^(.*\\.jtl)$";
    private static final String REGEXP_LINE_CHECK = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final int LINE_SIZE_CHECK = 17;
    private static final int COLUMN_RESPONSE_CODE_NUMBER = 4;
    private static final int LEAST_NUMBER_OF_STRINGS_IN_LOG_FILE = 4;
    private static final int CUSTOM_ANSWER = 599;

    private String path;

    JmeterLogParser(String path) {
        this.path = path;
    }

    /**
     * looking for files with exact extension in path
     * when jtl found:
     * read file - do parse - write parsed info into file
     * count all found files
     * count all parsed files
     * @return true parse is OK
     */
    public boolean doParse() {
        int filesParsed = 0;
        logger.logInfo("working in path: " + path);
        logger.logInfo("in progress..");
        List<String> foundFiles = find_valid_files(path, REGEXP_FILE_EXTENSION);
        logger.logInfo("number of found files: " + foundFiles.size());
        if (foundFiles.isEmpty()) {
            return false;
        }
        for (String filePath: foundFiles) {
            List<String> fileInfo;
            try {
                fileInfo = NTFileHelper.getFileInfo(filePath);
            } catch (Exception e) {
                logger.logError("error while trying to read file: + " + filePath, e);
                continue;
            }
            if (fileInfo.isEmpty()) {
                logger.logError("found file is empty: " + filePath);
                continue;
            }
            logger.logInfo("\n||parsing file: " + filePath);
            int changedStringsNumber;
            if ((changedStringsNumber = changeFile(fileInfo)) == 0) {
                logger.logInfo("--nothing changed, continue..");
                continue;
            }
            logger.logInfo("--file parsed, number of changed strings: " + changedStringsNumber);
            filesParsed++;
            try {
                NTFileHelper.writeListToFile(fileInfo, filePath, false);
            } catch (Exception e) {
                logger.logError("error while trying to rewrite file: " + filePath, e);
            }
        }
        logger.logInfo("changed files: " + filesParsed);
        logger.logInfo("parsing succeeded");
        return true;
    }

    /**
     * get list of valid files
     * @param catalogPath - path to catalog where to look for
     * @param fileMatcher - regexp for file to look for
     * @return list of files paths
     */
    public static List<String> find_valid_files(String catalogPath, String fileMatcher) {
        List<String> foundFiles = new ArrayList<>();
        List<File> filesInWork = NTFileHelper.listFilesFromFolder(catalogPath);
        if (filesInWork.isEmpty()) {
            return foundFiles;
        }
        for (File file : filesInWork) {
            String checkName = file.getName();
            if (!checkName.matches(fileMatcher)) {
                continue;
            }
            foundFiles.add(file.getAbsolutePath());
        }
        return foundFiles;
    }

    /**
     * parsing single file
     * check every string, remove spaces, combine split strings:
     * check for number of columns (remove invalid)
     * check the forth column, it can't be string, should be numeric only
     * @param resultList - all parsed lines from one file
     * @return number of changed strings
     */
    private int changeFile (List<String> resultList) {
        if (null == resultList || resultList.isEmpty()) {
            return 0;
        }
        int numberOfStringsInput = resultList.size();
        List<String> fileData;
        fileData = splitLines(resultList);
        if (null == fileData || fileData.isEmpty()) {
            logger.logError("--something wrong after lines were split");
            return 0;
        }
        fileData = checkInvalidLine(resultList);
        if (null == fileData || fileData.isEmpty()) {
            logger.logError("--something wrong after file was checked");
            return 0;
        }
        return numberOfStringsInput - resultList.size();
    }

    /**
     * split the lines
     * file can have some invalid lines (the were separated by \n)
     * @param fileDataIn - list with all file data
     * @return - reworked data list
     */
    private List<String> splitLines(List<String> fileDataIn) {
        if (null == fileDataIn || fileDataIn.isEmpty() ||
                fileDataIn.size() <= LEAST_NUMBER_OF_STRINGS_IN_LOG_FILE) {
            return fileDataIn;
        }
        List<String> fileData = new ArrayList<>(fileDataIn);
        fileDataIn.clear();
        fileDataIn.add(fileData.get(0));                                    // file headers
        StringBuffer previousLineBuf = new StringBuffer(fileData.get(1));
        for (int i = 2; i < fileData.size(); i++) {
            String currentLine = fileData.get(i);
            Matcher curLineMatcher = Pattern.compile(REGEXP_PATTERN).matcher(currentLine);
            if (!curLineMatcher.find()) {
                previousLineBuf.append(currentLine);
                continue;
            }
            fileDataIn.add(previousLineBuf.toString());
            previousLineBuf.delete(0, previousLineBuf.capacity());
            previousLineBuf.append(currentLine);
        }
        fileDataIn.add(previousLineBuf.toString());
        return fileDataIn;
    }

    /**
     * check input data
     * there are should be constant number of columns (separated by ',')
     * the 4th column should be numeric, must change if not
     * @param fileDataIn - list with all file data
     * @return - reworked data list
     */
    private List<String> checkInvalidLine(List<String> fileDataIn) {
        if (null == fileDataIn || fileDataIn.isEmpty() ||
                fileDataIn.size() <= LEAST_NUMBER_OF_STRINGS_IN_LOG_FILE) {
            return fileDataIn;
        }
        List<String> fileData = new ArrayList<>(fileDataIn);
        fileDataIn.clear();
        fileDataIn.add(fileData.get(0));                                    // file headers
        for (int i = 1; i < fileData.size(); i++) {
            String currentLine = fileData.get(i);
            if (!isValidLine(currentLine)) {
                continue;
            }
            StringBuffer currentLineBuf = new StringBuffer(currentLine);
            correctFileLine(currentLineBuf);
            fileDataIn.add(currentLineBuf.toString());
        }
        return fileDataIn;
    }

    /**
     * check log file line, counting number of separated columns
     * @param fileLine - file line to check
     * @return - true if line is valid
     */
    private boolean isValidLine(String fileLine) {
        String[] lineElem = fileLine.split(REGEXP_LINE_CHECK, -1);      // split to commas
        return lineElem.length == LINE_SIZE_CHECK;
    }

    /**
     * check forth column (response code value) of file line
     * it should be numeric only, no words or special characters
     * if it isn't - replace field value with number
     * @param lineBuffer - line to check
     * @return - changed line
     */
    private boolean correctFileLine(StringBuffer lineBuffer) {
        String lineText = lineBuffer.toString();
        String[] lineElem = lineText.split(REGEXP_LINE_CHECK, -1);
        int columnResponseIndex = COLUMN_RESPONSE_CODE_NUMBER - 1;
        String fieldToCheck = lineElem[columnResponseIndex].trim();
        if (fieldToCheck.matches("^\\d+$")) {
            return false;
        }
        lineBuffer.delete(0, lineBuffer.length());
        lineBuffer.append(lineElem[0]);
//        StringBuffer resultStrBuff = new StringBuffer(lineElem[0]);
        for (int i = 1; i < lineElem.length; i++) {
            lineBuffer.append(",");
            if (i == columnResponseIndex) {
                lineBuffer.append(CUSTOM_ANSWER);
                continue;
            }
            lineBuffer.append(lineElem[i]);
        }
        return true;
    }
}
