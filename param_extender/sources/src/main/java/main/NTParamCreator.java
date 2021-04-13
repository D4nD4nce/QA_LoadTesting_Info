package main;

import helpers.NTFileHelper;
import helpers.NTLogHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NTParamCreator {
    private NTLogHelper logger = new NTLogHelper(NTParamCreator.class);

    private final static String CSV_EXTENSION = "csv";

    private Path catalogPath;
    private String regexpMatcher;
    private int newFilesCount;
    private String newFilesSample;

    /**
     * @param catalogPath - path to folder
     * @param regexpMatcher - regexp matcher for files in folder
     * @param newFilesCount - number of files to create
     * @param newFilesSample - sample name for new files
     */
    public NTParamCreator(String catalogPath, String regexpMatcher, String newFilesCount, String newFilesSample) {
        if (!isParamOK(catalogPath) || !isParamOK(regexpMatcher) || !isParamOK(newFilesCount) || !isParamOK(newFilesSample)) {
            logger.logError("some params are invalid");
        }
        this.regexpMatcher = regexpMatcher;
        this.newFilesCount = Integer.parseInt(newFilesCount);
        this.newFilesSample = newFilesSample;
        Path relativePath = Paths.get(catalogPath);
        if (Files.exists(relativePath) && Files.isDirectory(relativePath)) {
            this.catalogPath = relativePath;
            return;
        }
        String userDir = System.getProperty("user.dir");
        Path absolutePath = Paths.get(userDir, catalogPath);
        if (Files.exists(absolutePath) && Files.isDirectory(absolutePath)) {
            this.catalogPath = absolutePath;
            return;
        }
        logger.logError("can't find folder with param files from parameter: " + catalogPath);
    }

    /**
     * main method to create new parameter files
     */
    public void createParams() {
        logger.logInfo("-- working in path: " + catalogPath.toString());
        List<String> foundInfo = readFiles(catalogPath);
        // count pack size
        int standardPackSize = foundInfo.size() / newFilesCount;
//        int lastPackSize = standardPackSize + foundInfo.size() % newFilesCount;
        for (int i = 0; i < newFilesCount - 1; i++) {
            List<String> onePackParams = getOneFileParams(i * standardPackSize, i * standardPackSize + standardPackSize, foundInfo);
            createFile(getNewFileAbsolutePath(i + 1), onePackParams);
        }
        List<String> lastPackParams = getOneFileParams(standardPackSize * (newFilesCount - 1), foundInfo.size(), foundInfo);
        createFile(getNewFileAbsolutePath(newFilesCount), lastPackParams);
        logger.logInfo("-- files created: " + newFilesCount);
    }

    /**
     * get absolute path and name, using current directory value and new file sample
     * @param index - part of the new file name
     * @return - full path
     */
    private String getNewFileAbsolutePath(int index) {
        return Paths.get(catalogPath.toString(), newFilesSample + "_" + index + "." + CSV_EXTENSION).toString();
    }

    /**
     * create new file
     * @param fileAbsoluteName - absolute new file name
     * @param fileInfo - new file data
     * @return - true if file was created
     */
    private boolean createFile(String fileAbsoluteName, List<String> fileInfo) {
        try {
            NTFileHelper.writeListToFile(fileInfo, fileAbsoluteName, false);
        } catch (Exception e) {
            logger.logError("can't create file: " + fileAbsoluteName, e);
            return false;
        }
        return true;
    }

    /**
     * check value: if not null and not empty
     * @param param - value to check
     * @return - true if param valid
     */
    private boolean isParamOK(String param) {
        return (param != null && !param.isEmpty());
    }

    /**
     * read all found files and get all lines as list
     * @param catalogPath - path were to look for files using regexpMatcher
     * @return - list with all found lines from all files
     */
    private List<String> readFiles(Path catalogPath) {
        List<String> allFoundFilesInfo = new ArrayList<>();
        List<File> foundFiles = NTFileHelper.findValidFiles(catalogPath.toString(), regexpMatcher);
        if (foundFiles.isEmpty()) {
            logger.logError("no files found in path: " + catalogPath.toString());
            return allFoundFilesInfo;
        }
        for (File file : foundFiles) {
            try {
                allFoundFilesInfo.addAll(NTFileHelper.getFileInfo(file.getAbsolutePath()));
            } catch (Exception ex) {
                logger.logError("can't read file: " + file.getAbsolutePath(), ex);
            }
        }
        logger.logInfo("-- found files: " + foundFiles.size() + " || found lines: " + allFoundFilesInfo.size());
        return allFoundFilesInfo;
    }

    /**
     * get part of the list in boundaries
     * @param first_index - left boundary, included
     * @param last_index - right boundary, excluded
     * @param paramList - general list, all values will be gotten from it using boundaries
     * @return - pack with lines found in general list between boundaries
     */
    private List<String> getOneFileParams(int first_index, int last_index, List<String> paramList) {
        List<String> oneFileParams = new ArrayList<>();
        for (int i = first_index; i < last_index; i++) {
            oneFileParams.add(paramList.get(i));
        }
        return oneFileParams;
    }
}
