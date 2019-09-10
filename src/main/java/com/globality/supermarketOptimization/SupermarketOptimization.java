package com.globality.supermarketOptimization;

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
    private ConcurrentHashMap<String,Combination> mapOfCombinations = new ConcurrentHashMap<>();
    private CacheHelper combinationsCache = new CacheHelper();
    public static void main(String[] args) throws IOException {
        String fileName = args[0];
        String sigma = args[1];

        System.out.print(new SupermarketOptimization().analyseFile(fileName, Integer.valueOf(sigma)));
    }

    public String analyseFile(String fileName, Integer sigma) throws IOException{
        try(Stream<String> linesStream = Files.lines(new File(fileName).toPath())){
            List<String> linesFilteredOnNumberOfElements = filterLinesByNumberOfIdsPerLine(linesStream, MIN_NUMBER_OF_IDS_PER_LINE);

            populateProductsCountMap(linesFilteredOnNumberOfElements.stream());

            List<String> linesWithIdsFilteredByTotalCount = filterIdsWhichOccurLessThanSigma(linesFilteredOnNumberOfElements.stream(), sigma);

            List<String> linesPrepared = filterLinesByNumberOfIdsPerLine(linesWithIdsFilteredByTotalCount.stream(), MIN_NUMBER_OF_IDS_PER_LINE);

            populateCombinationsMap(linesPrepared);

            return createReportOfCombinations(sigma);
        }
    }

    private String createReportOfCombinations(Integer sigma) {
        logger.info(">createReportOfCombinations");

        StringBuilder stringBuilder = new StringBuilder();

        String headerLine = "Item size, Nb occurences, values" + System.lineSeparator();
        logger.info(headerLine);
        stringBuilder.append(headerLine);

        combinationsCache.getCombinationsCache().spliterator().forEachRemaining(
            combination -> {
                String line = combination.getKey().toString().split(" ").length + ", " + combination.getValue() + ", " + combination.getKey();
                logger.info(line);
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }
        );
        return stringBuilder.toString();
    }

    private List<Object> populateCombinationsMap(List<String> lines) {
        logger.info(">populateCombinationsMap");
        for (int i = 0; i < lines.size(); i++) {
//        for (int i = 0; i < 1000; i++) {
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
                                        combinationsCache.addCombinationIfMissing(String.join(" ", combination),indexLine2);
                                    });
                        }
                        return null;
                    })
                    .count();
        }
        return null;
    }

    private List<String> filterIdsWhichOccurLessThanSigma(Stream<String> linesStream, Integer sigma) {
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

    private void populateProductsCountMap(Stream<String> linesStream) {
        logger.info(">populateProductsCountMap");
        linesStream
            .forEach(line -> {
                String[] products = line.split(" ");
                Stream.of(products).forEach(id -> {
                    mapOfProducts.compute(id, (idKey, count) -> count == null ? 1 : count + 1);
                });
            });
    }

}
