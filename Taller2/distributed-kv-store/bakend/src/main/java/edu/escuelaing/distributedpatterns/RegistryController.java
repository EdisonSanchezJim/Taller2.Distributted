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

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> req) throws Exception {
        String name = req.get("name");
        String timestamp = Instant.now().toString();
        simpleChat.put(name, timestamp);
        return "Registered: " + name;
    }

    @GetMapping("/names")
    public Map<String, String> getAll() {
        return simpleChat.getMap();
    }
}
