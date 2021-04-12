package main;

import helpers.NTLogHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * script_name,
 * ksed_targetConcurrency,
 * ksed_rampUpTime,
 * ksed_rampUpStepsCount,
 * ksed_holdTargetRateTime,
 * ksed_iterationCount,
 * ksed_pacingTargetThroughput,
 * ksed_testID,
 * ksed_docxParams,
 * ksed_docParams,
 * ksed_xlsxParams,
 * ksed_xlsParams,
 * ksed_pdfParams
 */

public class PropsCreator {
    private static NTLogHelper logger = new NTLogHelper(PropsCreator.class);

    private List<String> namesList;
    private List<String> valuesList;
    private final Map<String,String> params = new HashMap<>();

    public PropsCreator(List<String> paramNames, List<String> paramValues) {
        this.namesList = paramNames;
        this.valuesList = paramValues;
    }

    public boolean create() {
        if(!isParamOK(namesList) || !isParamOK(valuesList)) {
            callError("got invalid param lines");
            return false;
        }
//        logger.logInfo("-- names and values checked -> OK");
        for(int i = 0; i < namesList.size(); i++) {
            String paramName = namesList.get(i);
            String paramValue = valuesList.get(i);
            if (!isParamOK(paramName) || !isParamOK(paramValue)) {
                callError("current params are invalid, name: " + paramName + ", value: " + paramValue);
                return false;
            }
            logger.logInfo("-- saving props: " + paramName + " - " + paramValue);
            params.put(fixParam(paramName), fixParam(paramValue));
        }
        logger.logInfo("-- params saved -> OK");
        return true;
    }

    public String getParamsAsProps() {
        StringBuffer stringBuffer = new StringBuffer();
        params.forEach((name, value) -> stringBuffer.append(name).append("=").append(value).append("\n"));
        return stringBuffer.toString();
    }

    private void callError(String msg) {
        logger.logError(msg + " || invalid params, line: " + valuesList.toString() + " || headers: " + namesList.toString());
    }

    private boolean isParamOK(List<String> param) {
        return (null != param && !param.isEmpty());
    }

    private boolean isParamOK(String param) {
        return (null != param && !param.isEmpty());
    }

    private String fixParam(String param) {
        return param.trim().replace("\"","");
    }
}
