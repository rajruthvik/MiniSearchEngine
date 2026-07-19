import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * SearchApiServer - exposes MiniSearchEngine over plain HTTP.
 *
 * Deliberately uses only com.sun.net.httpserver, which ships with the JDK.
 * No Spring, no external dependencies - keeps the "backend" as simple as
 * the search engine itself, and easy to explain end to end.
 *
 * Endpoints:
 *   GET /search?keyword=hashmap
 *       -> [{"file":"doc1.txt","frequency":2}, ...]
 *
 *   GET /searchAll?keywords=time+complexity
 *       -> ["doc3.txt"]
 *
 *   GET /health
 *       -> "ok"  (used by the frontend to check if the server has woken up)
 */
public class SearchApiServer {

    public static void main(String[] args) throws IOException {
        MiniSearchEngine engine = new MiniSearchEngine();
        engine.buildIndex("data");
        engine.printIndexStats();

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/search", withCors(exchange -> handleSearch(exchange, engine)));
        server.createContext("/searchAll", withCors(exchange -> handleSearchAll(exchange, engine)));
        server.createContext("/health", withCors(exchange -> sendResponse(exchange, 200, "\"ok\"")));

        server.setExecutor(null); // default executor is fine for a project this size
        server.start();

        System.out.println("Listening on port " + port);
    }

    // ---- handlers ----

    private static void handleSearch(HttpExchange exchange, MiniSearchEngine engine) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI());
        String keyword = params.getOrDefault("keyword", "").trim();

        if (keyword.isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"missing keyword parameter\"}");
            return;
        }

        List<Map.Entry<String, Integer>> results = engine.search(keyword);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < results.size(); i++) {
            Map.Entry<String, Integer> entry = results.get(i);
            if (i > 0) json.append(",");
            json.append("{\"file\":\"").append(escape(entry.getKey()))
                .append("\",\"frequency\":").append(entry.getValue()).append("}");
        }
        json.append("]");

        sendResponse(exchange, 200, json.toString());
    }

    private static void handleSearchAll(HttpExchange exchange, MiniSearchEngine engine) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI());
        String keywordsRaw = params.getOrDefault("keywords", "").trim();

        if (keywordsRaw.isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"missing keywords parameter\"}");
            return;
        }

        String[] keywords = keywordsRaw.split("\\s+");
        Set<String> results = engine.searchAll(keywords);

        StringBuilder json = new StringBuilder("[");
        int i = 0;
        for (String file : results) {
            if (i > 0) json.append(",");
            json.append("\"").append(escape(file)).append("\"");
            i++;
        }
        json.append("]");

        sendResponse(exchange, 200, json.toString());
    }

    // ---- helpers ----

    /** Wraps a handler so every response (including OPTIONS preflight) carries CORS headers. */
    private static HttpHandler withCors(HttpHandler inner) {
        return exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            inner.handle(exchange);
        };
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getQuery();
        if (query == null) return params;

        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
