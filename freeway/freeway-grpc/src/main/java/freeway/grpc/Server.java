package freeway.grpc;

import freeway.common.GameEngine;
import io.grpc.ServerBuilder;

/**
 * Servidor gRPC do jogo Freeway.
 */
public class Server {

    private static final int PORT = 5001;
    private static final int TICK_MS = 150;

    public static void main(String[] args) throws Exception {
        GameEngine engine = new GameEngine();
        FreewayServiceImpl service = new FreewayServiceImpl(engine);

        io.grpc.Server grpcServer = ServerBuilder.forPort(PORT)
            .addService(service)
            .build()
            .start();

        System.out.println("[Servidor gRPC] Porta " + PORT);

        // Loop do jogo
        new Thread(() -> {
            while (true) {
                engine.tick();
                service.broadcastState(engine.getState());
                try { Thread.sleep(TICK_MS); } catch (InterruptedException e) { break; }
            }
        }, "game-loop").start();

        grpcServer.awaitTermination();
    }
}
