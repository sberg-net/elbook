package net.sberg.elbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import net.sberg.elbook.holderattrcmpts.HolderAttrCommand;
import net.sberg.elbook.holderattrcmpts.HolderAttrCommandContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class HolderAttrTest {

    //@Test
    public void execute() throws Exception {
        /*
        {
"vzdAuthId": null,
"vzdAuthSecret": null,
"handleEmptyHolder": true,
"commands": [
  {
    "telematikID" : "10-67.232.10000035",
    "holder": ["egbr","dtrust"]
  }
]
}
         */
        HolderAttrCommandContainer holderAttrCommandContainer = new HolderAttrCommandContainer();
        holderAttrCommandContainer.setHandleEmptyHolder(true);

        File file = new File("");
        CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1))
            .withSkipLines(1)
            .withCSVParser(
                new CSVParserBuilder()
                    .withSeparator(';')
                    .withQuoteChar('"').build()
        ).withKeepCarriageReturn(false).withVerifyReader(true).withMultilineLimit(0).withErrorLocale(Locale.getDefault()).build();

        Iterator<String[]> iter = csvReader.readAll().iterator();
        csvReader.close();

        String[] line;
        String[] holder;
        HolderAttrCommand holderAttrCommand;
        for (Iterator<String[]> iterator = iter; iterator.hasNext(); ) {
            line = iterator.next();
            holderAttrCommand = new HolderAttrCommand();
            holderAttrCommandContainer.getCommands().add(holderAttrCommand);
            holderAttrCommand.setTelematikID(line[0]);
            holderAttrCommand.setHolder(new ArrayList<>());
            holder = line[1].split(",");
            for (int i = 0; i < holder.length; i++) {
                holderAttrCommand.getHolder().add(holder[i].trim());
            }
        }

        File holderFile = new File("");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(holderFile, holderAttrCommandContainer);
    }
}
