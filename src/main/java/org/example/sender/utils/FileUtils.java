package org.example.sender.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    public static String getChairSequence(final File file) throws IOException {
        try (final BufferedReader in = new BufferedReader(new FileReader(file))){
            final StringBuilder sb = new StringBuilder();
            while(in.readLine() != null) {
                sb.append(in.readLine()).append("\n");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
    }
}
