import java.util.List;

// Common interface so TestRunner can run all solutions polymorphically.
// See README.md for the full write-up of each solution.
public interface WordLadderSolver {

    // To submit any one implementation to LeetCode directly: rename the
    // implementing class to "Solution" and drop "implements WordLadderSolver".
    int ladderLength(String beginWord, String endWord, List<String> wordList);
}
