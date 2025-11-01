package edu.escuelaing.distributedpatterns;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/")
public class RegistryController {

    private final SimpleChat simpleChat;
    private final RegistrationClient registrationClient;

    @Autowired
    public RegistryController(SimpleChat simpleChat, RegistrationClient registrationClient) {
        this.simpleChat = simpleChat;
        this.registrationClient = registrationClient;
    }

    // Registrar nombre y timestamp
    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null || name.isBlank()) {
            return "Error: name cannot be empty";
        }

        try {
            String timestamp = Instant.now().toString();
            simpleChat.put(name, timestamp);
            registrationClient.registerName(name);
            System.out.println("🟢 Nombre registrado en backend: " + name);
            return "Registered: " + name;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error registrando nombre: " + e.getMessage();
        }
    }

    // Mostrar nombres en formato vertical
    @GetMapping("/names")
    public String getAll() {
        StringBuilder sb = new StringBuilder("📋 Lista de registros:\n");
        simpleChat.getMap().forEach((k, v) -> sb.append(k).append(" -> ").append(v).append("\n"));
        return sb.toString();
    }
}
