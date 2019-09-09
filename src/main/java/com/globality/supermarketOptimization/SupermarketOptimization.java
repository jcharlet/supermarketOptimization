package com.globality.supermarketOptimization;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SupermarketOptimization {

    public static void main(String[] args) throws IOException {
        String fileName = args[0];
        String sigma = args[1];

        System.out.print(new SupermarketOptimization().analyseFile(fileName, Integer.valueOf(sigma)));
    }

    public String analyseFile(String fileName, Integer sigma) throws IOException{
        try(Stream<String> lines = Files.lines(new File(fileName).toPath())){
            return lines.map(line -> {
                return process(line);
            }).collect(Collectors.joining( System.lineSeparator()));
        }
    }

    private String process(String line) {
        return line;
    }

}
