import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {

    // 1. Ensure this is v1beta
    private static final String API_KEY = "API_key here";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println(" ClipCode AI Started (Gemini 1.5 Flash)");
        System.out.println("Copy a coding question to your clipboard and press ENTER...");

        while (true) {
            scanner.nextLine(); 
            String clipboardText = getClipboardText();

            if (clipboardText == null || clipboardText.trim().length() < 2) {
                System.out.println(" Clipboard empty or too short.");
                continue;
            }

            System.out.println("\n--- Processing Question ---");
            String response = callAI(clipboardText);
            System.out.println("\n--- Generated Code ---\n");
            System.out.println(response);
            System.out.println("\n-----------------------");
            System.out.println("Copy next question and press ENTER...");
        }
    }

    private static String getClipboardText() throws Exception {
        return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    }

    private static String callAI(String question) throws Exception {
        // Use a clearer prompt
        String prompt = "Act as an expert Java developer. Give ONLY the Java code. No explanation. No markdown backticks. Question: " + question;

        String requestBody = "{ \"contents\": [ { \"parts\": [ {\"text\": " + jsonEscape(prompt) + "} ] } ] }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            // This will tell you if the URL was actually v1 or v1beta
            return "Error: API returned status " + response.statusCode() + 
                   "\nCheck if your API Key is valid and the URL is correct." +
                   "\nResponse: " + response.body();
        }

        return extractGeminiText(response.body());
    }

    private static String jsonEscape(String str) {
        if (str == null) return "\"\"";
        return "\"" + str.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t") + "\"";
    }

    private static String extractGeminiText(String body) {
        try {
            // Using a slightly more robust way to grab the text part
            String marker = "\"text\": \"";
            int start = body.indexOf(marker);
            if (start == -1) return "No content found. Raw response: " + body;
            start += marker.length();

            int end = body.lastIndexOf("\"");
            // Basic logic: find the end of the text string before the closing JSON braces
            String result = body.substring(start, body.indexOf("\"", start));

            return result.replace("\\n", "\n")
                         .replace("\\\"", "\"")
                         .replace("\\t", "\t");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}
