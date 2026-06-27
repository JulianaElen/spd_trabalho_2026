package freeway.common;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    private final GameState state;
    private final List<Car> cars = new ArrayList<>();
    private int tickCount = 0;

    public GameEngine() {
        state = new GameState();
        initChickens();
    }

    private void initChickens() {
        // Pistas superiores (rows 1..MID_ROW-1): galinhas vão →
        // Pistas inferiores (rows MID_ROW..ROWS-2): galinhas vão ←
        //
        // Apenas 1–2 galinhas por pista com espaçamento maior.
        // Velocidades variadas (2–4 ticks por passo).
        int[] speeds = {2, 3, 4, 2, 3, 4,   // linhas superiores
                        3, 2, 4, 3, 2, 4, 3}; // linhas inferiores

        int[] counts = {2, 1, 2, 1, 2, 1,
                        1, 2, 1, 2, 1, 2, 1};

        int roadW = GameState.ROAD_COL_END - GameState.ROAD_COL_START + 1; // 11

        for (int row = 1; row <= GameState.ROWS - 2; row++) {
            int dir   = (row < GameState.MID_ROW) ? 1 : -1;
            int count = counts[row - 1];
            int spd   = speeds[row - 1];
            int spacing = roadW / (count + 1);  // espaçamento maior

            for (int i = 0; i < count; i++) {
                // posição inicial distribuída, com offset por linha para não alinharem
                int col = GameState.ROAD_COL_START + spacing * (i + 1);
                col = ((col - 1 + row * 2) % roadW) + GameState.ROAD_COL_START;
                cars.add(new Car(row, col, dir, spd));
            }
        }
    }

    public synchronized void tick() {
        tickCount++;

        // Cada galinha só move a cada car.speed ticks
        for (Car c : cars) {
            if (tickCount % c.speed == 0) {
                c.col += c.direction;
                if (c.col >= GameState.COLS) c.col = 0;
                if (c.col < 0)              c.col = GameState.COLS - 1;
            }
        }

        // Reconstrói grid
        for (int r = 0; r < GameState.ROWS; r++)
            for (int c = 0; c < GameState.COLS; c++)
                state.grid[r][c] = 0;
        for (Car c : cars)
            state.grid[c.row][c.col] = c.direction == 1 ? -1 : -2;

        // Colisão carro × galinha
        for (int p = 0; p < state.numPlayers; p++) {
            int r = state.playerRow[p];
            int c = state.playerCol[p];
            if (r >= 1 && r <= GameState.ROWS - 2 && state.grid[r][c] != 0) {
                resetPlayer(p);
            }
        }
    }

    public synchronized void moveUp(int id) {
        if (!validId(id)) return;
        int newRow = state.playerRow[id] - 1;
        if (newRow < 0) { state.scores[id]++; resetPlayer(id); }
        else state.playerRow[id] = newRow;
    }

    public synchronized void moveDown(int id) {
        if (!validId(id)) return;
        int newRow = state.playerRow[id] + 1;
        if (newRow < GameState.ROWS) state.playerRow[id] = newRow;
    }

    public synchronized void moveLeft(int id) {
        if (!validId(id)) return;
        int newCol = state.playerCol[id] - 1;
        if (newCol >= GameState.ROAD_COL_START) state.playerCol[id] = newCol;
    }

    public synchronized void moveRight(int id) {
        if (!validId(id)) return;
        int newCol = state.playerCol[id] + 1;
        if (newCol <= GameState.ROAD_COL_END) state.playerCol[id] = newCol;
    }

    private void resetPlayer(int id) {
        state.playerRow[id] = GameState.ROWS - 1;
        state.playerCol[id] = GameState.COLS / 2;
    }

    public synchronized int addPlayer() {
        if (state.numPlayers >= GameState.MAX_PLAYERS) return -1;
        int id = state.numPlayers++;
        resetPlayer(id);
        return id;
    }

    public synchronized void removePlayer(int id) {
        state.numPlayers = Math.max(0, state.numPlayers - 1);
    }

    private boolean validId(int id) { return id >= 0 && id < state.numPlayers; }

    public synchronized GameState getState() { return state; }
}
