import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Program runner: runs all three solutions against every test case and
// reports correctness + timing. See README.md for input/output file format
// and usage instructions.
public class TestRunner {

    // One parsed test case: the ladderLength() inputs plus the expected result.
    private static final class TestCase {
        final String name;
        final String beginWord;
        final String endWord;
        final List<String> wordList;
        final int expected;

        TestCase(String name, String beginWord, String endWord, List<String> wordList, int expected) {
            this.name = name;
            this.beginWord = beginWord;
            this.endWord = endWord;
            this.wordList = wordList;
            this.expected = expected;
        }
    }

    public static void main(String[] args) throws IOException {
        // testcases directory can be overridden via the first CLI argument.
        String testcasesDir = args.length > 0 ? args[0] : "testcases";
        List<TestCase> testCases = loadTestCases(new File(testcasesDir));

        if (testCases.isEmpty()) {
            System.out.println("No test cases found in: " + testcasesDir);
            return;
        }

        // Solutions under test, in presentation order: brute force -> optimized -> more optimized.
        // LinkedHashMap keeps insertion order so the report prints in this order.
        Map<String, WordLadderSolver> solutions = new LinkedHashMap<>();
        solutions.put("BruteForce", new BruteForceSolution());
        solutions.put("WildcardBFS", new WildcardBFSSolution());
        solutions.put("BidirectionalBFS", new BidirectionalBFSSolution());

        // Running totals used to build the summary table at the end.
        Map<String, Long> totalNanosBySolution = new LinkedHashMap<>();
        Map<String, Integer> passCountBySolution = new LinkedHashMap<>();
        for (String solutionName : solutions.keySet()) {
            totalNanosBySolution.put(solutionName, 0L);
            passCountBySolution.put(solutionName, 0);
        }

        System.out.println("=".repeat(100));
        System.out.printf("%-10s %-9s | %-38s %10s %8s%n", "Test", "Expected", "Solution", "Result", "Time(ms)");
        System.out.println("=".repeat(100));

        for (TestCase testCase : testCases) {
            boolean first = true; // only print the test name/expected value once per test case
            for (Map.Entry<String, WordLadderSolver> entry : solutions.entrySet()) {
                String solutionName = entry.getKey();
                WordLadderSolver solver = entry.getValue();

                // Defensive copy: keeps test cases independent if a solution ever mutates its input.
                List<String> wordListCopy = new ArrayList<>(testCase.wordList);

                long start = System.nanoTime();
                int actual = solver.ladderLength(testCase.beginWord, testCase.endWord, wordListCopy);
                long elapsedNanos = System.nanoTime() - start;

                totalNanosBySolution.merge(solutionName, elapsedNanos, Long::sum);
                boolean pass = actual == testCase.expected;
                if (pass) {
                    passCountBySolution.merge(solutionName, 1, Integer::sum);
                }

                double elapsedMs = elapsedNanos / 1_000_000.0;
                System.out.printf("%-10s %-9s | %-38s %10s %8.3f  %s%n",
                        first ? testCase.name : "",
                        first ? String.valueOf(testCase.expected) : "",
                        solutionName, actual, elapsedMs, pass ? "PASS" : "FAIL");
                first = false;
            }
            System.out.println("-".repeat(100));
        }

        // Final summary: pass rate and timing totals/averages per solution.
        int totalTests = testCases.size();
        System.out.println();
        System.out.println("SUMMARY");
        System.out.println("=".repeat(100));
        System.out.printf("%-20s %10s %15s %20s%n", "Solution", "Pass/Total", "Total Time(ms)", "Avg Time(ms)/test");
        for (String solutionName : solutions.keySet()) {
            int passed = passCountBySolution.get(solutionName);
            double totalMs = totalNanosBySolution.get(solutionName) / 1_000_000.0;
            double avgMs = totalMs / totalTests;
            System.out.printf("%-20s %6d/%-4d %15.3f %20.3f%n", solutionName, passed, totalTests, totalMs, avgMs);
        }
        System.out.println("=".repeat(100));
    }

    // Loads and sorts every inputN.txt/outputN.txt pair found in `dir` by test number N.
    private static List<TestCase> loadTestCases(File dir) throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        if (!dir.isDirectory()) {
            return testCases;
        }

        File[] inputFiles = dir.listFiles((d, name) -> name.matches("input\\d+\\.txt"));
        if (inputFiles == null) {
            return testCases;
        }
        // Sort numerically (input2 before input10) rather than lexicographically.
        java.util.Arrays.sort(inputFiles, (a, b) -> Integer.compare(extractNumber(a.getName()), extractNumber(b.getName())));

        for (File inputFile : inputFiles) {
            int number = extractNumber(inputFile.getName());
            File outputFile = new File(dir, "output" + number + ".txt");
            if (!outputFile.exists()) {
                System.out.println("Skipping input" + number + ".txt: no matching output" + number + ".txt found.");
                continue;
            }
            testCases.add(parseTestCase("test" + number, inputFile, outputFile));
        }
        return testCases;
    }

    // Pulls the numeric portion out of a filename like "input10.txt" -> 10.
    private static int extractNumber(String fileName) {
        String digits = fileName.replaceAll("\\D+", "");
        return Integer.parseInt(digits);
    }

    // Parses one inputN.txt/outputN.txt pair into a TestCase.
    private static TestCase parseTestCase(String name, File inputFile, File outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            // Line 1: "beginWord endWord"
            String[] firstLine = reader.readLine().trim().split("\\s+");
            String beginWord = firstLine[0];
            String endWord = firstLine[1];

            // Line 2: N, followed by N lines of wordList entries.
            int n = Integer.parseInt(reader.readLine().trim());
            List<String> wordList = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                wordList.add(reader.readLine().trim());
            }

            // Output file: single line containing the expected integer result.
            int expected;
            try (BufferedReader outReader = new BufferedReader(new FileReader(outputFile))) {
                expected = Integer.parseInt(outReader.readLine().trim());
            }

            return new TestCase(name, beginWord, endWord, wordList, expected);
        }
    }
}
