# Word Ladder: CS4137 Group 8

**Problem:** [LeetCode 127. Word Ladder](https://leetcode.com/problems/word-ladder/)

**Group Members:**
- Ayal Yakobe (amy2127)
- Leen Alshorafa (laa2202)
- Aashir Khan (ak5445)

## Problem Statement

Given `beginWord`, `endWord`, and a dictionary `wordList`, return the number of
words in the shortest transformation sequence from `beginWord` to `endWord`,
where each step changes exactly one letter and every intermediate word must
appear in `wordList`. Return `0` if no such sequence exists.

Signature required by the judge:

```java
class Solution {
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        ...
    }
}
```

Constraints: `1 <= beginWord.length <= 10`, `endWord.length == beginWord.length`,
`1 <= wordList.length <= 5000`, all words lowercase and unique, `beginWord != endWord`.

## Repository Layout

```
src/
  WordLadderSolver.java        common interface implemented by all three solutions
  BruteForceSolution.java      Solution 1: brute force
  WildcardBFSSolution.java     Solution 2: optimized (wildcard-pattern BFS)
  BidirectionalBFSSolution.java Solution 3: further optimized (bidirectional wildcard BFS)
  TestRunner.java              program runner: runs all 3 solutions against all test cases
testcases/
  input1.txt .. input10.txt    test inputs
  output1.txt .. output10.txt  expected outputs
README.md
```

## Solutions

We implemented three versions to show a progression from a naive approach to
two increasingly optimized ones. Each is a fully standalone, LeetCode-submittable
class: to submit one individually, rename the class to `Solution` and drop
`implements WordLadderSolver`.

### 1. `BruteForceSolution`: Brute Force

Builds the transformation graph by comparing **every pair** of words
character-by-character to check if they differ by exactly one letter, then
runs a plain BFS from `beginWord` counting levels until `endWord` is reached.

- Graph construction: `O(N^2 * L)` (N = word count, L = word length)
- BFS traversal: `O(N^2)` worst case
- Simple and obviously correct, but the pairwise comparison makes it the
  slowest solution as `wordList` grows: this is intentionally the "first
  attempt" baseline.

### 2. `WildcardBFSSolution`: Optimized #1 (Wildcard-Pattern BFS)

Instead of comparing every word to every other word, each word generates `L`
"wildcard" patterns (e.g. `"hot"` → `"*ot"`, `"h*t"`, `"ho*"`), bucketed in a
`HashMap<String, List<String>>`. Two words are neighbors iff they share a
bucket. BFS then expands through these pattern buckets rather than scanning
the whole word list.

- Building the pattern map: `O(N * L^2)`
- This is the standard accepted approach for this problem and is
  dramatically faster than the brute force for large `wordList` sizes.

### 3. `BidirectionalBFSSolution`: Optimized #2 (Bidirectional Wildcard BFS)

Builds on the same wildcard-pattern bucketing, but searches from `beginWord`
and `endWord` at the same time, always expanding whichever frontier is
currently smaller. Each side tracks its own distance map; the search stops
the moment a word discovered from one side is already known on the other
side, and the answer is the sum of the two distances (minus 1 for the shared
meeting word).

- Same `O(N * L^2)` graph-construction bound as solution 2, but in practice
  visits far fewer nodes: roughly `O(k^(d/2))` vs. `O(k^d)` for branching
  factor `k` and shortest-path depth `d`: so it tends to win as the graph
  gets deeper and more branched. On our thinner, mostly linear stress test
  (test 10) its advantage over solution 2 was modest, since bidirectional
  BFS mainly pays off when the graph branches heavily on both ends; see
  `testcases` output below for actual measured numbers.

## Building & Running

```bash
javac -d out src/*.java
java -cp out TestRunner testcases
```

`TestRunner` reads every `inputN.txt` / `outputN.txt` pair from the given
directory (defaults to `testcases`), runs all three solutions against each,
verifies the result against the expected output, and prints a PASS/FAIL
report plus a per-solution timing summary (wall-clock via
`System.nanoTime()`).

## Test Case Format

`inputN.txt`:
```
beginWord endWord
N
<word 1>
<word 2>
...
<word N>
```

`outputN.txt`: a single line containing the expected integer result.

## Test Case Descriptions

| # | Covers |
|---|--------|
| 1 | Classic example from the problem statement; a branching graph with multiple equally-short paths (`hit→hot→dot→dog→cog` or `hit→hot→lot→log→cog`). Expected `5`. |
| 2 | `endWord` missing from `wordList`: no sequence possible. Expected `0`. |
| 3 | Minimal case: a single word in `wordList` directly adjacent to `beginWord`. Expected `2`. |
| 4 | `endWord` present in `wordList` but in a disconnected component (unreachable via single-letter steps). Expected `0`. |
| 5 | Hand-engineered 4-letter chain of 9 words with no shortcuts, to confirm the exact shortest length is found on a longer, unambiguous path. Expected `10`. |
| 6 | "Diamond" graph with two equally short paths (`aaa→aab→abb` and `aaa→aba→abb`) to confirm BFS returns the correct length regardless of which path it explores first. Expected `3`. |
| 7 | Edge case: `beginWord` is redundantly also present in `wordList`; verifies no double-counting/crash. Expected `3`. |
| 8 | Different word length (5 letters) plus an unrelated distractor word (`plane`) that isn't adjacent to anything on the path. Expected `3`. |
| 9 | Mid-size randomly generated case (157 words, 5-letter alphabet) with an engineered 7-step chain embedded among random noise words, for a first look at performance differences. Expected `8`. |
| 10 | Stress test near the constraint limits (3515 words, 10-letter alphabet) with a 15-step engineered chain embedded among heavy random noise: this is where the brute force solution's `O(N^2 * L)` cost becomes clearly visible against the two optimized solutions. Expected `16`. |

Test cases 9 and 10 were generated with a fixed random seed and their
expected outputs were computed by running `WildcardBFSSolution` (verified
correct against the hand-computed cases 1–8) as a reference implementation,
rather than hand-tracing a large randomized graph.

## Sample Run Output

```
Solution             Pass/Total  Total Time(ms)    Avg Time(ms)/test
BruteForce               10/10            48.769                4.877
WildcardBFS              10/10            19.153                1.915
BidirectionalBFS         10/10            14.172                1.417
```

(All 30 solution/test-case combinations pass. Exact millisecond timings vary
run to run with JIT warm-up/JVM state: re-run `TestRunner` locally to
reproduce, or profile with JProfiler for a detailed method-level breakdown.)

