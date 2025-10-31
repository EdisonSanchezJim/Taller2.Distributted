package edu.escuelaing.distributedpatterns.loadbalancer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api") // Todos los endpoints bajo /api
public class LoadBalancerController {

    private final CopyOnWriteArrayList<String> backends = new CopyOnWriteArrayList<>();
    private final AtomicInteger index = new AtomicInteger(0);
    private final RestTemplate restTemplate;

    public LoadBalancerController() {
        // Configurar RestTemplate con timeout opcional
        this.restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
    }

    // Obtiene el siguiente backend usando round-robin
    private String getNextBackend() {
        if (backends.isEmpty()) {
            throw new IllegalStateException("No hay backends disponibles");
        }
        int i = index.getAndUpdate(n -> (n + 1) % backends.size());
        return backends.get(i);
    }

    // Endpoint para registrar dinámicamente un backend
    // POST /api/nodes/register?url=http://localhost:8081
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

    // Listar todos los backends registrados
    @GetMapping("/nodes")
    public List<String> listNodes() {
        return List.copyOf(backends);
    }

    // Endpoint para registrar un nombre en un backend disponible
    // POST /api/register
    // Body JSON: {"name":"Juan"}
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

    // Consultar los nombres desde un backend usando round-robin
    // GET /api/names
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

    // Consultar todos los nombres de todos los backends (para la web)
    // GET /api/all-names
    @GetMapping("/all-names")
    public ResponseEntity<Map<String, String>> listAllNames() {
        Map<String, String> combined = new HashMap<>();
        for (String backend : backends) {
            try {
                Map<String, String> data = restTemplate.getForObject(backend + "/names", Map.class);
                if (data != null) combined.putAll(data);
            } catch (Exception e) {
                System.err.println("❌ Error consultando " + backend + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok(combined);
    }
}
