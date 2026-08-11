import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// Solution 1: Brute Force. See README.md for the algorithm/complexity write-up.
public class BruteForceSolution implements WordLadderSolver {

    @Override
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        // Fast exit: if endWord never appears in the dictionary, no sequence can exist.
        if (!wordList.contains(endWord)) {
            return 0;
        }

        // Collect every candidate word into one indexed list: beginWord at index 0,
        // followed by wordList (skipping a duplicate of beginWord if present).
        List<String> nodes = new ArrayList<>();
        nodes.add(beginWord);
        int endIndex = -1;
        for (String w : wordList) {
            if (w.equals(beginWord)) {
                continue; // don't add beginWord twice
            }
            nodes.add(w);
            if (w.equals(endWord)) {
                endIndex = nodes.size() - 1; // remember where endWord landed
            }
        }
        if (endIndex == -1) {
            // Only reachable if endWord equaled beginWord, which the problem disallows.
            return 0;
        }

        int n = nodes.size();

        // Build an adjacency list by comparing every pair of words: O(n^2 * L).
        List<List<Integer>> adjacency = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adjacency.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (differsByOneLetter(nodes.get(i), nodes.get(j))) {
                    adjacency.get(i).add(j); // edge i -> j
                    adjacency.get(j).add(i); // edge j -> i (undirected)
                }
            }
        }

        // Standard level-order BFS from beginWord (index 0).
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(0);
        visited[0] = true;
        int level = 1; // beginWord itself counts as the first word in the sequence

        while (!queue.isEmpty()) {
            int size = queue.size(); // number of nodes at the current BFS level
            for (int i = 0; i < size; i++) {
                int current = queue.poll();
                if (current == endIndex) {
                    return level; // shortest sequence length found
                }
                for (int neighbor : adjacency.get(current)) {
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        queue.offer(neighbor);
                    }
                }
            }
            level++; // moving to the next BFS level = one more word in the sequence
        }

        return 0; // queue emptied without ever reaching endIndex: unreachable
    }

    // Returns true if a and b are the same length and differ in exactly one position.
    private boolean differsByOneLetter(String a, String b) {
        int diffCount = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                diffCount++;
                if (diffCount > 1) {
                    return false; // early exit once more than one difference is found
                }
            }
        }
        return diffCount == 1;
    }
}
