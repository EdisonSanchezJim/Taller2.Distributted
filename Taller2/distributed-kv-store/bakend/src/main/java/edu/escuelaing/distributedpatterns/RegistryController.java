package edu.escuelaing.distributedpatterns;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/")
public class RegistryController {

    private final SimpleChat simpleChat;

    public RegistryController(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    // Recibe el nombre desde el cuerpo en formato JSON: {"name":"valor"}
    @PostMapping("/nodes/register")
    public String register(@RequestBody Map<String, String> payload) throws Exception {
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
