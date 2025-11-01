package edu.escuelaing.distributedpatterns;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

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

    // Recibe el nombre desde el cuerpo en formato JSON: {"name":"valor"}
    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null || name.isBlank()) {
            return "Error: name cannot be empty";
        }
    
        String timestamp = Instant.now().toString();
        simpleChat.put(name, timestamp);
        System.out.println("🟢 Nombre registrado en backend: " + name);
        return "Registered: " + name;
    }


    @GetMapping("/names")
    public Map<String, String> getAll() {
        return simpleChat.getMap();
    }

    // Opcional: sirve index.html si quieres abrirlo desde el navegador
    @GetMapping("/")
    public String home() {
        return "index.html"; // Asegúrate de tener index.html en src/main/resources/static/
    }
}
