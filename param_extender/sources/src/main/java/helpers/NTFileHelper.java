package helpers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NTFileHelper {

    /*
     * folder methods
     * ==============================================================================================================
     */

    public static boolean createFile(String filePath, String fileName) {
        try {
            File folder = new File("./test");
            if (folder.mkdirs())
            {
                File file = new File("./test/1.txt");
                FileOutputStream stream = new FileOutputStream(file);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String delNoDigOrLet (String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character .isLetterOrDigit(s.charAt(i)))
                sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    /**
     * get all files from folder
     * @param path - folder path
     * @return list with all folder files
     */
    public static List<File> listFilesFromFolder(String path) {
        List<File> files = new ArrayList<>();
        File[] filesArray = new File(path).listFiles();
        if(filesArray == null || filesArray.length < 1) {
            return files;
        }
        for (final File fileEntry : filesArray) {
            if (!fileEntry.isDirectory()) {
                files.add(fileEntry);
            }
        }
        return files;
    }

    /**
     * get all catalogs from folder
     * @param path - parent folder path
     * @return list with all folder files
     */
    public static List<File> listDirectoriesFromFolder(String path) {
        List<File> files = new ArrayList<>();
        File[] filesArray = new File(path).listFiles();
        if(filesArray == null || filesArray.length < 1) {
            return files;
        }
        for (final File fileEntry : filesArray) {
            if (fileEntry.isDirectory()) {
                files.add(fileEntry);
            }
        }
        return files;
    }

    /**
     * get list of valid files
     * @param catalogPath - path to catalog where to look for
     * @param fileMatcher - regexp for file to look for
     * @return list of files paths
     */
    public static List<File> findValidFiles(String catalogPath, String fileMatcher) {
        List<File> foundFiles = new ArrayList<>();
        List<File> filesInWork = NTFileHelper.listFilesFromFolder(catalogPath);
        if (filesInWork.isEmpty()) {
            return foundFiles;
        }
        for (File file : filesInWork) {
            String checkName = file.getName();
            if (!checkName.matches(fileMatcher)) {
                continue;
            }
            foundFiles.add(file);
        }
        return foundFiles;
    }

    /**
     * search in folders and subfolders for file with exact name
     * @param parentCatalog - path to folder, where search starts
     * @param fileMatcher - regexp to look for file name
     * @return - path to first found file
     */
    public static String searchForFile(String parentCatalog, String fileMatcher) {
        if (parentCatalog == null || parentCatalog.isEmpty() || fileMatcher == null || fileMatcher.isEmpty()) {
            return "";
        }
        List<File> foundFiles = findValidFiles(parentCatalog, fileMatcher);
        if (!foundFiles.isEmpty()) {
            return foundFiles.get(0).getAbsolutePath();
        }
        List<File> foundFolders = listDirectoriesFromFolder(parentCatalog);
        if (foundFolders.isEmpty()) {
            return "";
        }
        for (File folder : foundFolders) {
            String thisFolderPath = folder.getAbsolutePath();
            String foundPath = searchForFile(thisFolderPath, fileMatcher);
            if (!foundPath.isEmpty()) {
                return foundPath;
            }
        }
        return "";
    }

    /*
     * read from files methods
     * ==============================================================================================================
     */

    /**
     * return file as list of string
     * @param pathToFile - file path
     * @return list, where string = file line
     */
    public static List<String> getFileInfo(String pathToFile) throws Exception {
        List<String> results = new ArrayList<>();
        try(FileInputStream myFile = new FileInputStream(pathToFile);
            InputStreamReader inputStreamReader = new InputStreamReader(myFile, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
        } catch (IOException e) {
            throw e;
        }
        return results;
    }

    /**
     * get info from file, check every line on regular expression
     * adds String into result list only if the WHOLE line matches regExp
     * @param pathToFile - path to file to read from
     * @param checkRegExp - expression, using in searchStore
     * @return list of Strings - lines, which equals regular expression
     */
    public static List<String> getFileInfoRegExp(String pathToFile, String checkRegExp) throws Exception {
        List<String> results = new ArrayList<>();
        try(FileInputStream myFile = new FileInputStream(pathToFile);
            InputStreamReader inputStreamReader = new InputStreamReader(myFile, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches(checkRegExp)) { results.add(line); }
            }
        } catch (IOException e) {
            throw e;
        }
        return results;
    }

    /**
     * read chosen file, for small files only
     * @param pathToFile - path to file that should be read
     * @return string with all file info
     */
    public static String getFileAsString(String pathToFile) throws Exception {
        StringBuffer buffer = new StringBuffer();
        try(FileInputStream myFile = new FileInputStream(pathToFile);
            InputStreamReader inputStreamReader = new InputStreamReader(myFile, StandardCharsets.UTF_8);
            Reader reader = new BufferedReader(inputStreamReader)) {
            int symbol;
            while ((symbol = reader.read()) > -1) {
                buffer.append((char)symbol);
            }
        } catch (IOException e) {
            throw e;
        }
        return buffer.toString();
    }


    /*
     * write to files methods
     * ==============================================================================================================
     */

    /**
     * write or append list of strings into file, one string = one line
     * appending starts from new string
     * @param output - list of strings to write
     * @param filePath - path to file to write into
     * @param append - choose if info should be appended without changing previous if exists
     */
    public static void writeListToFile(List<String> output, String filePath, boolean append) throws Exception {
        try(FileOutputStream myFile = new FileOutputStream(String.valueOf(Paths.get(filePath)), append);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(myFile, StandardCharsets.UTF_8);
            Writer out = new BufferedWriter(outputStreamWriter)) {
            if (append) {
                out.append("\r\n");
            }
            for (String string : output) {
                out.append(string);
                out.append("\r\n");
            }
        } catch (IOException | NullPointerException e) {
            throw e;
        }
    }

    /**
     * write or append new string to file
     * appending starts from new string
     * @param outString - string to write
     * @param filePath - path to file to write into
     * @param append - choose if string should be appended without changing previous if exists
     */
    public static void writeStringToFile(String outString, String filePath, boolean append) throws Exception {
        try(FileOutputStream myFile = new FileOutputStream(String.valueOf(Paths.get(filePath)), append);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(myFile, StandardCharsets.UTF_8);
            Writer out = new BufferedWriter(outputStreamWriter)) {
            if (append) {
                out.write("\r\n");
            }
            out.write(outString);
        } catch (IOException | NullPointerException e) {
            throw e;
        }
    }
}