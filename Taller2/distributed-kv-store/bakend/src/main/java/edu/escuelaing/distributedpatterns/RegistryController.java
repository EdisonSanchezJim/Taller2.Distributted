package edu.escuelaing.distributedpatterns;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/")
public class RegistryController {

    private final SimpleChat simpleChat;

    public RegistryController(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    // Registrar nombre (sin throws Exception)
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
            return "Registered: " + name;

        } catch (Exception e) {
            System.err.println("❌ Error registrando nombre: " + e.getMessage());
            return "Error registrando nombre";
        }
    }

    // Listar nombres en texto plano vertical legible
    @GetMapping("/names")
    public String getAllPlain() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                                                       .withZone(ZoneId.systemDefault());
        StringBuilder sb = new StringBuilder();
        simpleChat.getMap().forEach((name, ts) -> {
            sb.append(name)
              .append(" -> ")
              .append(formatter.format(Instant.parse(ts)))
              .append("\n");
        });
        return sb.toString();
    }

    // Servir index.html si lo tienes en src/main/resources/static/
    @GetMapping("/")
    public String home() {
        return "index.html";
    }
}
