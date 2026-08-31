import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 각인 테이블 GUI 배경(inscription_table.png)을 생성한다. 빌드에 포함되지 않는다.
 *
 * 테마: 어둠(흑자색) 바탕 + 금테. Iron's 의 밝은 양피지와 구분되는 우리 색.
 *
 * blit 의 6인자 버전은 텍스처가 256x256 이라고 가정하므로,
 * 256x256 캔버스의 좌상단에 254x166 패널(본체 176 + 상세 패널 78)을 그린다.
 *
 * 좌표는 InscriptionTableMenu / InscriptionTableScreen 과 짝이다 — 옮기면 같이 고친다:
 *   책 칸        : 아이템 (18, 22)   -> 베벨 (17, 21)
 *   투입 칸      : 아이템 (18, 58)   -> 베벨 (17, 57)
 *   배출 칸      : 아이템 (213, 138) -> 베벨 (212, 137)
 *   그리드 영역  : (60, 18) ~ (168, 82)  — 칸은 화면이 그린다
 *   상세 패널    : (176, 0) ~ (254, 166)
 *   인벤토리     : (8 + c*18, 84 + r*18), 핫바 (8 + c*18, 142)
 *
 * 실행:  javac GuiGen.java && java GuiGen ../src/main/resources/assets/arcana/textures/gui
 */
public class GuiGen {
    private static final int MAIN_W = 176;
    private static final int PANEL_H = 166;
    private static final int PARCH_X = 176;
    private static final int PARCH_W = 78;

    // 어둠 + 금테 팔레트
    private static final int BODY = 0xFF1A1722;
    private static final int BODY_LIGHT = 0xFF2C2738;
    private static final int BODY_SHADOW = 0xFF0C0A10;
    private static final int GOLD = 0xFFC9A227;
    private static final int GOLD_DARK = 0xFF7A611C;

    private static final int SLOT_BG = 0xFF262130;
    private static final int SLOT_DARK = 0xFF100E16;
    private static final int SLOT_LIGHT = 0xFF463D58;

    private static final int GRID_BG = 0xFF0D0B12;

    private static final int SIDE_BG = 0xFF221D2E;
    private static final int SIDE_SHADE = 0xFF191526;

    public static void main(String[] args) throws IOException {
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

        drawMainPanel(img);
        drawSidePanel(img);

        // 그리드 배경 (칸은 화면이 그린다) — 얇은 금테로 두른다
        fill(img, 59, 17, 110, 66, GOLD_DARK);
        fill(img, 60, 18, 108, 64, GRID_BG);

        // 책 칸 / 투입 칸 / 배출 칸
        drawSlot(img, 17, 21);
        drawSlot(img, 17, 57);
        drawSlot(img, 212, 137);

        // 투입 칸 -> 그리드 화살표 (금색)
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

        // 안쪽 베벨: 위/왼쪽 살짝 밝게, 아래/오른쪽 더 어둡게
        fill(img, 2, 2, MAIN_W - 4, 1, BODY_LIGHT);
        fill(img, 2, 2, 1, PANEL_H - 4, BODY_LIGHT);
        fill(img, 2, PANEL_H - 3, MAIN_W - 4, 1, BODY_SHADOW);
        fill(img, MAIN_W - 3, 2, 1, PANEL_H - 4, BODY_SHADOW);

        // 금테 2겹: 바깥 밝은 금 + 안쪽 어두운 금
        outline(img, 0, 0, MAIN_W, PANEL_H, GOLD);
        outline(img, 1, 1, MAIN_W - 2, PANEL_H - 2, GOLD_DARK);
    }

    private static void drawSidePanel(BufferedImage img) {
        fill(img, PARCH_X, 0, PARCH_W, PANEL_H, SIDE_BG);
        // 가장자리 그늘
        fill(img, PARCH_X, PANEL_H - 4, PARCH_W, 4, SIDE_SHADE);
        fill(img, PARCH_X + PARCH_W - 4, 0, 4, PANEL_H, SIDE_SHADE);
        // 금테
        outline(img, PARCH_X, 0, PARCH_W, PANEL_H, GOLD);
        outline(img, PARCH_X + 1, 1, PARCH_W - 2, PANEL_H - 2, GOLD_DARK);
    }

    /** (x, y)는 18x18 베벨 박스의 좌상단. 아이템이 놓이는 곳은 (x+1, y+1)의 16x16. */
    private static void drawSlot(BufferedImage img, int x, int y) {
        fill(img, x, y, 18, 18, SLOT_BG);
        fill(img, x, y, 17, 1, SLOT_DARK);
        fill(img, x, y, 1, 17, SLOT_DARK);
        fill(img, x + 1, y + 17, 17, 1, SLOT_LIGHT);
        fill(img, x + 17, y + 1, 1, 17, SLOT_LIGHT);
    }

    /** 오른쪽을 가리키는 작은 화살표. (x, y)는 몸통 왼쪽 중앙. */
    private static void drawArrow(BufferedImage img, int x, int y) {
        fill(img, x, y - 1, 10, 3, GOLD);
        for (int d = 0; d < 4; d++) {
            fill(img, x + 10 + d, y - 4 + d, 1, 9 - d * 2, GOLD);
        }
    }

    /** 1픽셀 테두리 사각형 */
    private static void outline(BufferedImage img, int x, int y, int w, int h, int argb) {
        fill(img, x, y, w, 1, argb);
        fill(img, x, y + h - 1, w, 1, argb);
        fill(img, x, y, 1, h, argb);
        fill(img, x + w - 1, y, 1, h, argb);
    }

    private static void fill(BufferedImage img, int x, int y, int w, int h, int argb) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                img.setRGB(x + dx, y + dy, argb);
            }
        }
    }
}
