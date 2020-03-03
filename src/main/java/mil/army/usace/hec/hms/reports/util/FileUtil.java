package mil.army.usace.hec.hms.reports.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

public class FileUtil {
    private FileUtil() {}

    public static String getFilePath(String directoryToSearch, String fileName) {
        final String[] x = new String[1];

        try {
            Files.walk(Paths.get(directoryToSearch))
                    .filter(Files::isRegularFile)
                    .forEach((f)->{
                        String file = f.toString();
                        if(file.endsWith(fileName)) {
                            x[0] = file;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return x[0];
    } // getFilePath()
}
