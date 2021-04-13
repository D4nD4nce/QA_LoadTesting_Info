package main;

// clean package exec:java -Dexec.mainClass=main.Main_Parse
// clean package exec:java -Dexec.mainClass=main.Main_Parse -Dexec.args=C:\WORK\test


/**
 * 1. path to folder
 * 2. regexp matcher for files in folder
 * 3. number of files to create
 * 4. sample name for new files
 */
public class Main {

    public static void main(String[] args) {
        if (null == args || args.length < 4 || null == args[0] || args[0].isEmpty() || null == args[1] || args[1].isEmpty()) {
            throw new IndexOutOfBoundsException("wrong arguments, set: " +
                    "'[path to folder] [regexp matcher] [new files count] [new files sample name]'");
        }
        NTParamCreator paramCreator = new NTParamCreator(args[0], args[1], args[2], args[3]);
        paramCreator.createParams();
    }
}
