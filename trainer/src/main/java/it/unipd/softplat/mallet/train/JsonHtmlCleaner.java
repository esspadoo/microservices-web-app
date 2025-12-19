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

public class JsonHtmlCleaner {

    public JsonHtmlCleaner(){
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void clean() throws IOException {
        Path inputPath = Paths.get("mallet/src/json_out.json");
        Path outputPath = Paths.get("mallet/src/main/resources/clean_json_out.json");

        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {

            String line;
            while ((line = reader.readLine()) != null) {
                JsonNode root = MAPPER.readTree(line);


                String docId = root.get("url").asText();
                String title = root.get("title").asText();
                String text = root.get("main_content").asText();

                // Convert HTML to plain text
                String cleanText = "";
                try {
                    Document doc =  Jsoup.parse(text);
                    doc.outputSettings()
                            .charset("UTF-8")
                            .escapeMode(Entities.EscapeMode.extended);
                    cleanText = doc.body().text();
                }
                catch (Exception e) {
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
            }
        }
    }
}
