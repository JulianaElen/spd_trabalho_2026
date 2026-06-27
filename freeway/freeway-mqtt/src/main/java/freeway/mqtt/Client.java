package freeway.mqtt;

import com.google.gson.Gson;
import freeway.common.GamePanel;
import freeway.common.GameState;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cliente MQTT — usa broker público HiveMQ.
 * Publicar: freeway/unifei/join e freeway/unifei/action/{playerId}
 * Assinar : freeway/unifei/state e freeway/unifei/join/ack/{clientId}
 */
public class Client implements MqttCallback {

    private final String clientId = "freeway-client-" + UUID.randomUUID().toString().substring(0, 8);
    private final Gson gson = new Gson();
    private MqttClient mqtt;
    private int playerId = -1;
    private GamePanel panel;

    public static void main(String[] args) throws Exception {
        new Client().start();
    }

    public void start() throws Exception {
        mqtt = new MqttClient(MqttTopics.BROKER, clientId, new MemoryPersistence());
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);
        mqtt.setCallback(this);
        mqtt.connect(opts);
        System.out.println("[Cliente MQTT] Conectado. ID=" + clientId);

        // Aguarda o ACK de join
        CountDownLatch joinLatch = new CountDownLatch(1);
        AtomicInteger assignedId = new AtomicInteger(-1);

        mqtt.subscribe(MqttTopics.JOIN_ACK + clientId, 1);
        mqtt.subscribe(MqttTopics.STATE, 0);  // QoS 0 para baixa latência

        // Override do messageArrived para capturar o JOIN_ACK antes de criar o painel
        String ackTopic = MqttTopics.JOIN_ACK + clientId;
        mqtt.setCallback(new MqttCallback() {
            @Override public void messageArrived(String topic, MqttMessage msg) throws Exception {
                if (topic.equals(ackTopic)) {
                    assignedId.set(Integer.parseInt(new String(msg.getPayload())));
                    joinLatch.countDown();
                } // outros mensagens descartadas até ter o ID
            }
            @Override public void connectionLost(Throwable cause) {}
            @Override public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        // Publica pedido de entrada
        mqtt.publish(MqttTopics.JOIN, new MqttMessage(clientId.getBytes()));

        joinLatch.await();
        playerId = assignedId.get();
        System.out.println("[Cliente MQTT] Sou o Jogador " + (playerId + 1));

        // Agora cria o painel e ouve o estado do jogo
        panel = new GamePanel(playerId);
        final int pid = playerId;
        mqtt.setCallback(this);  // restaura o callback principal

        JFrame frame = new JFrame("Freeway - MQTT - Jogador " + (playerId + 1));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String action = switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP    -> "UP";
                    case KeyEvent.VK_DOWN  -> "DOWN";
                    case KeyEvent.VK_LEFT  -> "LEFT";
                    case KeyEvent.VK_RIGHT -> "RIGHT";
                    default -> null;
                };
                if (action != null) {
                    try {
                        mqtt.publish(MqttTopics.ACTION + pid,
                            new MqttMessage(action.getBytes()));
                    } catch (MqttException ex) { ex.printStackTrace(); }
                }
            }
        });

        frame.setVisible(true);
        panel.requestFocusInWindow();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        if (topic.equals(MqttTopics.STATE) && panel != null) {
            GameState state = gson.fromJson(new String(message.getPayload()), GameState.class);
            panel.updateState(state);
        }
    }

    @Override public void connectionLost(Throwable cause) {
        System.out.println("[Cliente MQTT] Conexão perdida: " + cause.getMessage());
    }
    @Override public void deliveryComplete(IMqttDeliveryToken token) {}
}
