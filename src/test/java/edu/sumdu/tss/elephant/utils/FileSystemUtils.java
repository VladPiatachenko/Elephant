package edu.sumdu.tss.elephant.utils;

import edu.sumdu.tss.elephant.Preset;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.*;

public final class FileSystemUtils {
    private FileSystemUtils() { }

    public static boolean deleteDirectory(final File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directory.delete();
    }

    public static void copyDirectory(final File sourceDirectoryLocation, final File destinationDirectoryLocation)
            throws IOException {
        String src = sourceDirectoryLocation.toString();
        String dest = destinationDirectoryLocation.toString();

        Files.createDirectories(Paths.get(sourceDirectoryLocation.getAbsolutePath()));

        Files.walk(Paths.get(src))
                .forEach(source -> {
                    Path destination = Paths.get(dest, source.toString()
                            .substring(src.length()));
                    try {
                        Files.copy(source, destination);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static String waitForFile(final Path directory, final String ext) throws IOException, InterruptedException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        WatchKey key = directory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        String path;
        for (;;) {
            for (WatchEvent<?> event : key.pollEvents()) {
                path = event.context().toString();
                if (FilenameUtils.getExtension(path).equals(ext)) {
                    return path;
                }
            }
            Thread.sleep(Preset.FILESYSTEM_WATCHER_THRESHOLD);
        }
    }

    public static void appendToFile(final File file, final String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(content);
        }
    }

    public static void writeToFile(final File file, final String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

    public static String getContent(final File file) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        }

        return content.toString();
    }
}
