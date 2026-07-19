import java.io.*;
import java.util.*;

/**
 * MiniSearchEngine - a keyword based search engine over text files.
 *
 * Core idea: build an "inverted index" - a HashMap that maps each word
 * to the set of files it appears in, along with how many times it
 * appears in that file (term frequency).
 *
 *   index: word -> (fileName -> frequency)
 *
 * Two lookups happen here, both backed by HashMap:
 *   1. word -> which files? (outer map)
 *   2. within those files -> how often? (inner map)
 */
public class MiniSearchEngine {

    // word -> (fileName -> frequency)
    private HashMap<String, HashMap<String, Integer>> index;

    public MiniSearchEngine() {
        index = new HashMap<>();
    }

    /**
     * Reads every .txt file in the given folder and builds the index.
     * Time: O(F) to list files, then O(W) per file where W = words in file.
     * Overall indexing cost is O(T) where T = total words across all files.
     */
    public void buildIndex(String folderPath) throws IOException {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.out.println("No .txt files found in " + folderPath);
            return;
        }

        for (File file : files) {
            indexFile(file);
        }
    }

    /** Reads one file line by line and feeds each word into the index. */
    private void indexFile(File file) throws IOException {
        String fileName = file.getName();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String word : tokenize(line)) {
                    addToIndex(word, fileName);
                }
            }
        }
    }

    /**
     * Lowercases the line, strips punctuation, and splits on whitespace.
     * Kept deliberately simple - no stemming, no stopword removal.
     */
    private String[] tokenize(String line) {
        String cleaned = line.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        return cleaned.trim().split("\\s+");
    }

    /**
     * Inserts one (word, fileName) occurrence into the index.
     * putIfAbsent + getOrDefault avoid null checks and keep this O(1) average.
     */
    private void addToIndex(String word, String fileName) {
        if (word.isEmpty()) return;

        index.putIfAbsent(word, new HashMap<>());
        HashMap<String, Integer> fileFrequencies = index.get(word);
        fileFrequencies.put(fileName, fileFrequencies.getOrDefault(fileName, 0) + 1);
    }

    /**
     * Search for a single keyword.
     * Returns matching files sorted by frequency, highest first.
     * Time: O(1) average to find the word, O(k log k) to sort k matching files.
     */
    public List<Map.Entry<String, Integer>> search(String keyword) {
        HashMap<String, Integer> fileFrequencies = index.get(keyword.toLowerCase());

        if (fileFrequencies == null) {
            return new ArrayList<>();
        }

        List<Map.Entry<String, Integer>> results = new ArrayList<>(fileFrequencies.entrySet());
        results.sort((a, b) -> b.getValue() - a.getValue());
        return results;
    }

    /**
     * Search for files that contain ALL given keywords (AND search).
     * Approach: start with the file set for the first keyword, then
     * intersect with the file set of every other keyword.
     * Time: O(m * k) where m = number of keywords, k = avg files per keyword.
     */
    public Set<String> searchAll(String[] keywords) {
        Set<String> result = null;

        for (String keyword : keywords) {
            HashMap<String, Integer> fileFrequencies = index.get(keyword.toLowerCase());
            Set<String> filesWithWord = (fileFrequencies == null)
                    ? new HashSet<>()
                    : fileFrequencies.keySet();

            if (result == null) {
                result = new HashSet<>(filesWithWord);
            } else {
                result.retainAll(filesWithWord); // set intersection
            }
        }

        return (result == null) ? new HashSet<>() : result;
    }

    public void printIndexStats() {
        System.out.println("Total unique words indexed: " + index.size());
    }

    public static void main(String[] args) throws IOException {
        MiniSearchEngine engine = new MiniSearchEngine();
        String folderPath = "data";

        System.out.println("Building index from folder: " + folderPath);
        engine.buildIndex(folderPath);
        engine.printIndexStats();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Search single keyword");
            System.out.println("2. Search multiple keywords (AND)");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": {
                    System.out.print("Enter keyword: ");
                    String keyword = scanner.nextLine().trim();
                    List<Map.Entry<String, Integer>> results = engine.search(keyword);

                    if (results.isEmpty()) {
                        System.out.println("No matches found.");
                    } else {
                        System.out.println("Results (file : frequency):");
                        for (Map.Entry<String, Integer> entry : results) {
                            System.out.println("  " + entry.getKey() + " : " + entry.getValue());
                        }
                    }
                    break;
                }
                case "2": {
                    System.out.print("Enter keywords separated by space: ");
                    String[] keywords = scanner.nextLine().trim().split("\\s+");
                    Set<String> results = engine.searchAll(keywords);

                    if (results.isEmpty()) {
                        System.out.println("No file contains all keywords.");
                    } else {
                        System.out.println("Files containing all keywords: " + results);
                    }
                    break;
                }
                case "3":
                    System.out.println("Exiting.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
