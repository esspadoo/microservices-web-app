package it.unipd.softplat.mallet.train;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for cleaning JSON documents containing HTML content.
 * <p>
 * This class reads a JSON file where each line represents a document with
 * "url", "title", and "main_content" fields. The main content is cleaned
 * from HTML tags and encoded as plain text. The cleaned documents are then
 * written to a new JSON file.
 * </p>
 *
 * <p>
 * The cleaning process handles multiple character encodings and removes
 * unwanted characters such as quotes and Unicode line separators.
 * </p>
 */
public class JsonHtmlCleaner {

    /**
     * Default constructor.
     */
    public JsonHtmlCleaner(){
    }

    /**
     * Shared ObjectMapper instance for JSON parsing and generation.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Cleans HTML content from JSON documents.
     * <p>
     * Reads the input JSON file line by line, converts HTML in the "main_content"
     * field to plain text using Jsoup, handles different character encodings, and
     * removes unwanted characters. The cleaned documents are saved into a new JSON file.
     * </p>
     *
     * @throws IOException if there is an error reading or writing the files
     */
    public static void clean() throws IOException {
        Path inputPath = Paths.get("trainer/src/json_out.json");
        Path outputPath = Paths.get("trainer/src/main/resources/clean_json_out.json");

        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonNode root = MAPPER.readTree(line);

                    String docId = root.get("url").asText();
                    String title = root.get("title").asText();
                    String text = root.get("main_content").asText();

                    // Convert HTML to plain text
                    String cleanText = "";
                    try {
                        Document doc = Jsoup.parse(text);
                        doc.outputSettings()
                                .charset("UTF-8")
                                .escapeMode(Entities.EscapeMode.extended);
                        cleanText = doc.body().text();
                    } catch (Exception e) {
                        // Try common encodings
                        String[] encodings = {"UTF-8", "ISO-8859-1", "Windows-1252", "UTF-16"};

                        for (String encoding : encodings) {
                            try {
                                Document newDoc = Jsoup.parse(text, encoding);
                                newDoc.outputSettings()
                                        .charset("UTF-8")
                                        .escapeMode(Entities.EscapeMode.extended);
                                cleanText = newDoc.body().text();

                                if (!cleanText.contains("�") && !cleanText.contains("Ã")) {
                                    break;
                                }
                            } catch (Exception ex) {
                                System.out.println("Failed to match with all possible encoding: " + encoding);
                            }
                        }
                    }
                    cleanText = cleanText.replaceAll("[\"“”]", "");
                    cleanText = cleanText.replaceAll("\u2028", "");

                    ObjectNode outputNode = MAPPER.createObjectNode();
                    outputNode.put("url", docId);
                    outputNode.put("title", title);
                    outputNode.put("main_content", cleanText);

                    writer.write(MAPPER.writeValueAsString(outputNode));
                    writer.newLine();
                }catch (Exception e) {
                    System.err.println("Skipping invalid JSONL line:");
                    System.err.println(line);
                    continue;
                }
            }
        }
    }
}
