package edu.escuelaing.distributedpatterns.loadbalancer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class LoadBalancerController {

    private final List<String> backends = Arrays.asList(
        "http://ec2-54-234-165-68.compute-1.amazonaws.com:8082",
        "http://ec2-35-175-220-60.compute-1.amazonaws.com:8081"
    );


    private final AtomicInteger index = new AtomicInteger(0);

    private String getNextBackend() {
        int i = index.getAndUpdate(n -> (n + 1) % backends.size());
        return backends.get(i);
    }

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
