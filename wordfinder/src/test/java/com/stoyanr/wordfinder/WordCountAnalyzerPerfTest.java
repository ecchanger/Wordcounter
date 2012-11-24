package com.stoyanr.wordfinder;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class WordCountAnalyzerPerfTest {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789_";
    
    private static final int MAX_LENGTH = 12;

    @Parameters
    public static Collection<Object[]> data() {
        // @formatter:off
        Object[][] data = new Object[][] { 
            { 2000000, 1000000000, 10 }, 
        };
        // @formatter:on
        return asList(data);
    }

    private final int numWords;
    private final int maxCount;
    private final int top;

    private WordCountAnalyzer analyzer;

    public WordCountAnalyzerPerfTest(int numWords, int maxCount, int top) {
        this.numWords = numWords;
        this.maxCount = maxCount;
        this.top = top;
    }

    @Before
    public void setUp() {
        analyzer = new WordCountAnalyzer();
    }

    @Test
    public void test() throws Exception {
        Map<String, Integer> counts = counts();
        SortedMap<Integer, Set<String>> sorted = getSorted(counts);
        System.out.printf("Processing %d words ...\n", counts.size());
        long time0 = System.currentTimeMillis();
        SortedMap<Integer, Set<String>> sortedx = analyzer.analyze(counts, top);
        long time1 = System.currentTimeMillis();
        System.out.printf("Analyzed %d words in %d ms\n", counts.size(), (time1 - time0));
        printSorted(sortedx);
        TestUtils.assertEqualSortedMaps(sorted, sortedx);
    }
    
    private Map<String, Integer> counts() {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < numWords; i++) {
            counts.put(getRandomWord(), getRandomCount());
        }
        return counts;
    }

    private String getRandomWord() {
        StringBuilder sb = new StringBuilder();
        int length = (int) (Math.random() * MAX_LENGTH) + 1;
        for (int j = 0; j < length; j++) {
            int index = (int) (Math.random() * ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }

    private int getRandomCount() {
        return (int) (Math.random() * maxCount);
    }

    private SortedMap<Integer, Set<String>> getSorted(Map<String, Integer> counts) {
        SortedMap<Integer, Set<String>> sorted = new TreeMap<>(comparator());
        for (Entry<String, Integer> e : counts.entrySet()) {
            String word = e.getKey();
            int count = e.getValue();
            if (sorted.containsKey(count)) {
                sorted.get(count).add(word);
            } else {
                Set<String> set = new HashSet<>();
                set.add(word);
                sorted.put(count, set);
            }
        }
        return TestUtils.getHead(sorted, top);
    }

    private void printSorted(SortedMap<Integer, Set<String>> sorted) {
        int i = 0;
        for (Entry<Integer, Set<String>> e : sorted.entrySet()) {
            int count = e.getKey();
            Set<String> words = e.getValue();
            for (String word : words) {
                System.out.printf("%12s: %d\n", word, count);
                if (++i == top) {
                    return;
                }
            }
        }
    }

    private static Comparator<Integer> comparator() {
        return new WordCountAnalyzer.ReverseComparator();
    }
}
