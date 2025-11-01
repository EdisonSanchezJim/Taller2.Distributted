package edu.escuelaing.distributedpatterns;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;  // <--- Import necesario

@RestController
@RequestMapping("/")
public class RegistryController {

    private final SimpleChat simpleChat;

    public RegistryController(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    // Registrar nombre
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

    // Listar nombres en texto plano vertical
    @GetMapping("/names")
    public String getAllPlain() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
        StringBuilder sb = new StringBuilder();
        simpleChat.getMap().forEach((name, ts) -> {
            sb.append(name)
              .append(" -> ")
              .append(formatter.format(Instant.parse(ts)))
              .append("\n");
        });
        return sb.toString();
    }
}
