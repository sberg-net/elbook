/*
 *  Copyright (C) 2023 sberg it-systeme GmbH
 *
 *  Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the
 *  European Commission - subsequent versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package net.sberg.elbook.common;

import java.io.*;

public class FileUtils {

    public static final String readFileContent(String fileName) throws Exception {
        return readFileContent(new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8")));
    }

    private static final String readFileContent(BufferedReader reader) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(ls);
            }
            stringBuilder.append(line);
        }
        reader.close();
        return stringBuilder.toString();
    }

    public static final File writeToFile(String str, String fileName) throws Exception {
        File file = new File(fileName);
        checkExistsFileDir(fileName);

        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write(str);
        output.flush();
        output.close();
        return file;
    }

    public static final void checkExistsFileDir(String fileName) throws Exception {
        File hFile = new File(fileName).getParentFile();
        while (hFile != null && hFile.exists()) {
            hFile = hFile.getParentFile();
        }
        if (hFile != null && !hFile.exists()) {
            hFile.mkdirs();
        }
    }

}
