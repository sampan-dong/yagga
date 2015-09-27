package com.github.yu55.gog;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Component
public class GrepService {

    private static final Logger logger = LoggerFactory.getLogger(GrepService.class);

    private static final String COMMAND = "git grep -n ";

    private static final int SEARCH_LIMIT = 999;

    private Repositories repositories;

    @Autowired
    public GrepService(Repositories repositories) {
        this.repositories = repositories;
    }

    public List<String> grep(GrepRequest wanted) {
        List<String> allFindings = new LinkedList<>();
        List<File> directories = repositories.getDirectories();

        CommandLine commandLine = CommandLine.parse(COMMAND + wanted.getWanted());
        DefaultExecutor executor = new DefaultExecutor();
        ExecutorStreamHandler executorStreamHandler = new ExecutorStreamHandler();
        executor.setStreamHandler(new PumpStreamHandler(executorStreamHandler));
        for (File directory : directories) {
            if (wanted.getRepositories().contains(directory.getName())) {
                executor.setWorkingDirectory(directory);
                executorStreamHandler.setRepositoryName(directory.getName());
                try {
                    executor.execute(commandLine);
                    allFindings.addAll(executorStreamHandler.getFindings());
                } catch (IOException e) {
                    logger.warn("Problem while searching '{}' repository: {}", directory.getAbsolutePath(), e.getMessage());
                }
                executorStreamHandler.clearFindings();

                if (allFindings.size() > SEARCH_LIMIT) {
                    allFindings = allFindings.subList(0, SEARCH_LIMIT);
                    allFindings.add(String.format("More results than %d. Aborting...", SEARCH_LIMIT));
                    break;
                }
            }
        }

        if (allFindings.isEmpty()) {
            allFindings.add("No findings in selected repositories.");
        }

        return allFindings;
    }

    private class ExecutorStreamHandler extends LogOutputStream {

        private List<String> findings = new LinkedList<>();
        private String repositoryName = "";

        @Override
        protected void processLine(String line, int logLevel) {
            findings.add(String.format("[%s] %s", repositoryName, line));
        }

        public List<String> getFindings() {
            return findings;
        }

        public void clearFindings() {
            findings.clear();
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }
    }
}