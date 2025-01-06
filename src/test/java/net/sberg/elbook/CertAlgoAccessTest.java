package net.sberg.elbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.verzeichnisdienstcmpts.VerzeichnisdienstService;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.TiVZDConnector;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.PagingInfo;
import net.sberg.elbook.vzdclientcmpts.command.ReadDirCertCommand;
import net.sberg.elbook.vzdclientcmpts.command.ReadDirSyncEntryCommand;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.DefaultCommandResultCallbackHandler;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AppConfig.class)
//@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application.yml")
public class CertAlgoAccessTest {

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

        ReadDirSyncEntryCommand readDirSyncEntryCommand = new ReadDirSyncEntryCommand();
        readDirSyncEntryCommand.setTelematikIdSubstr("3-15.3");
        readDirSyncEntryCommand.setBaseEntryOnly(Boolean.TRUE);

        PagingInfo pagingInfo = new PagingInfo();
        pagingInfo.setOneTimePaging(false);
        pagingInfo.setPagingSize(200);
        pagingInfo.setPagingCookie("");
        readDirSyncEntryCommand.setPagingInfo(pagingInfo);

        DefaultCommandResultCallbackHandler defaultCommandResultCallbackHandler = execute(tiVZDProperties, readDirSyncEntryCommand);
        List<VzdEntryWrapper> directoryEntries = defaultCommandResultCallbackHandler.getDirectoryEntries(readDirSyncEntryCommand.getId());

        List<String> telematikIds = new ArrayList<>();
        for (Iterator<VzdEntryWrapper> iterator = directoryEntries.iterator(); iterator.hasNext(); ) {
            VzdEntryWrapper entry = iterator.next();
            telematikIds.add(entry.extractDirectoryEntryTelematikId());
        }

        ReadDirCertCommand readDirCertCommand = new ReadDirCertCommand();
        readDirCertCommand.setPublicKeyAlgorithm("RSA");

        LocalDate toThreshold = LocalDate.of(2025, 12, 31);

        Map<String, List<String>> res = new HashMap();

        for (Iterator<String> iterator = telematikIds.iterator(); iterator.hasNext(); ) {
            String telematikId = iterator.next();
            readDirCertCommand.setTelematikId(telematikId);
            defaultCommandResultCallbackHandler = execute(tiVZDProperties, readDirCertCommand);
            if (defaultCommandResultCallbackHandler.getResultReasons(readDirCertCommand.getId()).isEmpty()) {
                List<VzdEntryWrapper> certs = defaultCommandResultCallbackHandler.getUserCertificates(readDirCertCommand.getId());
                for (Iterator<VzdEntryWrapper> iteratored = certs.iterator(); iteratored.hasNext(); ) {
                    VzdEntryWrapper vzdEntryWrapper = iteratored.next();
                    Instant localDateTime = Instant.parse(vzdEntryWrapper.extractUserCertificateNotAfter());
                    LocalDateTime to = LocalDateTime.ofInstant(localDateTime, ZoneId.of("UTC"));

                    if (to.toLocalDate().isBefore(toThreshold)) {
                        continue;
                    }

                    localDateTime = Instant.parse(vzdEntryWrapper.extractUserCertificateNotBefore());
                    LocalDateTime from = LocalDateTime.ofInstant(localDateTime, ZoneId.of("UTC"));
                    if (from.until(to, ChronoUnit.DAYS) / 365 >= 5) {
                        if (!res.containsKey(telematikId)) {
                            res.put(telematikId, new ArrayList<>());
                        }
                        res.get(telematikId).add(vzdEntryWrapper.extractUserCertificateDnCn());
                    }
                }
            }
        }

        readDirCertCommand.setPublicKeyAlgorithm("EC");
        for (Iterator<String> iterator = telematikIds.iterator(); iterator.hasNext(); ) {
            String telematikId = iterator.next();
            readDirCertCommand.setTelematikId(telematikId);
            defaultCommandResultCallbackHandler = execute(tiVZDProperties, readDirCertCommand);
            if (defaultCommandResultCallbackHandler.getResultReasons(readDirCertCommand.getId()).isEmpty()) {
                List<VzdEntryWrapper> certs = defaultCommandResultCallbackHandler.getUserCertificates(readDirCertCommand.getId());
                if (!certs.isEmpty()) {
                    res.remove(telematikId);
                }
            }
        }

        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File("/home/basketmc/Downloads/res_3_15_3_RSA.json"), res);
    }
}
