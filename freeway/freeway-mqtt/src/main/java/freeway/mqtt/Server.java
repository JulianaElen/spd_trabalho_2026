package freeway.mqtt;

import com.google.gson.Gson;
import freeway.common.GameEngine;
import freeway.common.GameState;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Servidor MQTT — publica o estado do jogo no tópico freeway/unifei/state
 * e reage a pedidos de entrada e ações dos clientes.
 *
 * Broker público: broker.hivemq.com:1883 (sem autenticação)
 */
public class Server implements MqttCallback {

    private final GameEngine engine = new GameEngine();
    private final Gson gson = new Gson();
    private MqttClient mqtt;

    public static void main(String[] args) throws MqttException {
        new Server().start();
    }

    public void start() throws MqttException {
        mqtt = new MqttClient(MqttTopics.BROKER, "freeway-server-" + System.currentTimeMillis(),
            new MemoryPersistence());

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);

        mqtt.setCallback(this);
        mqtt.connect(opts);
        System.out.println("[Servidor MQTT] Conectado ao broker " + MqttTopics.BROKER);

        // Inscreve nos tópicos de controle
        mqtt.subscribe(MqttTopics.JOIN, 1);
        mqtt.subscribe(MqttTopics.ACTION + "#", 1);  // freeway/unifei/action/+

        // Loop do jogo
        while (true) {
            engine.tick();
            publishState();
            try { Thread.sleep(150); } catch (InterruptedException e) { break; }
        }
    }

    private void publishState() throws MqttException {
        String json = gson.toJson(engine.getState());
        mqtt.publish(MqttTopics.STATE, new MqttMessage(json.getBytes()));
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());

        if (topic.equals(MqttTopics.JOIN)) {
            // Atribui um ID ao novo jogador e envia de volta
            String clientId = payload;
            int id = engine.addPlayer();
            if (id == -1) id = 0;
            mqtt.publish(MqttTopics.JOIN_ACK + clientId,
                new MqttMessage(String.valueOf(id).getBytes()));
            System.out.println("[Servidor MQTT] Jogador " + (id + 1) + " entrou (client=" + clientId + ")");

        } else if (topic.startsWith(MqttTopics.ACTION)) {
            // freeway/unifei/action/{playerId}
            String[] parts = topic.split("/");
            int pid = Integer.parseInt(parts[parts.length - 1]);
            switch (payload) {
                case "UP"    -> engine.moveUp(pid);
                case "DOWN"  -> engine.moveDown(pid);
                case "LEFT"  -> engine.moveLeft(pid);
                case "RIGHT" -> engine.moveRight(pid);
            }
        }
    }

    @Override public void connectionLost(Throwable cause) {
        System.out.println("[Servidor MQTT] Conexão perdida: " + cause.getMessage());
    }
    @Override public void deliveryComplete(IMqttDeliveryToken token) {}
}
