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
}