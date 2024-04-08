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

import java.util.Base64;
import java.util.List;

public class StringUtils {

    public static final boolean isBase64(String content) throws Exception {
        return org.apache.commons.codec.binary.Base64.isBase64(content.getBytes());
    }

    public static final String decrypt(String[] keys, String content) throws Exception {
        String hContent = new String(Base64.getDecoder().decode(content.getBytes()));
        return StringUtils.xor(hContent, keys);
    }

    public static final String encrypt(String[] keys, String content) throws Exception {
        String hContent = StringUtils.xor(content, keys);
        return new String(Base64.getEncoder().encode(hContent.getBytes()));
    }

    public static final String xor(String s, String[] keys) throws Exception {
        for (int i = 0; i < keys.length; i++) {
            s = xor(s, keys[i]);
        }
        return s;
    }

    public static final String xor(String s, String key) throws Exception {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < s.length(); i++) {
            sb.append((char)(s.charAt(i) ^ key.charAt(i % key.length())));
        }
        return sb.toString();
    }

    public static String listToString(List<String> list, String delim) {
        StringBuilder sb = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            for (String s : list) {
                sb.append(s + delim);
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
        }
        return sb.toString();
    }

    public static String listToString(List<String> list) {
        return listToString(list, ",");
    }
}
