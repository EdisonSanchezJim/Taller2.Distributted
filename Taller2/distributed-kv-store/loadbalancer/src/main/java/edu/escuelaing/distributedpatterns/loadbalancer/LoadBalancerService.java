package edu.escuelaing.distributedpatterns.loadbalancer;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadBalancerService {

    private final CopyOnWriteArrayList<String> backends = new CopyOnWriteArrayList<>();
    private final AtomicInteger index = new AtomicInteger(0);
    private final RestTemplate restTemplate;

    public LoadBalancerService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(2000);
        this.restTemplate = new RestTemplate(factory);
    }

    public List<String> getBackends() {
        return List.copyOf(backends);
    }

    public boolean addBackend(String url) {
        if (url == null || url.isBlank()) return false;
        if (!backends.contains(url)) {
            backends.add(url);
            System.out.println(Instant.now() + " ➕ Backend registrado: " + url);
            return true;
        }
        return false;
    }

    public boolean removeBackend(String url) {
        boolean removed = backends.remove(url);
        if (removed) System.out.println(Instant.now() + " ➖ Backend eliminado: " + url);
        return removed;
    }

    public String nextBackend() {
        int size = backends.size();
        if (size == 0) throw new IllegalStateException("No backends available");
        int i = Math.floorMod(index.getAndUpdate(n -> n + 1), size);
        return backends.get(i);
    }

    public boolean isAlive(String backendUrl) {
        try {
            ResponseEntity<String> r = restTemplate.exchange(backendUrl + "/health", HttpMethod.GET, null, String.class);
            return r.getStatusCode().is2xxSuccessful();
        } catch (Exception ignore) {}

        try {
            ResponseEntity<String> r2 = restTemplate.exchange(backendUrl + "/names", HttpMethod.GET, null, String.class);
            return r2.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Health check failed for " + backendUrl + ": " + e.getMessage());
            return false;
        }
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
