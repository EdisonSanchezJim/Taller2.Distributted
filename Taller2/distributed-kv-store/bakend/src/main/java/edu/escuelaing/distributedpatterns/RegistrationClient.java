package edu.escuelaing.distributedpatterns;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.Map;

@Component
public class RegistrationClient {

    @Value("${loadbalancer.url}")
    private String loadBalancerUrl;

    @Value("${backend.url}")
    private String backendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Lista de otros nodos a replicar
    private List<String> otherNodes;

    @PostConstruct
    public void register() {
        try {
            // 1️⃣ Registrar este backend en el balanceador
            String registerUrl = loadBalancerUrl + "/api/nodes/register";
            System.out.println("🚀 Registrando nodo en: " + registerUrl + "?url=" + backendUrl);

            String response = restTemplate.postForObject(registerUrl + "?url=" + backendUrl, null, String.class);
            System.out.println("🟢 Nodo registrado en el balanceador: " + response);

            // 2️⃣ Obtener lista de nodos para replicación
            String nodesUrl = loadBalancerUrl + "/api/nodes";
            List<String> allNodes = restTemplate.getForObject(nodesUrl, List.class);

            // Excluir este nodo
            otherNodes = allNodes.stream()
                                 .filter(url -> !url.equals(backendUrl))
                                 .toList();

            System.out.println("📋 Nodos disponibles para replicación: " + otherNodes);

        } catch (Exception e) {
            System.err.println("❌ Error registrando nodo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Replicar un nombre a los demás nodos
     * @param payload Map con {"name": "valor"}
     */
    public void replicateToOtherNodes(Map<String, String> payload) {
        if (otherNodes == null || otherNodes.isEmpty()) return;

        for (String node : otherNodes) {
            try {
                restTemplate.postForObject(node + "/register", payload, String.class);
                System.out.println("🔁 Nombre replicado a: " + node);
            } catch (Exception e) {
                System.err.println("❌ Error replicando a " + node + ": " + e.getMessage());
            }
        }
    }

    @PreDestroy
    public void unregister() {
        // Opcional: eliminar este nodo del balanceador
        System.out.println("🔴 Nodo eliminado: " + backendUrl);
    }
}
