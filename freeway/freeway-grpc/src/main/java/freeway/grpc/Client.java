package freeway.grpc;

import freeway.common.GamePanel;
import freeway.common.GameState;
import freeway.grpc.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Cliente gRPC — conecta ao servidor, recebe stream de estado e envia ações.
 * Uso: java -cp ... freeway.grpc.Client [host] [porta]
 */
public class Client {

    public static void main(String[] args) throws InterruptedException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port    = args.length > 1 ? Integer.parseInt(args[1]) : 5001;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();

        FreewayServiceGrpc.FreewayServiceBlockingStub  blockingStub = FreewayServiceGrpc.newBlockingStub(channel);
        FreewayServiceGrpc.FreewayServiceStub asyncStub   = FreewayServiceGrpc.newStub(channel);

        // Entra no jogo e obtém ID
        JoinResponse joinResp = blockingStub.join(Empty.newBuilder().build());
        int playerId = joinResp.getPlayerId();
        System.out.println("[Cliente gRPC] Sou o Jogador " + (playerId + 1));

        GamePanel panel = new GamePanel(playerId);
        JFrame frame = new JFrame("Freeway - gRPC - Jogador " + (playerId + 1));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        final int pid = playerId;
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
                    PlayerAction pa = PlayerAction.newBuilder()
                        .setPlayerId(pid).setAction(action).build();
                    blockingStub.sendAction(pa);
                }
            }
        });

        frame.setVisible(true);
        panel.requestFocusInWindow();

        // Recebe stream de estado do servidor
        asyncStub.streamState(Empty.newBuilder().build(), new StreamObserver<GameStateProto>() {
            @Override public void onNext(GameStateProto proto) {
                panel.updateState(fromProto(proto));
            }
            @Override public void onError(Throwable t) {
                System.out.println("[Cliente gRPC] Erro no stream: " + t.getMessage());
            }
            @Override public void onCompleted() {
                System.out.println("[Cliente gRPC] Stream encerrado.");
            }
        });

        // Mantém o cliente vivo
        Thread.currentThread().join();
    }

    private static GameState fromProto(GameStateProto proto) {
        GameState s = new GameState();
        s.numPlayers = proto.getNumPlayers();
        for (int r = 0; r < GameState.ROWS; r++)
            for (int c = 0; c < GameState.COLS; c++)
                s.grid[r][c] = proto.getGrid(r * GameState.COLS + c);
        for (int p = 0; p < GameState.MAX_PLAYERS; p++) {
            s.playerRow[p] = proto.getPlayerRow(p);
            s.playerCol[p] = proto.getPlayerCol(p);
            s.scores[p]    = proto.getScores(p);
        }
        return s;
    }
}
