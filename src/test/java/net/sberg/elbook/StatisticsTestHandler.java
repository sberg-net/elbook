package net.sberg.elbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.VerzeichnisdienstService;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.VzdEntryWrapper;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDConnector;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDProperties;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.AbstractCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.PagingInfo;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.ReadDirCertCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.ReadDirSyncEntryCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.DefaultCommandResultCallbackHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AppConfig.class)
//@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application.yml")
public class StatisticsTestHandler {

    //@Autowired
    private TiVZDConnector tiVZDConnector;
    //@Autowired
    private JdbcGenericDao genericDao;
    //@Autowired
    private VerzeichnisdienstService verzeichnisdienstService;
    //@Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    private String username = "";

    private DefaultCommandResultCallbackHandler execute(TiVZDProperties tiVZDProperties, AbstractCommand command) throws Exception {
        List<AbstractCommand> commands = new ArrayList<>();
        commands.add(command);
        return tiVZDConnector.executeCommands(commands, tiVZDProperties);
    }

    //@Test
    public void execute() throws Exception {

        Mandant mandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("nutzername", username)));
        mandant.decrypt(ENC_KEYS);
        TiVZDProperties tiVZDProperties = mandant.createAndGetTiVZDProperties(verzeichnisdienstService);

        PagingInfo pagingInfo = new PagingInfo();
        pagingInfo.setPagingSize(200);

        ReadDirSyncEntryCommand readDirSyncEntryCommand = new ReadDirSyncEntryCommand();
        readDirSyncEntryCommand.setPagingInfo(pagingInfo);
        readDirSyncEntryCommand.setTelematikIdSubstr("3-09.3");
        readDirSyncEntryCommand.setBaseEntryOnly(true);

        ReadDirCertCommand readDirCertCommand = new ReadDirCertCommand();

        DefaultCommandResultCallbackHandler defaultCommandResultCallbackHandler = execute(tiVZDProperties, readDirSyncEntryCommand);
        List entries = defaultCommandResultCallbackHandler.getDirectoryEntries(readDirSyncEntryCommand.getId());

        List<String> emptyHolderAndNotEmptyCerts = new ArrayList<>();
        List<String> oneSizeHolderAndNotEmptyCerts = new ArrayList<>();

        if (entries != null) {
            for (Iterator<VzdEntryWrapper> iterator = entries.iterator(); iterator.hasNext(); ) {
                VzdEntryWrapper entry = iterator.next();
                String telematikId = entry.extractDirectoryEntryTelematikId();
                List<String> holder = entry.extractDirectoryEntryHolder();
                if (holder.size() == 0 || holder.size() == 1) {
                    readDirCertCommand.setTelematikId(telematikId);
                    defaultCommandResultCallbackHandler = execute(tiVZDProperties, readDirCertCommand);
                    List certs = defaultCommandResultCallbackHandler.getUserCertificates(readDirCertCommand.getId());
                    if (!certs.isEmpty() && holder.size() == 0) {
                        emptyHolderAndNotEmptyCerts.add(telematikId);
                    }
                    else if (!certs.isEmpty() && holder.size() == 1) {
                        oneSizeHolderAndNotEmptyCerts.add(telematikId);
                    }
                }
            }
        }

        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(emptyHolderAndNotEmptyCerts));
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(oneSizeHolderAndNotEmptyCerts));
    }
}
