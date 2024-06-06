package com.cribl.logMonitor.controller;


import com.cribl.logMonitor.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class LogController {

    @Autowired
    private LogService logService;

    /**
     * Endpoint to get log entries from a specified file.
     *
     * @param filename Name of the log file
     * @param lines    Number of lines to retrieve
     * @param keyword  Keyword to filter the log entries
     * @return ResponseBodyEmitter to stream log entries
     */
    @GetMapping("/v1/logs")
    public ResponseBodyEmitter getLogEntries(@RequestParam String filename,
                                             @RequestParam(required = false, defaultValue = "") String lines,
                                             @RequestParam(required = false) String keyword) {
        long startTime = System.currentTimeMillis();
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                // URL-decode the filename
                String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8.name());
                logService.streamLogEntries(decodedFilename, lines, keyword, emitter);
            } catch (IOException e) {
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });
        long endTime = System.currentTimeMillis();
        System.out.println("Took " + (endTime - startTime) + "ms");
        return emitter;
    }
}
