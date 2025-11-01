package edu.escuelaing.distributedpatterns;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RegistrationClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SimpleChat simpleChat;
    private final AtomicInteger index = new AtomicInteger(0);

    public RegistrationClient(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    // Método Round Robin basado en las instancias del clúster
    private String getNextInstance() throws Exception {
        List<String> instances = simpleChat.getMembers();

        if (instances == null || instances.isEmpty()) {
            throw new Exception("No hay instancias activas en el clúster");
        }

        int i = Math.abs(index.getAndIncrement() % instances.size());
        return instances.get(i);
    }

    // Método para registrar el nombre en una instancia
    public void registerName(String name) throws Exception {
        String targetInstance = getNextInstance();
        String url = "http://" + targetInstance + "/register";

        System.out.println("🔁 Enviando solicitud al nodo: " + url);
        restTemplate.postForObject(url, new NameRequest(name), String.class);
    }

    // Clase auxiliar para enviar el JSON {"name": "valor"}
    public static class NameRequest {
        private String name;

        public NameRequest(String name) {
            this.name = name;
        }

        public NameRequest() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
