package helpers;

public class NTNumbersHelper {

    /**
     * get value, check if it has one number or two, if one - add zero before this number
     * @param value - number to check and format
     * @return - formatted value as String
     */
    public static String getNiceNumber(long value) {
        String strValue = String.valueOf(value);
        int length = strValue.length();
        return (length < 2) ? "0" + strValue : strValue;
    }
}
