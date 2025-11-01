package edu.escuelaing.distributedpatterns;

import org.jgroups.*;
import org.jgroups.util.Util;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SimpleChat implements Receiver {

    private JChannel channel;
    private final String user_name = System.getProperty("user.name", "n/a");
    private final Map<String, String> stateMap = new ConcurrentHashMap<>();

    // Constructor vacío para Spring Boot
    public SimpleChat() {}

    // Inicializa JGroups
    public void initChannel() throws Exception {
        channel = new JChannel().setReceiver(this);
        channel.connect("ChatCluster");
        channel.getState(null, 10000); // obtiene el estado de otros nodos
    }

    public void put(String key, String value) throws Exception {
        stateMap.put(key, value);
        // Enviar update a otros nodos
        Map<String, String> update = new ConcurrentHashMap<>();
        update.put(key, value);
        Message msg = new ObjectMessage(null, update);
        channel.send(msg);
    }

    public Map<String, String> getMap() {
        return stateMap;
    }

    @Override
    public void receive(Message msg) {
        Object obj = msg.getObject();
        if (obj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, String> update = (Map<String, String>) obj;
            update.forEach((k, v) -> {
                stateMap.put(k, v);
                System.out.println("Updated: " + k + " -> " + v);
            });
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (stateMap) {
            Util.objectToStream(stateMap, new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        Object obj = Util.objectFromStream(new DataInputStream(input));
        if (obj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) obj;
            synchronized (stateMap) {
                stateMap.clear();
                stateMap.putAll(map);
            }
            System.out.println(map.size() + " entries in store:");
            map.forEach((k, v) -> System.out.println(k + " -> " + v));
        }
    }

    @Override
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    // 🔥 NUEVO: Método para obtener los miembros actuales del clúster
    public List<String> getMembers() {
        if (channel == null || channel.getView() == null) {
            return List.of("No members connected");
        }
        return channel.getView().getMembers()
                      .stream()
                      .map(Address::toString)
                      .collect(Collectors.toList());
    }
}
