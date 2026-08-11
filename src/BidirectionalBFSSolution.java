import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// Solution 3: Optimized #2, Bidirectional Wildcard BFS. See README.md for the
// algorithm/complexity write-up.
public class BidirectionalBFSSolution implements WordLadderSolver {

    @Override
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> dictionary = new HashSet<>(wordList); // O(1) membership checks
        if (!dictionary.contains(endWord)) {
            return 0; // endWord isn't reachable if it's not even in the dictionary
        }

        int wordLength = beginWord.length();
        // Same wildcard-pattern bucketing as WildcardBFSSolution.
        Map<String, List<String>> patternMap = buildPatternMap(wordList, wordLength);

        // distFromBegin/distFromEnd: word -> number of words in the shortest path
        // from that word's own origin (beginWord or endWord) to it, inclusive.
        Map<String, Integer> distFromBegin = new HashMap<>();
        Map<String, Integer> distFromEnd = new HashMap<>();
        distFromBegin.put(beginWord, 1);
        distFromEnd.put(endWord, 1);

        Queue<String> queueBegin = new LinkedList<>();
        Queue<String> queueEnd = new LinkedList<>();
        queueBegin.offer(beginWord);
        queueEnd.offer(endWord);

        while (!queueBegin.isEmpty() && !queueEnd.isEmpty()) {
            Integer result;
            // Always expand whichever frontier currently has fewer nodes, to keep
            // both search trees as shallow/narrow as possible.
            if (queueBegin.size() <= queueEnd.size()) {
                result = expandOneLevel(queueBegin, distFromBegin, distFromEnd, patternMap, wordLength);
            } else {
                result = expandOneLevel(queueEnd, distFromEnd, distFromBegin, patternMap, wordLength);
            }
            if (result != null) {
                return result; // the two searches met; result is the full ladder length
            }
        }

        return 0; // one side ran out of nodes without ever meeting the other: unreachable
    }

    // Expands every word currently in `queue` by one level, updating distOwn.
    // If a newly-discovered neighbor already has a distance in distOther, the
    // two searches have met there; returns the combined ladder length, else null.
    private Integer expandOneLevel(Queue<String> queue, Map<String, Integer> distOwn,
                                    Map<String, Integer> distOther,
                                    Map<String, List<String>> patternMap, int wordLength) {
        int levelSize = queue.size(); // process exactly this level, not anything enqueued during it
        for (int i = 0; i < levelSize; i++) {
            String word = queue.poll();
            int distance = distOwn.get(word);

            for (int j = 0; j < wordLength; j++) {
                String pattern = word.substring(0, j) + '*' + word.substring(j + 1);
                for (String next : patternMap.getOrDefault(pattern, Collections.emptyList())) {
                    if (distOther.containsKey(next)) {
                        // Met at `next`: stitch the two half-paths together
                        // (next is distance+1 on this side; distOther already counts it once).
                        return distance + distOther.get(next);
                    }
                    if (!distOwn.containsKey(next)) {
                        distOwn.put(next, distance + 1);
                        queue.offer(next); // enqueue for this side's next level
                    }
                }
            }
        }
        return null; // no meeting point found at this level yet
    }

    // Maps each wildcard pattern (e.g. "h*t") to every word in wordList matching it.
    private Map<String, List<String>> buildPatternMap(List<String> wordList, int wordLength) {
        Map<String, List<String>> patternMap = new HashMap<>();
        for (String word : wordList) {
            for (int j = 0; j < wordLength; j++) {
                // Replace character at position j with '*' to form the pattern key.
                String pattern = word.substring(0, j) + '*' + word.substring(j + 1);
                patternMap.computeIfAbsent(pattern, k -> new java.util.ArrayList<>()).add(word);
            }
        }
        return patternMap;
    }
}
