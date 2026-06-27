package freeway.common;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GamePanel extends JPanel {

    private static final int SHEET_COLS = 3;

    // Tiles de estrada
    private BufferedImage road21, road22, road23;   // cantos e meio da estrada
    private BufferedImage road69, road70, road71;   // meta
    private BufferedImage road85;                   // meio da largada
    private BufferedImage grassTile;
    private BufferedImage[] framesRight;             // sprites da galinha →
    private BufferedImage[] carSprites;

    private volatile GameState state = new GameState();
    private final int myPlayerId;
    private int animFrame = 0;

    public GamePanel(int myPlayerId) {
        this.myPlayerId = myPlayerId;
        setPreferredSize(new Dimension(
            GameState.COLS * GameState.CELL_SIZE,
            GameState.ROWS * GameState.CELL_SIZE + 40));
        setBackground(Color.BLACK);
        loadAssets();
        new Timer(160, e -> { animFrame = (animFrame + 1) % SHEET_COLS; repaint(); }).start();
    }

    // -------------------------------------------------------------------------
    private void loadAssets() {
        String b = findAssetsDir();
        road21 = load(b + "/road_asphalt21.png");
        road22 = load(b + "/road_asphalt22.png");
        road23 = load(b + "/road_asphalt23.png");
        road69 = load(b + "/road_asphalt69.png");
        road70 = load(b + "/road_asphalt70.png");
        road71 = load(b + "/road_asphalt71.png");
        road85 = load(b + "/road_asphalt85.png");
        grassTile   = load(b + "/land_grass11.png");
        framesRight = extractFrames(load(b + "/chicken.png"));
        carSprites  = new BufferedImage[]{
            load(b + "/car_yellow_small_1.png"),
            load(b + "/car_blue_small_2.png"),
            load(b + "/car_red_small_3.png"),
            load(b + "/car_green_small_4.png"),
            load(b + "/car_black_small_5.png")
        };
    }

    private BufferedImage[] extractFrames(BufferedImage src) {
        if (src == null) return new BufferedImage[]{null};
        int fw = src.getWidth() / SHEET_COLS;
        int fh = src.getHeight();
        BufferedImage[] f = new BufferedImage[SHEET_COLS];
        for (int c = 0; c < SHEET_COLS; c++)
            f[c] = src.getSubimage(c * fw, 0, fw, fh);
        return f;
    }

    private String findAssetsDir() {
        File f = new File("").getAbsoluteFile();
        while (f != null) {
            for (String rel : new String[]{"assets", "freeway/assets"})
                if (new File(f, rel).isDirectory()) return new File(f, rel).getAbsolutePath();
            f = f.getParentFile();
        }
        return "assets";
    }

    private BufferedImage load(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { System.err.println("Asset não encontrado: " + path); return null; }
    }

    // -------------------------------------------------------------------------
    public void updateState(GameState s) { this.state = s; repaint(); }

    // -------------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        GameState s = this.state;
        int cs = GameState.CELL_SIZE;
        int oy = 40;
        int RS = GameState.ROAD_COL_START;   // 1
        int RE = GameState.ROAD_COL_END;     // 11

        // --- Placar ---
        g2.setColor(new Color(15, 15, 15));
        g2.fillRect(0, 0, getWidth(), 40);
        g2.setFont(new Font("Monospaced", Font.BOLD, 17));
        for (int p = 0; p < GameState.MAX_PLAYERS; p++) {
            g2.setColor(p == myPlayerId ? Color.YELLOW : Color.LIGHT_GRAY);
            g2.drawString("Carro " + (p+1) + ": " + (p < s.numPlayers ? s.scores[p] : 0) + " pts",
                20 + p * 230, 28);
        }

        // --- Fundo célula a célula ---
        for (int r = 0; r < GameState.ROWS; r++) {
            int y = oy + r * cs;

            for (int c = 0; c < GameState.COLS; c++) {
                int x = c * cs;

                // Colunas de grama (bordas esq/dir) — cobrem TODA a coluna
                if (c == 0 || c == GameState.COLS - 1) {
                    drawTile(g2, grassTile, x, y, cs, cs, new Color(34, 130, 34));
                    continue;
                }

                // Dentro da área da estrada: escolhe tile por linha e coluna
                if (r == 0) {
                    // META: 69 | 70 (repetido) | 71
                    if      (c == RS) drawTile(g2, road69, x, y, cs, cs, new Color(70,70,70));
                    else if (c == RE) drawTile(g2, road71, x, y, cs, cs, new Color(70,70,70));
                    else              drawTile(g2, road70, x, y, cs, cs, new Color(80,80,80));

                } else if (r == GameState.ROWS - 1) {
                    // LARGADA: 21 | road85 (meio) | 23
                    if      (c == RS) drawTile(g2, road21, x, y, cs, cs, new Color(70,70,70));
                    else if (c == RE) drawTile(g2, road23, x, y, cs, cs, new Color(70,70,70));
                    else              drawTile(g2, road85, x, y, cs, cs, new Color(60,60,60));

                } else {
                    // PISTAS: cantos 21/23, meio road22
                    if      (c == RS) drawTile(g2, road21, x, y, cs, cs, new Color(70,70,70));
                    else if (c == RE) drawTile(g2, road23, x, y, cs, cs, new Color(70,70,70));
                    else              drawTile(g2, road22, x, y, cs, cs, new Color(60,60,60));
                }
            }
        }

        // --- Linhas verticais amarelas (raias) — apenas na área interna ---
        int roadY1 = oy + cs;                        // abaixo da meta
        int roadY2 = oy + (GameState.ROWS - 1) * cs; // acima da largada
        Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            1, new float[]{10, 8}, 0);
        g2.setColor(new Color(255, 215, 0, 180));
        g2.setStroke(dashed);
        // Linhas internas (entre RS+1 e RE, excluindo as bordas que já têm tile próprio)
        for (int c = RS + 1; c <= RE; c++) {
            int x = c * cs;
            g2.drawLine(x, roadY1, x, roadY2);
        }
        // Linha horizontal divisória de mão dupla
        g2.setColor(new Color(255, 215, 0));
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(RS * cs, oy + GameState.MID_ROW * cs,
                    (RE + 1) * cs, oy + GameState.MID_ROW * cs);
        g2.setStroke(new BasicStroke(1));

        // --- Galinhas ---
        for (int r = 1; r <= GameState.ROWS - 2; r++) {
            for (int c = 0; c < GameState.COLS; c++) {
                int cell = s.grid[r][c];
                if (cell == 0) continue;
                int px = c * cs, py = oy + r * cs;
                boolean goLeft = (cell == -2);
                BufferedImage frame = framesRight[animFrame % framesRight.length];
                drawSprite(g2, frame, px + 2, py + 2, cs - 4, cs - 4, goLeft, Color.WHITE);
            }
        }

        // --- Carros dos jogadores ---
        for (int p = 0; p < s.numPlayers; p++) {
            int px = s.playerCol[p] * cs;
            int py = oy + s.playerRow[p] * cs;
            BufferedImage car = (carSprites != null && p < carSprites.length) ? carSprites[p] : null;
            drawSprite(g2, car, px + 5, py + 2, cs - 10, cs - 4, false,
                p == 0 ? Color.YELLOW : Color.CYAN);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.drawString("P"+(p+1), px + cs/2 - 7, py + cs/2 + 4);
        }
    }

    // -------------------------------------------------------------------------
    private void drawTile(Graphics2D g2, BufferedImage img, int x, int y, int w, int h, Color fallback) {
        if (img == null) { g2.setColor(fallback); g2.fillRect(x, y, w, h); }
        else g2.drawImage(img, x, y, w, h, null);
    }

    private void drawSprite(Graphics2D g2, BufferedImage img,
                            int x, int y, int w, int h, boolean flipH, Color fallback) {
        if (img == null) { g2.setColor(fallback); g2.fillRoundRect(x, y, w, h, 6, 6); return; }
        if (flipH) g2.drawImage(img, x + w, y, -w, h, null);
        else       g2.drawImage(img, x, y, w, h, null);
    }
}
