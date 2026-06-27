package freeway.sockets;

import freeway.common.GamePanel;
import freeway.common.GameState;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;

/**
 * Cliente Sockets — conecta ao servidor, exibe o jogo e envia comandos do teclado.
 * Uso: java -cp ... freeway.sockets.Client [host] [porta]
 */
public class Client {

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port    = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        Socket socket = new Socket(host, port);
        System.out.println("[Cliente Sockets] Conectado ao servidor " + host + ":" + port);

        BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        // Lê o ID do jogador enviado pelo servidor
        String idLine = in.readLine();
        int playerId = Integer.parseInt(idLine.split(" ")[1]);
        System.out.println("[Cliente] Sou o Jogador " + (playerId + 1));

        // Cria janela
        GamePanel panel = new GamePanel(playerId);
        JFrame frame = new JFrame("Freeway - Sockets - Jogador " + (playerId + 1));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // Teclado → envia comandos ao servidor
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP    -> out.println("UP");
                    case KeyEvent.VK_DOWN  -> out.println("DOWN");
                    case KeyEvent.VK_LEFT  -> out.println("LEFT");
                    case KeyEvent.VK_RIGHT -> out.println("RIGHT");
                }
            }
        });

        frame.setVisible(true);
        panel.requestFocusInWindow();

        // Loop de leitura de estado
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("STATE ")) {
                        GameState state = Server.deserialize(line.substring(6));
                        panel.updateState(state);
                    }
                }
            } catch (IOException e) {
                System.out.println("[Cliente] Conexão encerrada.");
            }
        }, "recv").start();
    }
}
