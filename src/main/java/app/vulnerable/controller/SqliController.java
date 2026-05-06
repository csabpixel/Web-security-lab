package app.vulnerable.controller;

import app.vulnerable.dto.BlindContentRequest;
import app.vulnerable.dto.BlindContentResponse;
import app.vulnerable.dto.BlindTimeRequest;
import app.vulnerable.dto.BlindTimeResponse;
import app.vulnerable.dto.SearchRequest;
import app.vulnerable.dto.SearchResponse;
import app.vulnerable.service.SqliService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sqli")
@CrossOrigin
public class SqliController {

    private final SqliService service;

    public SqliController(SqliService service) {
        this.service = service;
    }

    @PostMapping("/search")
    public SearchResponse search(@RequestBody SearchRequest request) {
        List<Object[]> rawResults;

        if ("vulnerable".equalsIgnoreCase(request.getMode())) {
            rawResults = service.vulnerableSearch(request.getQuery());
        } else {
            rawResults = service.secureSearch(request.getQuery());
        }

        List<Map<String, Object>> results = new ArrayList<>();

        for (Object[] row : rawResults) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", row[0]);
            user.put("username", row[1]);
            user.put("email", row[2]);
            user.put("role", row[3]);
            results.add(user);
        }

        String explanation = "vulnerable".equalsIgnoreCase(request.getMode())
                ? "Ez a lekérdezés string összefűzéssel épül fel, ezért sérülékeny."
                : "Ez paraméterezett lekérdezés, ezért biztonságos.";

        return new SearchResponse(
                request.getMode(),
                request.getQuery(),
                explanation,
                results
        );
    }

    @PostMapping("/blind/content")
    public BlindContentResponse blindContent(@RequestBody BlindContentRequest request) {
        boolean success;

        if ("vulnerable".equalsIgnoreCase(request.getMode())) {
            success = service.vulnerableBlindContent(request.getInput());
        } else {
            success = service.secureBlindContent(request.getInput());
        }

        String message = success
                ? "Az alkalmazás válasza alapján VAN találat."
                : "Az alkalmazás válasza alapján NINCS találat.";

        return new BlindContentResponse(
                request.getMode(),
                request.getInput(),
                success,
                message
        );
    }

    @PostMapping("/blind/time")
    public BlindTimeResponse blindTime(@RequestBody BlindTimeRequest request) {
        long start = System.currentTimeMillis();

        String constructedSql;
        if ("vulnerable".equalsIgnoreCase(request.getMode())) {
            constructedSql = service.vulnerableBlindTime(request.getInput());
        } else {
            constructedSql = service.secureBlindTime(request.getInput());
        }

        long end = System.currentTimeMillis();
        long responseTime = end - start;

        String message = responseTime >= 3000
                ? "A válasz jelentősen késleltetve érkezett — a payload időalapú injectiont valósított meg."
                : "A válasz normál időn belül érkezett.";

        return new BlindTimeResponse(
                request.getMode(),
                request.getInput(),
                message,
                responseTime,
                constructedSql
        );
    }

    //  Első feladat (SQLI)

    public record LoginRequest(String username, String password) {}

    @PostMapping("/tasks/login")
    public Map<String, Object> taskLogin(@RequestBody LoginRequest request) {
        List<Object[]> rows = service.vulnerableLogin(request.username(), request.password());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("constructedSql", service.buildLoginSql(request.username(), request.password()));

        if (rows.isEmpty()) {
            response.put("success", false);
            response.put("message", "Sikertelen belépés — érvénytelen felhasználónév vagy jelszó.");
        } else {
            Object[] row = rows.get(0);
            Map<String, Object> user = new LinkedHashMap<>();
            user.put("id", row[0]);
            user.put("username", row[1]);
            user.put("role", row[2]);
            response.put("success", true);
            response.put("user", user);
            String role = String.valueOf(row[2]);
            String message = "ADMIN".equalsIgnoreCase(role)
                    ? "🎉 Sikeres belépés ADMIN-ként! A feladat teljesítve."
                    : "Sikeres belépés " + role + " jogosultsággal.";
            response.put("message", message);
        }

        return response;
    }

    //  Második feladat (SQLI)

    public record ProductSearchRequest(String input) {}

    @PostMapping("/tasks/products")
    public Map<String, Object> taskProductSearch(@RequestBody ProductSearchRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("constructedSql", service.buildProductSearchSql(request.input()));

        try {
            List<Object[]> rows = service.vulnerableProductSearch(request.input());

            List<Map<String, Object>> results = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("col1", row.length > 0 ? row[0] : null);
                item.put("col2", row.length > 1 ? row[1] : null);
                results.add(item);
            }

            response.put("count", results.size());
            response.put("results", results);
            response.put("message", results.isEmpty()
                    ? "Nincs találat."
                    : results.size() + " találat.");
        } catch (Exception e) {
            response.put("count", 0);
            response.put("results", new ArrayList<>());
            response.put("error", true);
            response.put("message", "SQL hiba: " + rootMessage(e));
        }
        return response;
    }

    private static String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        String msg = cur.getMessage();
        return msg == null ? cur.getClass().getSimpleName() : msg;
    }
}