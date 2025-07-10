package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.rmi.Naming;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

/**
 * Web Server that provides HTTP interface to the auction service
 * This acts as a bridge between the web frontend and RMI backend
 */
public class WebServer {
    
    private static final int WEB_PORT = 8080;
    private static final String SERVICE_URL = "rmi://localhost:1099/AuctionService";
    private static final String WEB_ROOT = "/home/scrapybara/auction-platform/web";
    
    private HttpServer server;
    private AuctionService auctionService;
    
    public WebServer() throws Exception {
        // Connect to the auction service
        auctionService = (AuctionService) Naming.lookup(SERVICE_URL);
        
        // Create HTTP server
        server = HttpServer.create(new InetSocketAddress(WEB_PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Set up route handlers
        setupRoutes();
    }
    
    private void setupRoutes() {
        // Static file handler for HTML, CSS, JS files
        server.createContext("/", new StaticFileHandler());
        
        // API endpoints
        server.createContext("/api/auctions", new AuctionHandler());
        server.createContext("/api/bids", new BidHandler());
        server.createContext("/api/users", new UserHandler());
        server.createContext("/api/status", new StatusHandler());
    }
    
    public void start() {
        server.start();
        System.out.println("Web server started on http://localhost:" + WEB_PORT);
    }
    
    public void stop() {
        server.stop(0);
        System.out.println("Web server stopped");
    }
    
    // Static file handler for serving HTML, CSS, JS files
    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            String filePath = WEB_ROOT + path;
            File file = new File(filePath);
            
            if (file.exists() && file.isFile()) {
                String contentType = getContentType(path);
                byte[] content = Files.readAllBytes(Paths.get(filePath));
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);
                
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            return "text/plain";
        }
    }
    
    // Handler for auction-related API calls
    class AuctionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            
            try {
                if ("GET".equals(method)) {
                    // Get all active auctions
                    List<Auction> auctions = auctionService.getAllActiveAuctions();
                    response = convertAuctionsToJson(auctions);
                } else if ("POST".equals(method)) {
                    // Create new auction
                    String body = readRequestBody(exchange);
                    Map<String, String> params = parseFormData(body);
                    
                    long auctionId = auctionService.createAuction(
                        params.get("itemName"),
                        params.get("description"),
                        params.get("sellerName"),
                        Double.parseDouble(params.get("startingPrice")),
                        Double.parseDouble(params.get("bidIncrement")),
                        Long.parseLong(params.get("duration"))
                    );
                    
                    response = "{\"success\": true, \"auctionId\": " + auctionId + "}";
                }
                
                sendJsonResponse(exchange, response);
                
            } catch (Exception e) {
                String errorResponse = "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, errorResponse);
            }
        }
    }
    
    // Handler for bid-related API calls
    class BidHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            
            try {
                if ("POST".equals(method)) {
                    // Place a bid
                    String body = readRequestBody(exchange);
                    Map<String, String> params = parseFormData(body);
                    
                    long bidId = auctionService.placeBid(
                        Long.parseLong(params.get("auctionId")),
                        params.get("bidderName"),
                        Double.parseDouble(params.get("bidAmount"))
                    );
                    
                    response = "{\"success\": true, \"bidId\": " + bidId + "}";
                } else if ("GET".equals(method)) {
                    // Get bids for an auction
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = parseQueryString(query);
                    
                    if (params.containsKey("auctionId")) {
                        long auctionId = Long.parseLong(params.get("auctionId"));
                        List<Bid> bids = auctionService.getBidsForAuction(auctionId);
                        response = convertBidsToJson(bids);
                    }
                }
                
                sendJsonResponse(exchange, response);
                
            } catch (Exception e) {
                String errorResponse = "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, errorResponse);
            }
        }
    }
    
    // Handler for user-related API calls
    class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            
            try {
                if ("POST".equals(method)) {
                    // Register user
                    String body = readRequestBody(exchange);
                    Map<String, String> params = parseFormData(body);
                    
                    boolean success = auctionService.registerUser(
                        params.get("username"),
                        params.get("email"),
                        Boolean.parseBoolean(params.get("isSeller"))
                    );
                    
                    response = "{\"success\": " + success + "}";
                }
                
                sendJsonResponse(exchange, response);
                
            } catch (Exception e) {
                String errorResponse = "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, errorResponse);
            }
        }
    }
    
    // Handler for server status
    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String status = auctionService.getServerStatus();
                String response = "{\"status\": \"" + status.replace("\\n", "\\\\n").replace("\"", "\\\\\"") + "\"}";
                sendJsonResponse(exchange, response);
            } catch (Exception e) {
                String errorResponse = "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
                sendJsonResponse(exchange, errorResponse);
            }
        }
    }
    
    // Helper methods
    private String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder body = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }
    
    private Map<String, String> parseFormData(String formData) throws Exception {
        Map<String, String> result = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                result.put(URLDecoder.decode(keyValue[0], "UTF-8"), 
                          URLDecoder.decode(keyValue[1], "UTF-8"));
            }
        }
        return result;
    }
    
    private Map<String, String> parseQueryString(String query) throws Exception {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    result.put(URLDecoder.decode(keyValue[0], "UTF-8"), 
                              URLDecoder.decode(keyValue[1], "UTF-8"));
                }
            }
        }
        return result;
    }
    
    private void sendJsonResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    private String convertAuctionsToJson(List<Auction> auctions) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < auctions.size(); i++) {
            if (i > 0) json.append(",");
            Auction a = auctions.get(i);
            json.append("{")
                .append("\"auctionId\":").append(a.getAuctionId()).append(",")
                .append("\"itemName\":\"").append(escapeJson(a.getItemName())).append("\",")
                .append("\"description\":\"").append(escapeJson(a.getDescription())).append("\",")
                .append("\"sellerName\":\"").append(escapeJson(a.getSellerName())).append("\",")
                .append("\"startingPrice\":").append(a.getStartingPrice()).append(",")
                .append("\"currentHighestBid\":").append(a.getCurrentHighestBid()).append(",")
                .append("\"highestBidder\":\"").append(escapeJson(a.getHighestBidder())).append("\",")
                .append("\"bidIncrement\":").append(a.getBidIncrement()).append(",")
                .append("\"startTime\":\"").append(a.getStartTime()).append("\",")
                .append("\"endTime\":\"").append(a.getEndTime()).append("\",")
                .append("\"isActive\":").append(a.isActive()).append(",")
                .append("\"totalBids\":").append(a.getTotalBids())
                .append("}");
        }
        json.append("]");
        return json.toString();
    }
    
    private String convertBidsToJson(List<Bid> bids) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < bids.size(); i++) {
            if (i > 0) json.append(",");
            Bid b = bids.get(i);
            json.append("{")
                .append("\"bidId\":").append(b.getBidId()).append(",")
                .append("\"auctionId\":").append(b.getAuctionId()).append(",")
                .append("\"bidderName\":\"").append(escapeJson(b.getBidderName())).append("\",")
                .append("\"amount\":").append(b.getAmount()).append(",")
                .append("\"timestamp\":\"").append(b.getTimestamp()).append("\"")
                .append("}");
        }
        json.append("]");
        return json.toString();
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\\"", "\\\\\"").replace("\\n", "\\\\n").replace("\\r", "\\\\r");
    }
    
    public static void main(String[] args) {
        try {
            WebServer webServer = new WebServer();
            webServer.start();
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\\nShutting down web server...");
                webServer.stop();
            }));
            
            System.out.println("Web server is running. Press Ctrl+C to stop.");
            
            // Keep the server running
            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }
            
        } catch (Exception e) {
            System.err.println("Web server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}