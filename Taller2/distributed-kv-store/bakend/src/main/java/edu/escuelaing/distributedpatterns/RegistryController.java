package edu.escuelaing.distributedpatterns;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
public class RegistryController {

    private final SimpleChat simpleChat;

    public RegistryController(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    // Cambiado para recibir nombre por query param
    @PostMapping("/register")
    public String register(@RequestParam String name) throws Exception {
        String timestamp = Instant.now().toString();
        simpleChat.put(name, timestamp);
        System.out.println("🟢 Nombre registrado en backend: " + name);
        return "Registered: " + name;
    }

    @GetMapping("/names")
    public Map<String, String> getAll() {
        return simpleChat.getMap();
    }
}
