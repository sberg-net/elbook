package net.sberg.elbook;

import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.verzeichnisdienstcmpts.VerzeichnisdienstService;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.TiVZDConnector;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.DelDirEntryCommand;
import net.sberg.elbook.vzdclientcmpts.command.PagingInfo;
import net.sberg.elbook.vzdclientcmpts.command.ReadDirSyncEntryCommand;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.DefaultCommandResultCallbackHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AppConfig.class)
//@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application.yml")
public class DeleteTest {

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

    private void deleteDirEntry4(TiVZDProperties tiVZDProperties, String uid) throws Exception {
        DelDirEntryCommand delDirEntryCommand = new DelDirEntryCommand();
        delDirEntryCommand.setUid(uid);
        DefaultCommandResultCallbackHandler defaultCommandResultCallbackHandler = execute(tiVZDProperties, delDirEntryCommand);
        List<AbstractCommandResultCallbackHandler.ResultReason> reasons = defaultCommandResultCallbackHandler.getResultReasons(delDirEntryCommand.getId());
        AbstractCommandResultCallbackHandler.ResultReason resultReason = reasons.get(0);
        System.out.println(resultReason);
    }

    //@Test
    public void readDirEntryAndDelete() throws Exception {

        Mandant mandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("nutzername", username)));
        mandant.decrypt(ENC_KEYS);
        TiVZDProperties tiVZDProperties = mandant.createAndGetTiVZDProperties(verzeichnisdienstService);

        boolean delete = true;
        ReadDirSyncEntryCommand readDirSyncEntryCommand = new ReadDirSyncEntryCommand();
        readDirSyncEntryCommand.setTelematikIdSubstr("3-11");
        readDirSyncEntryCommand.setBaseEntryOnly(true);

        PagingInfo pagingInfo = new PagingInfo();
        pagingInfo.setOneTimePaging(false);
        pagingInfo.setPagingSize(200);
        pagingInfo.setPagingCookie("");
        readDirSyncEntryCommand.setPagingInfo(pagingInfo);

        DefaultCommandResultCallbackHandler defaultCommandResultCallbackHandler = execute(tiVZDProperties, readDirSyncEntryCommand);
        List entries = defaultCommandResultCallbackHandler.getDirectoryEntries(readDirSyncEntryCommand.getId());
        if (entries != null) {
            for (Iterator<VzdEntryWrapper> iterator = entries.iterator(); iterator.hasNext(); ) {
                VzdEntryWrapper entry = iterator.next();
                if (delete) {
                    deleteDirEntry4(tiVZDProperties, entry.extractDirectoryEntryUid());
                }
            }
        }
    }
}
