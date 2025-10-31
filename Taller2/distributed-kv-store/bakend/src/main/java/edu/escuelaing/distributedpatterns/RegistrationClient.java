package edu.escuelaing.distributedpatterns;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

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
            // Endpoint correcto para registrar nodos
            String registerUrl = loadBalancerUrl + "/api/nodes/register";

            // El backend envía su URL como query param "url"
            System.out.println("🚀 Intentando registrar nodo en: " + registerUrl + "?url=" + backendUrl);

            String response = restTemplate.postForObject(registerUrl + "?url=" + backendUrl, null, String.class);
            System.out.println("🟢 Nodo registrado en el balanceador: " + response);

        } catch (Exception e) {
            System.err.println("❌ Error registrando nodo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void unregister() {
        // Opcional: implementar si quieres eliminar nodos dinámicamente
        System.out.println("🔴 Nodo eliminado: " + backendUrl);
    }
}
