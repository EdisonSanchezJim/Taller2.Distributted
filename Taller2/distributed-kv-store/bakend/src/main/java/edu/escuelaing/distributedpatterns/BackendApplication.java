package edu.escuelaing.distributedpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @PostConstruct
    public void registerAtLoadBalancer() {
        try {
            String port = System.getProperty("server.port", "8081");
            String backendUrl = "http://localhost:" + port;
            String loadBalancerUrl = "http://localhost:8080/nodes/register?url=" + backendUrl;

            System.out.println("🚀 Intentando registrar en: " + loadBalancerUrl);
            new RestTemplate().postForObject(loadBalancerUrl, null, String.class);
            System.out.println("🟢 Registrado en balanceador: " + backendUrl);
        } catch (Exception e) {
            System.err.println("❌ Error registrando en balanceador: " + e.getMessage());
        }
    }
}
