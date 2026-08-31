import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 각인 테이블 GUI 배경(inscription_table.png)을 생성한다. 빌드에 포함되지 않는다.
 *
 * blit 의 6인자 버전은 텍스처가 256x256 이라고 가정하므로,
 * 256x256 캔버스의 좌상단에 254x166 패널(본체 176 + 양피지 78)을 그린다.
 *
 * 좌표는 InscriptionTableMenu / InscriptionTableScreen 과 짝이다 — 옮기면 같이 고친다:
 *   책 칸        : 아이템 (18, 22)   -> 베벨 (17, 21)
 *   투입 칸      : 아이템 (18, 58)   -> 베벨 (17, 57)
 *   배출 칸      : 아이템 (213, 138) -> 베벨 (212, 137)
 *   그리드 영역  : (60, 18) ~ (168, 82)  — 칸은 화면이 그린다
 *   양피지 패널  : (176, 0) ~ (254, 166)
 *   인벤토리     : (8 + c*18, 84 + r*18), 핫바 (8 + c*18, 142)
 *
 * 실행:  javac GuiGen.java && java GuiGen ../src/main/resources/assets/arcana/textures/gui
 */
public class GuiGen {
    private static final int MAIN_W = 176;
    private static final int PANEL_H = 166;
    private static final int PARCH_X = 176;
    private static final int PARCH_W = 78;
    private static final int TOTAL_W = MAIN_W + PARCH_W;

    private static final int BODY = 0xFFC6C6C6;
    private static final int BORDER = 0xFF000000;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;
    private static final int SLOT_BG = 0xFF8B8B8B;
    private static final int SLOT_DARK = 0xFF373737;

    private static final int GRID_BG = 0xFF14141E;
    private static final int GRID_BORDER = 0xFF000000;

    private static final int PARCHMENT = 0xFFF0E3C0;
    private static final int PARCHMENT_SHADE = 0xFFDECBA0;
    private static final int PARCH_BORDER = 0xFF3A2C1E;

    private static final int ARROW = 0xFF6B6B6B;

    public static void main(String[] args) throws IOException {
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

        drawMainPanel(img);
        drawParchment(img);

        // 그리드 배경 (칸은 화면이 그린다)
        fill(img, 59, 17, 110, 66, GRID_BORDER);
        fill(img, 60, 18, 108, 64, GRID_BG);

        // 책 칸 / 투입 칸 / 배출 칸
        drawSlot(img, 17, 21);
        drawSlot(img, 17, 57);
        drawSlot(img, 212, 137);

        // 투입 칸 -> 그리드 화살표
        drawArrow(img, 39, 62);

        // 인벤토리 3줄 + 핫바
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(img, 7 + col * 18, 83 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(img, 7 + col * 18, 141);
        }

        File dir = new File(args[0]);
        dir.mkdirs();
        File out = new File(dir, "inscription_table.png");
        ImageIO.write(img, "PNG", out);
        System.out.println("written: " + out.getPath());
    }

    private static void drawMainPanel(BufferedImage img) {
        fill(img, 0, 0, MAIN_W, PANEL_H, BODY);
        fill(img, 1, 1, MAIN_W - 2, 2, LIGHT);
        fill(img, 1, 1, 2, PANEL_H - 2, LIGHT);
        fill(img, 1, PANEL_H - 3, MAIN_W - 2, 2, SHADOW);
        fill(img, MAIN_W - 3, 1, 2, PANEL_H - 2, SHADOW);
        fill(img, 1, 0, MAIN_W - 2, 1, BORDER);
        fill(img, 1, PANEL_H - 1, MAIN_W - 2, 1, BORDER);
        fill(img, 0, 1, 1, PANEL_H - 2, BORDER);
    }

    private static void drawParchment(BufferedImage img) {
        fill(img, PARCH_X, 0, PARCH_W, PANEL_H, PARCHMENT);
        // 가장자리 그늘
        fill(img, PARCH_X, 0, PARCH_W, 3, PARCHMENT_SHADE);
        fill(img, PARCH_X, PANEL_H - 3, PARCH_W, 3, PARCHMENT_SHADE);
        fill(img, PARCH_X + PARCH_W - 3, 0, 3, PANEL_H, PARCHMENT_SHADE);
        // 테두리와 본체 사이 이음매
        fill(img, PARCH_X, 0, 1, PANEL_H, PARCH_BORDER);
        fill(img, PARCH_X + PARCH_W - 1, 0, 1, PANEL_H, PARCH_BORDER);
        fill(img, PARCH_X, 0, PARCH_W, 1, PARCH_BORDER);
        fill(img, PARCH_X, PANEL_H - 1, PARCH_W, 1, PARCH_BORDER);
    }

    /** (x, y)는 18x18 베벨 박스의 좌상단. 아이템이 놓이는 곳은 (x+1, y+1)의 16x16. */
    private static void drawSlot(BufferedImage img, int x, int y) {
        fill(img, x, y, 18, 18, SLOT_BG);
        fill(img, x, y, 17, 1, SLOT_DARK);
        fill(img, x, y, 1, 17, SLOT_DARK);
        fill(img, x + 1, y + 17, 17, 1, LIGHT);
        fill(img, x + 17, y + 1, 1, 17, LIGHT);
    }

    /** 오른쪽을 가리키는 작은 화살표. (x, y)는 몸통 왼쪽 중앙. */
    private static void drawArrow(BufferedImage img, int x, int y) {
        fill(img, x, y - 1, 10, 3, ARROW);
        for (int d = 0; d < 4; d++) {
            fill(img, x + 10 + d, y - 4 + d, 1, 9 - d * 2, ARROW);
        }
    }

    private static void fill(BufferedImage img, int x, int y, int w, int h, int argb) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                img.setRGB(x + dx, y + dy, argb);
            }
        }
    }
}
