package app.vulnerable.model;

import jakarta.persistence.*;

@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private String apiKey;

    public ApiKey() {
    }

    public ApiKey(String serviceName, String apiKey) {
        this.serviceName = serviceName;
        this.apiKey = apiKey;
    }

    public Long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApiKey() {
        return apiKey;
    }
}
