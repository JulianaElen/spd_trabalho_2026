package freeway.common;

import java.io.Serializable;

public class Car implements Serializable {
    private static final long serialVersionUID = 1L;

    public int row;
    public int col;
    public int direction; // +1 = direita, -1 = esquerda
    public int speed;     // move apenas a cada 'speed' ticks (1=mais rápido, 4=mais devagar)

    public Car(int row, int col, int direction, int speed) {
        this.row = row;
        this.col = col;
        this.direction = direction;
        this.speed = speed;
    }
}
