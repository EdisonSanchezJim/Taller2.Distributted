package edu.escuelaing.distributedpatterns.loadbalancer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;

@RestController
public class LoadBalancerController {

    // Lista de backends que se puede modificar dinámicamente
    private final CopyOnWriteArrayList<String> backends = new CopyOnWriteArrayList<>();

    private final AtomicInteger index = new AtomicInteger(0);

    private String getNextBackend() {
        if (backends.isEmpty()) {
            throw new IllegalStateException("No backends disponibles");
        }
        int i = index.getAndUpdate(n -> (n + 1) % backends.size());
        return backends.get(i);
    }

    // Endpoint para registrar dinámicamente un backend
    @PostMapping("/nodes/register")
    public String registerBackend(@RequestParam String url) {
        if (!backends.contains(url)) {
            backends.add(url);
            System.out.println("🟢 Nodo registrado: " + url);
            return "Nodo registrado: " + url;
        } else {
            return "Nodo ya registrado: " + url;
        }
    }

    // Endpoint para listar todos los nodos registrados
    @GetMapping("/nodes")
    public List<String> listNodes() {
        return List.copyOf(backends);
    }

    // Endpoint para registrar un nombre usando round-robin a los backends
    @PostMapping("/register")
    public void registerName(@RequestBody Map<String, String> body) {
        String backendUrl = getNextBackend() + "/register";
        System.out.println("➡️ Enviando registro a: " + backendUrl);
        try {
            new RestTemplate().postForObject(backendUrl, body, String.class);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar registro a " + backendUrl + ": " + e.getMessage());
        }
    }

    // Endpoint para listar nombres usando round-robin a los backends
    @GetMapping("/names")
    public ResponseEntity<Map<String, String>> listNames() {
        String backendUrl = getNextBackend() + "/names";
        System.out.println("📥 Consultando nombres en: " + backendUrl);
        try {
            Map<String, String> data = new RestTemplate().getForObject(backendUrl, Map.class);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("❌ Error al consultar " + backendUrl + ": " + e.getMessage());
            return ResponseEntity.internalServerError().body(Collections.emptyMap());
        }
    }
}
