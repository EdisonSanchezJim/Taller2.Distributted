package edu.escuelaing.distributedpatterns;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;

@Component
public class RegistrationClient {

    @Value("${loadbalancer.url}")
    private String loadBalancerUrl;

    @Value("${backend.url}")
    private String backendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void register() {
        try {
            String registerUrl = loadBalancerUrl + "/nodes/register"; // Endpoint corregido
            Map<String, String> payload = Map.of("name", backendUrl); // JSON body esperado
            System.out.println("🚀 Intentando registrar en: " + registerUrl);
            String response = restTemplate.postForObject(registerUrl, payload, String.class);
            System.out.println("🟢 Nodo registrado: " + response);
        } catch (Exception e) {
            System.err.println("❌ Error registrando en balanceador: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void unregister() {
        try {
            // Opcional: si quieres implementar un endpoint de "unregister" en tu backend, aquí va
            System.out.println("🔴 Nodo eliminado: " + backendUrl);
        } catch (Exception e) {
            System.err.println("⚠️ Error al eliminar nodo del balanceador: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
