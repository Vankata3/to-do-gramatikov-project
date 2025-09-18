package com.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodoServer {
    private final TodoBackend backend;
    private final ObjectMapper objectMapper;
    private HttpServer server;

    public TodoServer() {
        this.backend = new TodoBackend();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Serve static files
        server.createContext("/", new StaticFileHandler());
        
        // API endpoints
        server.createContext("/api/tasks", new TasksHandler());
        server.createContext("/api/tasks/add", new AddTaskHandler());
        server.createContext("/api/tasks/update", new UpdateTaskHandler());
        server.createContext("/api/tasks/delete", new DeleteTaskHandler());
        server.createContext("/api/tasks/clear", new ClearCompletedHandler());
        server.createContext("/api/firebase/init", new FirebaseInitHandler());
        server.createContext("/api/status", new StatusHandler());
        
        server.setExecutor(null);
        server.start();
        System.out.println("Todo server started on http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            backend.close();
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/") || path.equals("/index.html")) {
                serveFile(exchange, "index.html", "text/html");
            } else if (path.equals("/style.css")) {
                serveFile(exchange, "style.css", "text/css");
            } else {
                // 404
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }

        private void serveFile(HttpExchange exchange, String filename, String contentType) throws IOException {
            try {
                byte[] bytes = java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(filename)
                );
                String content = new String(bytes, StandardCharsets.UTF_8);
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content.getBytes());
                }
            } catch (IOException e) {
                String response = "Error reading file: " + e.getMessage();
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    private class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String response = backend.getTasks();
            sendResponse(exchange, 200, response);
        }
    }

    private class AddTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);
            
            String title = params.get("title");
            String dueDate = params.get("due");
            
            if (title == null || title.trim().isEmpty()) {
                sendError(exchange, 400, "Title is required");
                return;
            }

            String response = backend.addTask(title, dueDate);
            sendResponse(exchange, 200, response);
        }
    }

    private class UpdateTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);
            
            String taskId = params.get("id");
            String title = params.get("title");
            boolean completed = "true".equals(params.get("completed"));
            String dueDate = params.get("due");
            
            if (taskId == null) {
                sendError(exchange, 400, "Task ID is required");
                return;
            }

            String response = backend.updateTask(taskId, title, completed, dueDate);
            sendResponse(exchange, 200, response);
        }
    }

    private class DeleteTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);
            
            String taskId = params.get("id");
            if (taskId == null) {
                sendError(exchange, 400, "Task ID is required");
                return;
            }

            boolean success = backend.deleteTask(taskId);
            String response = "{\"success\":" + success + "}";
            sendResponse(exchange, 200, response);
        }
    }

    private class ClearCompletedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            int cleared = backend.clearCompletedTasks();
            String response = "{\"cleared\":" + cleared + "}";
            sendResponse(exchange, 200, response);
        }
    }

    private class FirebaseInitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);
            
            String serviceAccountPath = params.get("serviceAccountPath");
            String userId = params.get("userId");
            
            if (serviceAccountPath == null || userId == null) {
                sendError(exchange, 400, "Service account path and user ID are required");
                return;
            }

            boolean success = backend.initializeFirebase(serviceAccountPath, userId);
            String response = "{\"success\":" + success + "}";
            sendResponse(exchange, 200, response);
        }
    }

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String syncStatus = backend.getSyncStatus();
            String response = "{\"syncStatus\":\"" + syncStatus + "\"}";
            sendResponse(exchange, 200, response);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = "{\"error\":\"" + message + "\"}";
        sendResponse(exchange, statusCode, response);
    }

    private Map<String, String> parseFormData(String body) {
        Map<String, String> params = new HashMap<>();
        if (body != null && !body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        params.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name()));
                    } catch (UnsupportedEncodingException e) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }
        return params;
    }

    public static void main(String[] args) {
        try {
            TodoServer server = new TodoServer();
            server.start(8080);
            
            System.out.println("Press Enter to stop the server...");
            System.in.read();
            
            server.stop();
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
