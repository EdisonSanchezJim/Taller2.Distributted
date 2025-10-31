package edu.escuelaing.distributedpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;  // <--- IMPORT CORRECTO
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BackendApplication {

    @Autowired
    private SimpleChat simpleChat;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @PostConstruct
    public void registerAtLoadBalancer() {
        try {
            // Recibe la URL del backend como propiedad al iniciar la app
            String backendUrl = System.getProperty("backend.url", "http://localhost:8081");
            String loadBalancerUrl = "http://ec2-ZZ-ZZ-ZZ-ZZ.compute-1.amazonaws.com:8080/nodes/register?url=" + backendUrl;

            System.out.println("🚀 Intentando registrar en: " + loadBalancerUrl);
            new RestTemplate().postForObject(loadBalancerUrl, null, String.class);
            System.out.println("🟢 Registrado en balanceador: " + backendUrl);
        } catch (Exception e) {
            System.err.println("❌ Error registrando en balanceador: " + e.getMessage());
        }
    }
}
