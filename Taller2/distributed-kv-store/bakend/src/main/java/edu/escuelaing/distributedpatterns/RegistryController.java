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

    /**
     * Recibe el nombre desde el cuerpo en formato JSON: {"name":"valor"}
     * Registra el nombre con timestamp en el backend
     */
    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> payload) {
        try {
            String name = payload.get("name");
            if (name == null || name.isBlank()) {
                return "Error: name cannot be empty";
            }

            String timestamp = Instant.now().toString();
            simpleChat.put(name, timestamp);
            System.out.println("🟢 Nombre registrado en backend: " + name);

            // Opcional: aquí podrías propagar a otros nodos si deseas replicación entre backends

            return "Registered: " + name;
        } catch (Exception e) {
            System.err.println("❌ Error registrando nombre: " + e.getMessage());
            return "Error registrando nombre";
        }
    }

    /**
     * Devuelve todos los nombres con sus timestamps
     */
    @GetMapping("/names")
    public Map<String, String> getAll() {
        return simpleChat.getMap();
    }

    /**
     * Sirve la página HTML principal si está en resources/static/index.html
     */
    @GetMapping("/")
    public String home() {
        return "index.html";
    }
}
