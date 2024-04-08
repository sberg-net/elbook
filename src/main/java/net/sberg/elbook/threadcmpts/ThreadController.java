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
package net.sberg.elbook.threadcmpts;

import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.stammdatenzertimportcmpts.VerzeichnisdienstImportErgebnis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Controller
public class ThreadController extends AbstractWebController {

    @RequestMapping(value = "/thread", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String threadView() {
        return "thread/thread";
    }

    @RequestMapping(value = "/thread/uebersicht", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Model model) {
        model.addAttribute("anzahl", 0);
        model.addAttribute("anzahlThreads", 0);
        model.addAttribute("result", new ArrayList<>());
        return "thread/threadUebersicht";
    }

    @RequestMapping(value = "/thread/starten", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String starten(Model model, int anzahl, int anzahlThreads) throws Exception {

        ExecutorService executorService = null;
        if (anzahlThreads > 0) {
            executorService = Executors.newFixedThreadPool(anzahlThreads);
        }

        List<ThreadObj> result = new ArrayList<>();
        List futureList = Collections.synchronizedList(new ArrayList());

        for (int i = 1; i <= anzahl; i++) {
            futureList.add(starten(i, executorService));
        }

        CompletableFuture<VerzeichnisdienstImportErgebnis>[] arr = new CompletableFuture[futureList.size()];
        CompletableFuture.allOf((CompletableFuture<ThreadObj>[])futureList.toArray(arr)).join();

        result = (List<ThreadObj>)futureList.stream().map(f -> {
            try {
                return ((CompletableFuture)f).get();
            }
            catch (Exception e) {}
            return null;
        }).collect(Collectors.toList());

        model.addAttribute("anzahl", anzahl);
        model.addAttribute("anzahlThreads", anzahlThreads);
        model.addAttribute("result", result);

        return "thread/threadUebersicht";
    }

    private CompletableFuture<ThreadObj> starten(int i, ExecutorService executorService) {
        if (executorService == null) {
            return CompletableFuture.supplyAsync(() -> { return starten(i); });
        }
        else {
            return CompletableFuture.supplyAsync(() -> { return starten(i); }, executorService);
        }
    }

    private ThreadObj starten(int i) {
        ThreadObj res = new ThreadObj();
        res.setZahl(i);
        res.setGestartetAm(LocalDateTime.now());
        res.setName(Thread.currentThread().getName());
        int result = 1;
        for (int j = 0; j < i; j++) {
            try { Thread.sleep(10); } catch (Exception e) {}
            result = result + j;
        }
        res.setErgebnis(result);
        res.setBeendetAm(LocalDateTime.now());
        return res;
    }
}
