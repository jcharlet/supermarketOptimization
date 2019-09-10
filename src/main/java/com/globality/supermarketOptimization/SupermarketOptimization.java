package com.globality.supermarketOptimization;

import com.globality.supermarketOptimization.domain.CombinationInfo;
import com.globality.supermarketOptimization.storage.CombinationsStore;
import org.paukov.combinatorics3.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SupermarketOptimization {

    final private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int MIN_NUMBER_OF_IDS_PER_LINE = 3;
    private ConcurrentHashMap<String,Integer> mapOfProducts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CombinationInfo> mapOfCombinations = new ConcurrentHashMap<>();
    private CombinationsStore combinationsCache = new CombinationsStore("/mnt/da7f9961-d147-4d0b-82c5-9e3594ec7170/tmp");

    /**
     * Command Line Interface to run the application on a transaction database
     * @param args: filePath, sigma
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String filePath = args[0];
        String sigma = args[1];

        SupermarketOptimization supermarketOptimization = new SupermarketOptimization();
        System.out.print(supermarketOptimization.analyseFile(filePath, Integer.valueOf(sigma)));
    }

    /**
     * Main method to process a file and create a report on its combinations
     * @param fileName
     * @param sigma
     * @return
     * @throws IOException
     */
    public String analyseFile(String fileName, Integer sigma) throws IOException{
        try(Stream<String> linesStream = Files.lines(new File(fileName).toPath())){
            List<String> linesPrepared = cleanDataset(sigma, linesStream);

            findAndStoreAllRecurringCombinations(linesPrepared);

            return createReportOfCombinations(sigma);
        }
    }

    private List<String> cleanDataset(Integer sigma, Stream<String> linesStream) {
        List<String> linesFilteredOnNumberOfElements = filterLinesByNumberOfIdsPerLine(linesStream, MIN_NUMBER_OF_IDS_PER_LINE);

        countProductIdsOccurrences(linesFilteredOnNumberOfElements.stream());

        List<String> linesWithIdsFilteredByTotalCount = filterProductIdsWhichOccurLessThanSigma(linesFilteredOnNumberOfElements.stream(), sigma);

        return filterLinesByNumberOfIdsPerLine(linesWithIdsFilteredByTotalCount.stream(), MIN_NUMBER_OF_IDS_PER_LINE);
    }

    private String createReportOfCombinations(Integer sigma) {
        logger.info(">createReportOfCombinations");

        StringBuilder stringBuilder = new StringBuilder();

        String headerLine = "Item size, Nb occurences, values";
        logger.info(headerLine);
        stringBuilder.append(headerLine);

        combinationsCache.getIterator().forEachRemaining(
            combination -> {
                String line = combination.getKey().toString().split(" ").length + ", " + combination.getValue() + ", " + combination.getKey();
                logger.info(line);
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(line);
            }
        );
        return stringBuilder.toString();
    }

    private List<Object> findAndStoreAllRecurringCombinations(List<String> lines) {
        logger.info(">populateCombinationsMap");
        for (int i = 0; i < lines.size(); i++) {
            if(i%100==0){
                logger.info("index: " + i + " , combinations cache put operations: " + combinationsCache.putOperations());
            }
            String line1 = lines.get(i);
            int indexLine1 = i;
            AtomicInteger atomicInteger = new AtomicInteger(0);
            IntStream.range(0, lines.size())
                    .filter(indexLine2 -> indexLine2 != indexLine1)
                    .mapToObj(indexLine2 -> {
                        String line2 = lines.get(indexLine2);
                        List<String> line1Ids = Arrays.asList(line1.split(" "));
                        List<String> line2Ids = Arrays.asList(line2.split(" "));
                        List<String> commonCombinations = line1Ids.stream().filter(line2Ids::contains).collect(Collectors.toList());
                        for (int k = 3; k <= commonCombinations.size(); k++) {
                            //        for (int i = 3; i <4 ; i++) {
                            Generator.combination(commonCombinations)
                                    .simple(k)
                                    .forEach(combination -> {
                                        combinationsCache.addIfMissing(String.join(" ", combination),indexLine2);
                                    });
                        }
                        return null;
                    })
                    .count(); //this .count() method is unused and only serves to execute the processing of the stream
        }
        return null;
    }

    private List<String> filterProductIdsWhichOccurLessThanSigma(Stream<String> linesStream, Integer sigma) {
        logger.info(">filterIdsWhichOccurLessThanSigma");
        return linesStream
                .map(line -> {
                    List<String> ids = Stream.of(line.split(" "))
                            .filter(id -> id != null && mapOfProducts.get(id) != null && mapOfProducts.get(id) >= sigma)
                            .collect(Collectors.toList());
                    if(ids.size()==0){
                        return null;
                    }
                    return ids.stream().collect(Collectors.joining(" "));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> filterLinesByNumberOfIdsPerLine(Stream<String> linesStream, int minNumberOfIdsPerLine) {
        logger.info(">filterLinesByNumberOfIdsPerLine");
        return linesStream
                .map(line -> {
                    String[] products = line.split(" ");
                    if(products.length< minNumberOfIdsPerLine){
                        return null;
                    }
                    return line;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void countProductIdsOccurrences(Stream<String> linesStream) {
        logger.info(">countProductIdsOccurrences");
        linesStream
            .forEach(line -> {
                String[] products = line.split(" ");
                Stream.of(products).forEach(id -> {
                    mapOfProducts.compute(id, (idKey, count) -> count == null ? 1 : count + 1);
                });
            });
    }

}
