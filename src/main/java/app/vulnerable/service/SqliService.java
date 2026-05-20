package app.vulnerable.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SqliService {

    private final EntityManager entityManager;

    public SqliService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Object[]> vulnerableSearch(String input) {
        String safeInput = input == null ? "" : input;
        String sql = "SELECT * FROM users WHERE username = '" + safeInput + "'";
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    public List<Object[]> secureSearch(String input) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, input == null ? "" : input);
        return query.getResultList();
    }

    public boolean vulnerableBlindContent(String input) {
        String safeInput = input == null ? "" : input;
        String sql = "SELECT * FROM users WHERE username = '" + safeInput + "'";
        Query query = entityManager.createNativeQuery(sql);
        return !query.getResultList().isEmpty();
    }

    public boolean secureBlindContent(String input) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, input == null ? "" : input);
        return !query.getResultList().isEmpty();
    }


    public String vulnerableBlindTime(String input) {
        String safeInput = input == null ? "" : input;
        String sql = "SELECT * FROM users WHERE username = '" + safeInput + "'";
        entityManager.createNativeQuery(sql).getResultList();
        addNoise();
        return sql;
    }

    public String secureBlindTime(String input) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, input == null ? "" : input);
        query.getResultList();
        addNoise();
        return sql + "   -- param: " + (input == null ? "" : input);
    }

    private void addNoise() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        long base = 50 + r.nextInt(350);
        if (r.nextDouble() < 0.05) {
            base += 800 + r.nextInt(700);
        }
        try {
            Thread.sleep(base);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Task 1 — Auth bypass

    public List<Object[]> vulnerableLogin(String username, String password) {
        String u = username == null ? "" : username;
        String p = password == null ? "" : password;
        String sql = "SELECT id, username, role FROM users "
                   + "WHERE username = '" + u + "' AND password = '" + p + "'";
        return entityManager.createNativeQuery(sql).getResultList();
    }

    public List<Object[]> secureLogin(String username, String password) {
        String sql = "SELECT id, username, role FROM users "
                   + "WHERE username = ? AND password = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, username == null ? "" : username);
        query.setParameter(2, password == null ? "" : password);
        return query.getResultList();
    }

    public String buildLoginSql(String username, String password) {
        String u = username == null ? "" : username;
        String p = password == null ? "" : password;
        return "SELECT id, username, role FROM users "
             + "WHERE username = '" + u + "' AND password = '" + p + "'";
    }

    // Task 2 — UNION-based product search

    public List<Object[]> vulnerableProductSearch(String input) {
        String safeInput = input == null ? "" : input;
        String sql = "SELECT name, price FROM products WHERE name ILIKE '%" + safeInput + "%'";
        return entityManager.createNativeQuery(sql).getResultList();
    }

    public List<Object[]> secureProductSearch(String input) {
        String sql = "SELECT name, price FROM products WHERE name ILIKE ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, "%" + (input == null ? "" : input) + "%");
        return query.getResultList();
    }

    public String buildProductSearchSql(String input) {
        String safeInput = input == null ? "" : input;
        return "SELECT name, price FROM products WHERE name ILIKE '%" + safeInput + "%'";
    }

    // Task 3 — String injection

    public List<Object[]> vulnerableTask3(String input) {
        String safeInput = input == null ? "" : input;
        String sql = "SELECT id, name, price FROM products WHERE name ILIKE '" + safeInput + "'";
        return entityManager.createNativeQuery(sql).getResultList();
    }

    public String buildTask3Sql(String input) {
        String safeInput = input == null ? "" : input;
        return "SELECT id, name, price FROM products WHERE name ILIKE '" + safeInput + "'";
    }

    // Task 4 — sqlmap

    public List<Object[]> vulnerableSqlmapTarget(String id) {
        String safeInput = id == null ? "1" : id;
        String sql = "SELECT id, name, price FROM products WHERE id = " + safeInput;
        return entityManager.createNativeQuery(sql).getResultList();
    }

    private static final long MAX_SLEEP_MS = 10_000;

    public static long sleep(double seconds) {
        long ms = clampMs((long) (seconds * 1000));
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ms;
    }

    public static long sleepMs(long ms) {
        long clamped = clampMs(ms);
        try {
            Thread.sleep(clamped);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return clamped;
    }

    private static long clampMs(long ms) {
        if (ms < 0) return 0;
        if (ms > MAX_SLEEP_MS) return MAX_SLEEP_MS;
        return ms;
    }
}