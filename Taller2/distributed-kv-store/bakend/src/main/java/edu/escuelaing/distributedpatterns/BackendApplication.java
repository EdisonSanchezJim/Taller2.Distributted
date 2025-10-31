package edu.escuelaing.distributedpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import java.util.Map;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @PostConstruct
    public void registerAtLoadBalancer() {
        try {
            // URLs obtenidas desde propiedades o valores por defecto
            String backendUrl = System.getProperty("backend.url", "http://localhost:8081");
            String loadBalancerUrl = System.getProperty("loadbalancer.url", "http://localhost:8080");

            // Endpoint correcto según tu RegistryController
            String registerUrl = loadBalancerUrl + "/nodes/register";

            // Payload JSON esperado por el backend: {"name": "URL_DEL_NODO"}
            Map<String, String> payload = Map.of("name", backendUrl);

            System.out.println("🚀 Intentando registrar en: " + registerUrl);

            // Realiza el POST
            String response = new RestTemplate().postForObject(registerUrl, payload, String.class);
            System.out.println("🟢 Nodo registrado en el balanceador: " + response);
        } catch (Exception e) {
            System.err.println("❌ Error registrando en balanceador: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
