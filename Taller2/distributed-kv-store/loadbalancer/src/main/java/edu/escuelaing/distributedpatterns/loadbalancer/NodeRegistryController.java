package edu.escuelaing.distributedpatterns.loadbalancer;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/nodes")
public class NodeRegistryController {

    private final List<String> nodes = new ArrayList<>();

    @PostMapping("/register")
    public synchronized String registerNode(@RequestParam String url) {
        if (!nodes.contains(url)) {
            nodes.add(url);
            System.out.println("🟢 Nodo registrado: " + url);
        } else {
            System.out.println("ℹ️ Nodo ya registrado: " + url);
        }
        return "OK";
    }

    @GetMapping
    public synchronized List<String> getNodes() {
        return new ArrayList<>(nodes);
    }

    @DeleteMapping("/remove")
    public synchronized void removeNode(@RequestParam String url) {
        nodes.remove(url);
        System.out.println("❌ Nodo eliminado: " + url);
    }
}
