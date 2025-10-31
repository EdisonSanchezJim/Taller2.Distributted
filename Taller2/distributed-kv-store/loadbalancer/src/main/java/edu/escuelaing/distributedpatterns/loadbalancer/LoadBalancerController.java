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
@RequestMapping("/api") // Opcional: agrupa todos los endpoints bajo /api
public class LoadBalancerController {

    private final CopyOnWriteArrayList<String> backends = new CopyOnWriteArrayList<>();
    private final AtomicInteger index = new AtomicInteger(0);
    private final RestTemplate restTemplate = new RestTemplate();

    private String getNextBackend() {
        if (backends.isEmpty()) {
            throw new IllegalStateException("No hay backends disponibles");
        }
        int i = index.getAndUpdate(n -> (n + 1) % backends.size());
        return backends.get(i);
    }

    // Registrar dinámicamente un backend
    @PostMapping("/nodes/register")
    public String registerBackend(@RequestParam String url) {
        if (url == null || url.isBlank()) return "URL inválida";
        if (!backends.contains(url)) {
            backends.add(url);
            System.out.println("🟢 Nodo registrado: " + url);
            return "Nodo registrado: " + url;
        } else {
            return "Nodo ya registrado: " + url;
        }
    }

    // Listar todos los nodos registrados
    @GetMapping("/nodes")
    public List<String> listNodes() {
        return List.copyOf(backends);
    }

    // Registrar un nombre usando round-robin
    @PostMapping("/register")
    public ResponseEntity<String> registerName(@RequestBody Map<String, String> body) {
        try {
            String backendUrl = getNextBackend() + "/register";
            System.out.println("➡️ Enviando registro a: " + backendUrl);
            restTemplate.postForObject(backendUrl, body, String.class);
            return ResponseEntity.ok("Registro enviado a backend");
        } catch (Exception e) {
            System.err.println("❌ Error al enviar registro: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error al registrar");
        }
    }

    // Listar nombres usando round-robin
    @GetMapping("/names")
    public ResponseEntity<Map<String, String>> listNames() {
        try {
            String backendUrl = getNextBackend() + "/names";
            System.out.println("📥 Consultando nombres en: " + backendUrl);
            Map<String, String> data = restTemplate.getForObject(backendUrl, Map.class);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("❌ Error al consultar nombres: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Collections.emptyMap());
        }
    }
}
