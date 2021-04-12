package main;

// clean package exec:java -Dexec.mainClass=main.Main_Parse
// clean package exec:java -Dexec.mainClass=main.Main_Parse -Dexec.args=C:\WORK\test


/**
 * 1. script name
 * 2. params file name
 */
public class Main_Parse {

    public static void main(String[] args) {
        if (null == args || args.length < 2 || null == args[0] || args[0].isEmpty() || null == args[1] || args[1].isEmpty()) {
            throw new IndexOutOfBoundsException("wrong arguments, set: '[this jar file] [path to settings] [script name]'");
        }
        Parser paramParser = new Parser(args[0]);
        for (int i = 1; i < args.length; i++) {
            if (null == args[i] || args[i].isEmpty())
                break;
            paramParser.createProps(args[i]);
        }
    }
}
