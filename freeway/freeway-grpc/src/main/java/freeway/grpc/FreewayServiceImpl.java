package freeway.grpc;

import freeway.common.GameEngine;
import freeway.common.GameState;
import freeway.grpc.proto.*;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementação do serviço gRPC — roda dentro do servidor.
 */
public class FreewayServiceImpl extends FreewayServiceGrpc.FreewayServiceImplBase {

    private final GameEngine engine;
    private final List<StreamObserver<GameStateProto>> observers = new CopyOnWriteArrayList<>();

    public FreewayServiceImpl(GameEngine engine) {
        this.engine = engine;
    }

    @Override
    public void join(Empty request, StreamObserver<JoinResponse> responseObserver) {
        int id = engine.addPlayer();
        if (id == -1) id = 0; // fallback se cheio
        responseObserver.onNext(JoinResponse.newBuilder().setPlayerId(id).build());
        responseObserver.onCompleted();
    }

    @Override
    public void streamState(Empty request, StreamObserver<GameStateProto> responseObserver) {
        observers.add(responseObserver);
    }

    @Override
    public void sendAction(PlayerAction request, StreamObserver<Empty> responseObserver) {
        String action = request.getAction();
        int pid = request.getPlayerId();
        switch (action) {
            case "UP"    -> engine.moveUp(pid);
            case "DOWN"  -> engine.moveDown(pid);
            case "LEFT"  -> engine.moveLeft(pid);
            case "RIGHT" -> engine.moveRight(pid);
        }
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    /** Chamado pelo loop do jogo a cada tick para enviar o estado a todos os clientes. */
    public void broadcastState(GameState state) {
        GameStateProto proto = toProto(state);
        for (StreamObserver<GameStateProto> obs : observers) {
            try {
                obs.onNext(proto);
            } catch (Exception e) {
                observers.remove(obs);
            }
        }
    }

    private GameStateProto toProto(GameState state) {
        GameStateProto.Builder b = GameStateProto.newBuilder()
            .setRows(GameState.ROWS)
            .setCols(GameState.COLS)
            .setNumPlayers(state.numPlayers);
        for (int r = 0; r < GameState.ROWS; r++)
            for (int c = 0; c < GameState.COLS; c++)
                b.addGrid(state.grid[r][c]);
        for (int p = 0; p < GameState.MAX_PLAYERS; p++) {
            b.addPlayerRow(state.playerRow[p]);
            b.addPlayerCol(state.playerCol[p]);
            b.addScores(state.scores[p]);
        }
        return b.build();
    }
}
