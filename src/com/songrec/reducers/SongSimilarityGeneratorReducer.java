package com.songrec.reducers;

import com.songrec.algorithms.PearsonCorrelationSimilarity;
import com.songrec.dto.*;
import com.songrec.utils.BoundedPriorityQueue;
import com.songrec.workflows.Thresholds;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

public class SongSimilarityGeneratorReducer extends Reducer<IntWritable, PlayCountPairsMap, IntWritable, SongVectorOrSimilarityScores> {
    @Override
    protected void reduce(IntWritable songId, Iterable<PlayCountPairsMap> playCountVectors, Context context) throws IOException, InterruptedException {
        Iterator<PlayCountPairsMap> iterator = playCountVectors.iterator();
        PlayCountPairsMap aggregatedMap = new PlayCountPairsMap(mergeMaps(iterator));
        BoundedPriorityQueue<SimilarityScore> topSimilarityScores = new BoundedPriorityQueue<SimilarityScore>(10);

        for (Map.Entry<Integer, List<PlayCountPair>> entry : aggregatedMap.entrySet()) {
            if (entry.getValue().size() < Thresholds.MINIMUM_NUMBER_SONGS_TO_COMPARE)
                continue;

            double similarityScore = similarityScore(entry.getValue());
            if (similarityScore > Thresholds.MINIMUM_SIMILARITY_SCORE)
                topSimilarityScores.add(new SimilarityScore(entry.getKey(), similarityScore));
        }

        ArrayList<SimilarityScore> result = topSimilarityScores.retrieve();
        if (!result.isEmpty()) {
            context.write(songId, new SongVectorOrSimilarityScores(new SimilarityScores(result)));
        }
    }

    private HashMap<Integer, List<PlayCountPair>> mergeMaps(Iterator<PlayCountPairsMap> iterator) {
        HashMap<Integer, List<PlayCountPair>> map = new HashMap<Integer, List<PlayCountPair>>();

        while (iterator.hasNext()) {
            PlayCountPairsMap playCountVector = iterator.next();
            for (Map.Entry<Integer, List<PlayCountPair>> entry : playCountVector.entrySet()) {
                if (map.containsKey(entry.getKey())) {
                    map.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return map;
    }

    private double similarityScore(List<PlayCountPair> playCountPairs) {
        ArrayList<Short> firstSongPlayCounts = new ArrayList<Short>();
        ArrayList<Short> secondSongPlayCounts = new ArrayList<Short>();
        for (int i = 0; i < playCountPairs.size(); i++) {
            PlayCountPair pair = playCountPairs.get(i);
            firstSongPlayCounts.add(pair.firstSongPlayCount());
            secondSongPlayCounts.add(pair.secondSongPlayCount());

        }
        return PearsonCorrelationSimilarity.score(firstSongPlayCounts, secondSongPlayCounts);
    }
}