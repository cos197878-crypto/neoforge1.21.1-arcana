import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 주문서 GUI 배경(spell_book.png)을 생성한다. 빌드에 포함되지 않는다.
 *
 * blit 의 6인자 버전은 텍스처가 256x256 이라고 가정하므로,
 * 256x256 캔버스의 좌상단에 176x166 패널을 그린다.
 *
 * 슬롯 좌표는 SpellBookMenu 와 짝이다 — 옮기면 양쪽을 같이 고친다:
 *   주문 칸 5개  : (44 + i*18, 26)  -> 베벨 박스 (43 + i*18, 25)
 *   인벤토리     : (8 + c*18, 84 + r*18)
 *   핫바         : (8 + c*18, 142)
 *
 * 실행:  javac GuiGen.java && java GuiGen ../src/main/resources/assets/arcana/textures/gui
 */
public class GuiGen {
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 166;

    private static final int BODY = 0xFFC6C6C6;
    private static final int BORDER = 0xFF000000;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;
    private static final int SLOT_BG = 0xFF8B8B8B;
    private static final int SLOT_DARK = 0xFF373737;

    public static void main(String[] args) throws IOException {
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

        drawPanel(img);

        // 주문 칸 5개
        for (int i = 0; i < 5; i++) {
            drawSlot(img, 43 + i * 18, 25);
        }
        // 플레이어 인벤토리 3줄
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(img, 7 + col * 18, 83 + row * 18);
            }
        }
        // 핫바
        for (int col = 0; col < 9; col++) {
            drawSlot(img, 7 + col * 18, 141);
        }

        File dir = new File(args[0]);
        dir.mkdirs();
        File out = new File(dir, "spell_book.png");
        ImageIO.write(img, "PNG", out);
        System.out.println("written: " + out.getPath());
    }

    private static void drawPanel(BufferedImage img) {
        fill(img, 0, 0, PANEL_W, PANEL_H, BODY);

        // 안쪽 베벨: 위/왼쪽 밝게, 아래/오른쪽 어둡게 (2px)
        fill(img, 1, 1, PANEL_W - 2, 2, LIGHT);
        fill(img, 1, 1, 2, PANEL_H - 2, LIGHT);
        fill(img, 1, PANEL_H - 3, PANEL_W - 2, 2, SHADOW);
        fill(img, PANEL_W - 3, 1, 2, PANEL_H - 2, SHADOW);

        // 바깥 테두리 1px. 모서리 픽셀은 비워 바닐라처럼 살짝 둥글게 보이게 한다.
        fill(img, 1, 0, PANEL_W - 2, 1, BORDER);
        fill(img, 1, PANEL_H - 1, PANEL_W - 2, 1, BORDER);
        fill(img, 0, 1, 1, PANEL_H - 2, BORDER);
        fill(img, PANEL_W - 1, 1, 1, PANEL_H - 2, BORDER);
    }

    /** (x, y)는 18x18 베벨 박스의 좌상단. 아이템이 놓이는 곳은 (x+1, y+1)의 16x16. */
    private static void drawSlot(BufferedImage img, int x, int y) {
        fill(img, x, y, 18, 18, SLOT_BG);
        fill(img, x, y, 17, 1, SLOT_DARK);
        fill(img, x, y, 1, 17, SLOT_DARK);
        fill(img, x + 1, y + 17, 17, 1, LIGHT);
        fill(img, x + 17, y + 1, 1, 17, LIGHT);
    }

    private static void fill(BufferedImage img, int x, int y, int w, int h, int argb) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                img.setRGB(x + dx, y + dy, argb);
            }
        }
    }
}
