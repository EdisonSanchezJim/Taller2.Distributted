package edu.escuelaing.distributedpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BackendApplication {

    @Autowired
    private SimpleChat simpleChat;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @PostConstruct
    public void registerAtLoadBalancer() {
        try {
            // Obtener URLs desde las propiedades de inicio de la aplicación
            String backendUrl = System.getProperty("backend.url", "http://localhost:8081");
            String loadBalancerUrl = System.getProperty("loadbalancer.url", "http://localhost:8080");

            String registerUrl = loadBalancerUrl + "/nodes/register?url=" + backendUrl;

            System.out.println("🚀 Intentando registrar en: " + registerUrl);
            new RestTemplate().postForObject(registerUrl, null, String.class);
            System.out.println("🟢 Nodo registrado en el balanceador: " + backendUrl);
        } catch (Exception e) {
            System.err.println("❌ Error registrando en balanceador: " + e.getMessage());
        }
    }
}
