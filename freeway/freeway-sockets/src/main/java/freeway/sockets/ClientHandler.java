package freeway.sockets;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    final int playerId;
    private final Socket socket;
    private final Server server;
    private PrintWriter out;

    public ClientHandler(Socket socket, int playerId, Server server) {
        this.socket = socket;
        this.playerId = playerId;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            // Informa o ID ao cliente
            out.println("ID " + playerId);

            String line;
            while ((line = in.readLine()) != null) {
                server.handleAction(playerId, line.trim());
            }
        } catch (IOException e) {
            System.out.println("[Servidor] Jogador " + (playerId + 1) + " desconectado.");
        } finally {
            server.removeClient(this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public void send(String message) {
        if (out != null) out.println("STATE " + message);
    }
}
