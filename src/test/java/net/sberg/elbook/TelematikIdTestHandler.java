package net.sberg.elbook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelematikIdTestHandler {

    //@Test
    public void execute() throws Exception {
        Pattern pattern = Pattern.compile("^3-...2.*");
        Matcher matcher = pattern.matcher("3-11.2.0000022151.16.997");
        if (matcher.find() && matcher.group().equals("3-11.2.0000022151.16.997")) {
            System.out.println("juchu");
        }
    }
}
