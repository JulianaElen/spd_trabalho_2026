package freeway.sockets;

import freeway.common.GameEngine;
import freeway.common.GameState;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Servidor Sockets — aceita até 2 clientes, gerencia o estado do jogo
 * e envia atualizações a todos os clientes a cada 150 ms.
 *
 * Protocolo (texto simples via PrintWriter / BufferedReader):
 *   Servidor → Cliente : "STATE <serialized base64>"  (GameState serializado)
 *   Cliente  → Servidor: "UP" | "DOWN"
 *   Servidor → Cliente : "ID <n>"  (logo após a conexão, informa o playerId)
 */
public class Server {

    private static final int PORT = 5000;
    private static final int TICK_MS = 150;

    private final GameEngine engine = new GameEngine();
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

    public void start() throws IOException {
        System.out.println("[Servidor Sockets] Aguardando conexões na porta " + PORT + "...");
        // Loop do jogo em thread separada
        new Thread(this::gameLoop, "game-loop").start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                int playerId = engine.addPlayer();
                if (playerId == -1) {
                    // Jogo cheio
                    socket.close();
                    continue;
                }
                System.out.println("[Servidor] Jogador " + (playerId + 1) + " conectado.");
                ClientHandler handler = new ClientHandler(socket, playerId, this);
                clients.add(handler);
                new Thread(handler, "client-" + playerId).start();
            }
        }
    }

    private void gameLoop() {
        while (true) {
            engine.tick();
            broadcast(serialize(engine.getState()));
            try { Thread.sleep(TICK_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    void broadcast(String message) {
        for (ClientHandler c : clients) c.send(message);
    }

    void handleAction(int playerId, String action) {
        switch (action) {
            case "UP"    -> engine.moveUp(playerId);
            case "DOWN"  -> engine.moveDown(playerId);
            case "LEFT"  -> engine.moveLeft(playerId);
            case "RIGHT" -> engine.moveRight(playerId);
        }
    }

    void removeClient(ClientHandler handler) {
        clients.remove(handler);
        engine.removePlayer(handler.playerId);
    }

    static String serialize(GameState state) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(state);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static GameState deserialize(String data) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(Base64.getDecoder().decode(data)))) {
            return (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
