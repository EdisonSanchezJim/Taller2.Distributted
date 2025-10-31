package edu.escuelaing.distributedpatterns;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class RegistrationClient {

    @Value("${server.port}")
    private String port;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String loadBalancerUrl = "http://localhost:8080";

    @PostConstruct
    public void register() {
        try {
            String backendUrl = "http://localhost:" + port;
            String registerUrl = loadBalancerUrl + "/nodes/register?url=" + backendUrl;
            System.out.println("🚀 Intentando registrar en: " + registerUrl);
            restTemplate.postForObject(registerUrl, null, String.class);
            System.out.println("🟢 Nodo registrado: " + backendUrl);
        } catch (Exception e) {
            System.err.println("❌ Error registrando en balanceador: " + e.getMessage());
        }
    }

    @PreDestroy
    public void unregister() {
        try {
            String backendUrl = "http://localhost:" + port;
            String unregisterUrl = loadBalancerUrl + "/nodes/unregister?url=" + backendUrl;
            restTemplate.delete(unregisterUrl);
            System.out.println("🔴 Nodo eliminado: " + backendUrl);
        } catch (Exception e) {
            System.err.println("⚠️ Error al eliminar nodo del balanceador: " + e.getMessage());
        }
    }
}
