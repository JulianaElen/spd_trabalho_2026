package freeway.mqtt;

/**
 * Tópicos MQTT usados no jogo.
 *
 *  freeway/state          — servidor publica o estado do jogo (JSON)
 *  freeway/join           — cliente publica para entrar no jogo
 *  freeway/join/ack/{id}  — servidor responde com o playerId atribuído
 *  freeway/action/{id}    — cliente publica ação (UP/DOWN)
 */
public final class MqttTopics {
    public static final String BROKER    = "tcp://broker.hivemq.com:1883";
    public static final String STATE     = "freeway/unifei/state";
    public static final String JOIN      = "freeway/unifei/join";
    public static final String JOIN_ACK  = "freeway/unifei/join/ack/";
    public static final String ACTION    = "freeway/unifei/action/";

    private MqttTopics() {}
}
