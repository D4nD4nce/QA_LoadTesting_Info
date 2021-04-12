package main;

import helpers.NTFileHelper;
import helpers.NTLogHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Parser {
    private static NTLogHelper logger = new NTLogHelper(Parser.class);

    private static final String RESULT_EXTENSION = ".properties";

    private ParamFileReader fileReader;

    public Parser(String file) {
        parseFile(file);
    }

    public void createProps(String scriptName) {
        if (!isParamOK(scriptName)) {
            logger.logError("wrong script name param");
            return;
        }
        // get all params
        List<String> paramNames = fileReader.getParamNames();
        List<String> paramValues = fileReader.getParamValues(scriptName);
        if (null == paramValues || paramValues.isEmpty()) {
            logger.logError("can't find settings for script: " + scriptName);
            return;
        }
        logger.logInfo("-- found params for script: " + scriptName);
        //---------------------
        // store parsed info
        PropsCreator creator = new PropsCreator(paramNames, paramValues);
        if (!creator.create()) {
            logger.logError("properties were not created");
            return;
        }
        //---------------------
        // create new file
        createNewFile(scriptName, creator);
        logger.logInfo("-- done!");
    }

    private void parseFile(String paramsFileName) {
        //---------------------
        // check all
        if (!isParamOK(paramsFileName)) {
            logger.logError("wrong file name arg: " + paramsFileName);
            return;
        }
        logger.logInfo("-- checked args -> OK");
        Path filePath;
        if(!isAbsolutePath(paramsFileName)) {
            filePath = Paths.get(System.getProperty("user.dir"), paramsFileName);
        } else {
            filePath = Paths.get(paramsFileName);
        }
        if(!Files.exists(filePath)) {
            logger.logError("can't find file with params: " + filePath.toString());
            return;
        }
        logger.logInfo("-- found file: " + filePath.toString());
        //---------------------
        // read file
        fileReader = new ParamFileReader();
        fileReader.readFile(filePath);
    }

    private void createNewFile(String scriptName, PropsCreator creator) {
        String newFileName = scriptName + RESULT_EXTENSION;
        Path newFilePath = Paths.get(System.getProperty("user.dir"), newFileName);
        logger.logInfo("-- new props file path: " + newFilePath.toString());
        try {
            NTFileHelper.writeStringToFile(creator.getParamsAsProps(), newFilePath.toString(), false);
        } catch (Exception ex) {
            logger.logError("can't create file: " + newFilePath.toString(), ex);
        }
    }

    private boolean isAbsolutePath(String fileName) {
        return fileName.contains(File.separator);
    }

    private boolean isParamOK(String param) {
        return (null != param && !param.isEmpty());
    }

}
