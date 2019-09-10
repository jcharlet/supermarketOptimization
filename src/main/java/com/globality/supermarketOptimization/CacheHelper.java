package com.globality.supermarketOptimization;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.internal.statistics.DefaultStatisticsService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CacheHelper {

    private final DefaultStatisticsService statisticsService;
    private PersistentCacheManager cacheManager;
    private Cache<String, Combination> combinationsCache;

    public CacheHelper() {
        statisticsService = new DefaultStatisticsService();
        cacheManager = CacheManagerBuilder
                .newCacheManagerBuilder()
                .using(statisticsService)
                .with(CacheManagerBuilder.persistence(new File("/mnt/da7f9961-d147-4d0b-82c5-9e3594ec7170/tmp", "globalityCache")))
                .build(true);

        combinationsCache = cacheManager
                .createCache("combinationsCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    String.class, Combination.class, ResourcePoolsBuilder.newResourcePoolsBuilder()
//                                .heap(1950, MemoryUnit.MB)
                                .heap(6000, MemoryUnit.MB)
                                .disk(70, MemoryUnit.GB,false)
//                                .offheap(2, MemoryUnit.GB)
                ));
    }

    public Cache<String, Combination> getCombinationsCache() {
        return cacheManager.getCache("combinationsCache", String.class, Combination.class);
    }

    public long putOperations() {
        return statisticsService.getCacheStatistics("combinationsCache").getCachePuts();
    }

    public void addCombinationIfMissing(String combination, int indexLine) {
        synchronized (combination){
            Combination combinationContainer = combinationsCache.get(combination);
            if(combinationContainer== null){
                combinationContainer = new Combination();
                combinationContainer.lines=String.valueOf(indexLine);
                combinationContainer.count+=1;
                combinationsCache.put(combination,combinationContainer);
            }else{
                List<String> lines = new ArrayList<>(Arrays.asList(combinationContainer.lines.split(" ")));
                if(!lines.contains(String.valueOf(indexLine))){
                    lines.add(String.valueOf(indexLine));
                    combinationContainer.lines=String.join(" ", lines);
                    combinationContainer.count+=1;
                    combinationsCache.put(combination,combinationContainer);
                }
            }
        }
    }
}