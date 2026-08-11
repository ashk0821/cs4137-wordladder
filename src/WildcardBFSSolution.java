import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// Solution 2: Optimized #1, Wildcard-Pattern BFS. See README.md for the
// algorithm/complexity write-up.
public class WildcardBFSSolution implements WordLadderSolver {

    @Override
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> dictionary = new HashSet<>(wordList); // O(1) membership checks
        if (!dictionary.contains(endWord)) {
            return 0; // endWord isn't reachable if it's not even in the dictionary
        }

        int wordLength = beginWord.length();
        // patternMap: e.g. "h*t" -> ["hot", "hat", ...]; two words are neighbors
        // iff they share at least one wildcard pattern.
        Map<String, List<String>> patternMap = buildPatternMap(wordList, wordLength);

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.offer(beginWord);
        visited.add(beginWord);
        int level = 1; // beginWord itself counts as the first word in the sequence

        while (!queue.isEmpty()) {
            int size = queue.size(); // number of words at the current BFS level
            for (int i = 0; i < size; i++) {
                String word = queue.poll();
                if (word.equals(endWord)) {
                    return level; // shortest sequence length found
                }

                // Generate every wildcard pattern of `word` and look up its neighbors.
                for (int j = 0; j < wordLength; j++) {
                    String pattern = word.substring(0, j) + '*' + word.substring(j + 1);
                    for (String next : patternMap.getOrDefault(pattern, Collections.emptyList())) {
                        if (!visited.contains(next)) {
                            visited.add(next);
                            queue.offer(next); // enqueue for the next BFS level
                        }
                    }
                }
            }
            level++; // moving to the next BFS level = one more word in the sequence
        }

        return 0; // queue emptied without ever reaching endWord: unreachable
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
