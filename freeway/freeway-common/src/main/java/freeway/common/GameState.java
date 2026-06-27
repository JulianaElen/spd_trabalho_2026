package freeway.common;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // Layout: road runs vertically (top → bottom)
    //   Row  0         : top grass (meta)
    //   Rows 1–6       : pistas superiores — galinhas vão para DIREITA →
    //   Rows 7–13      : pistas inferiores — galinhas vão para ESQUERDA ←
    //   Row  14        : bottom grass (largada)
    //
    //   Col 0          : grama esquerda
    //   Cols 1–11      : estrada (11 pistas verticais)
    //   Col 12         : grama direita
    public static final int ROWS      = 15;
    public static final int COLS      = 13;
    public static final int CELL_SIZE = 55;
    public static final int MAX_PLAYERS = 2;

    public static final int ROAD_COL_START = 1;
    public static final int ROAD_COL_END   = 11;
    public static final int MID_ROW        = 7;  // divisor de mão

    // grid[r][c]: 0=vazio, -1=galinha→direita, -2=galinha←esquerda
    public int[][] grid = new int[ROWS][COLS];

    public int numPlayers = 0;
    public int[] playerRow = new int[MAX_PLAYERS];
    public int[] playerCol = new int[MAX_PLAYERS];
    public int[] scores    = new int[MAX_PLAYERS];

    public GameState() {
        for (int p = 0; p < MAX_PLAYERS; p++) {
            playerRow[p] = ROWS - 1;         // começa na grama inferior
            playerCol[p] = COLS / 2;         // pista central
        }
    }
}
