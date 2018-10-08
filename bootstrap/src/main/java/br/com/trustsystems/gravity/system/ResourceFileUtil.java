package br.com.trustsystems.gravity.system;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ResourceFileUtil {


    public static File getFileFromResource(Class classInPackage, String resource) throws UnRetriableException {

        try {

            File file = new File(resource);

            if (file.exists()) {
                return file;
            }

            URL res = classInPackage.getResource("/" + resource);

            if (null == res) {
                throw new UnRetriableException("The file [" + resource + "] was not located within the system.");
            }


            if (res.toString().startsWith("jar:")) {
                InputStream input = classInPackage.getResourceAsStream("/" + resource);

                Files.copy(input, file.toPath());
            } else {
                Files.copy(Paths.get(res.getFile()), file.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            }
            return file;

        } catch (IOException ex) {
            throw new UnRetriableException(ex);
        }


    }
}
