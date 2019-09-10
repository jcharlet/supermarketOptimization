package com.globality.supermarketOptimization;

import org.paukov.combinatorics3.Generator;

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
    public static final int MIN_NUMBER_OF_IDS_PER_LINE = 3;
    private ConcurrentHashMap<String,Integer> mapOfProducts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Combination> mapOfCombinations = new ConcurrentHashMap<>();

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

    private void log(String s) {
        System.out.println(Calendar.getInstance().getTime().toString() + " " + s );
    }

    private String createReportOfCombinations(Integer sigma) {
        log(">createReportOfCombinations");
        return "Item size, Nb occurences, values" + System.lineSeparator() + mapOfCombinations.entrySet().stream()
//                .sorted(Comparator.comparing()
                .filter(combination -> combination.getValue().count>=sigma)

                .map(entry -> {
                    return entry.getKey().toString().split(" ").length + ", " + entry.getValue() + ", " +entry.getKey() ;
                })
                .collect(Collectors.joining( System.lineSeparator()));
    }

    private List<Object> populateCombinationsMap(List<String> lines) {
        log(">populateCombinationsMap");
//        for (int i = 0; i < lines.size(); i++) {
        for (int i = 0; i < 500; i++) {
            if(i%100==0){
                log("index: " + i + " , mapOfCombinations size: " + mapOfCombinations.size());
            }
            String line1 = lines.get(i);
            int indexLine1 = i;
            AtomicInteger atomicInteger = new AtomicInteger(0);
            IntStream.range(0, lines.size())
                    .filter(indexLine2 -> indexLine2 != indexLine1)
                    .mapToObj(indexLine2 -> {
//                        int countLines2 = atomicInteger.incrementAndGet();
//                        if(countLines2 %2500==0){
//                            log("        nb elements processed from lines2: " + countLines2 );
//                        }
                        String line2 = lines.get(indexLine2);
                        List<String> line1Ids = Arrays.asList(line1.split(" "));
                        List<String> line2Ids = Arrays.asList(line2.split(" "));
                        List<String> commonCombinations = line1Ids.stream().filter(line2Ids::contains).collect(Collectors.toList());
                        for (int k = 3; k <= commonCombinations.size(); k++) {
                            //        for (int i = 3; i <4 ; i++) {
                            Generator.combination(commonCombinations)
                                    .simple(k)
                                    .forEach(combination -> {
                                        mapOfCombinations.compute(String.join(" ", combination),
                                                (stringCombination, combinationContainer) -> {
                                            if(combinationContainer== null){
                                                combinationContainer = new Combination();
                                            }
                                            if(!combinationContainer.lines.contains(indexLine2)){
                                                combinationContainer.lines.add(indexLine2);
                                                combinationContainer.count+=1;
                                            }
                                            return combinationContainer;
                                        });
                                    });
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private List<String> filterIdsWhichOccurLessThanSigma(Stream<String> linesStream, Integer sigma) {
        log(">filterIdsWhichOccurLessThanSigma");
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
        log(">filterLinesByNumberOfIdsPerLine");
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
        log(">populateProductsCountMap");
        linesStream
            .forEach(line -> {
                String[] products = line.split(" ");
                Stream.of(products).forEach(id -> {
                    mapOfProducts.compute(id, (idKey, count) -> count == null ? 1 : count + 1);
                });
            });
    }

    private class Combination {
        Integer count = 0;
        List<Integer> lines = new ArrayList<>();

        @Override
        public String toString() {
            return String.valueOf(count);
        }
    }
}
