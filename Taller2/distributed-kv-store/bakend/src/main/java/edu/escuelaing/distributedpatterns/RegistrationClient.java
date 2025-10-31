package edu.escuelaing.distributedpatterns;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class RegistrationClient {

    // URL del Load Balancer (puede ser IP pública o DNS)
    @Value("${loadbalancer.url}")
    private String loadBalancerUrl;

    // URL completa del backend (IP pública + puerto)
    @Value("${backend.url}")
    private String backendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void register() {
        try {
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
            String unregisterUrl = loadBalancerUrl + "/nodes/unregister?url=" + backendUrl;
            restTemplate.delete(unregisterUrl);
            System.out.println("🔴 Nodo eliminado: " + backendUrl);
        } catch (Exception e) {
            System.err.println("⚠️ Error al eliminar nodo del balanceador: " + e.getMessage());
        }
    }
}
