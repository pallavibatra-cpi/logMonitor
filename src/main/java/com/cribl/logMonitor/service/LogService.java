package com.cribl.logMonitor.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class LogService {

    private static final String LOG_DIR = "/var/log";
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._:-]+$");

    public LogService() {
    }

    /**
     * Streams the specified number of log entries from the end of the given log file.
     *
     * @param filename Name of the log file
     * @param lines    Number of lines to retrieve
     * @param keyword  Keyword to filter the log entries
     * @param emitter  ResponseBodyEmitter to stream log entries
     * @throws IOException If an error occurs while reading the file
     */
    public void streamLogEntries(String filename, String lines, String keyword, ResponseBodyEmitter emitter) throws IOException {

        if (!isValidFilename(filename)) {
            emitter.send("Invalid filename: " + filename);
            emitter.complete();
            return;
        }

        File logFile = new File(LOG_DIR, filename);

        if (!logFile.exists() || !logFile.isFile()) {
            emitter.send("File not found: " + filename);
            emitter.complete();
            return;
        }

        // validate and add lines to fetch
        double linesToFetch = 0;

        try {
            if (StringUtils.hasLength(lines)) {
                linesToFetch = Double.parseDouble(lines);
            }
        }catch (NumberFormatException e){
            emitter.send("Invalid input: " + lines);
            emitter.complete();
            return;
        }

        // access the file from the very end and divide it into chunks
        try (RandomAccessFile accessFile = new RandomAccessFile(logFile, "r")) {
            long position = accessFile.length();

            while (position > 0) {
                int bufferSize = 1024;
                position = Math.max(position - bufferSize, 0);
                accessFile.seek(position);

                byte[] buffer = new byte[bufferSize];
                accessFile.read(buffer);
                String chunk = new String(buffer, StandardCharsets.UTF_8);

                List<String> chunkLines = readLines(chunk);
                Collections.reverse(chunkLines);

                for (String line : chunkLines) {
                    if (linesToFetch == 0) {
                        if (keyword == null || line.contains(keyword)) {
                            emitter.send(line + "\n");
                        }
                    }else {
                        if (keyword == null || line.contains(keyword)) {
                            emitter.send(line + "\n");
                            linesToFetch--;
                        }
                        if (linesToFetch <= 0) break;
                    }
                }
            }
        }

        emitter.complete();
    }

    /**
     * Validates the given filename to ensure it only contains safe characters.
     *
     * @param filename The filename to validate
     * @return True if the filename is valid, false otherwise
     */
    private boolean isValidFilename(String filename) {
        return FILENAME_PATTERN.matcher(filename).matches();
    }

    /**
     * Reads lines from a given string using BufferedReader.
     *
     * @param text The input text
     * @return A list of lines
     * @throws IOException If an error occurs while reading the lines
     */
    private List<String> readLines(String text) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
