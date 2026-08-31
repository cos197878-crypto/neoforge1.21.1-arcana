import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 각인 테이블 블록 텍스처 3장(윗면/옆면/아랫면, 16x16)을 생성한다. 빌드에 포함되지 않는다.
 *
 * 실행:  javac BlockTexGen.java && java BlockTexGen ../src/main/resources/assets/arcana/textures/block
 */
public class BlockTexGen {
    private static final int WOOD_DARK = 0xFF3A2C1E;
    private static final int WOOD = 0xFF5C4632;
    private static final int WOOD_LIGHT = 0xFF6E5640;
    private static final int PARCHMENT = 0xFFF0E3C0;
    private static final int PARCHMENT_SHADE = 0xFFDECBA0;
    private static final int RUNE = 0xFF8A3FC0;
    private static final int RUNE_LIGHT = 0xFFD08BFF;

    public static void main(String[] args) throws IOException {
        File dir = new File(args[0]);
        dir.mkdirs();

        write(dir, "inscription_table_top", top());
        write(dir, "inscription_table_side", side());
        write(dir, "inscription_table_bottom", bottom());
        System.out.println("done: 3");
    }

    /** 윗면: 나무 테두리 + 양피지 + 중앙 룬 문양 */
    private static BufferedImage top() {
        BufferedImage img = base(WOOD);
        // 양피지 면
        fill(img, 2, 2, 12, 12, PARCHMENT);
        // 양피지 그늘 (오른쪽/아래 가장자리)
        fill(img, 12, 2, 2, 12, PARCHMENT_SHADE);
        fill(img, 2, 12, 12, 2, PARCHMENT_SHADE);
        // 중앙 룬: 마름모
        int cx = 8, cy = 8;
        for (int d = 0; d < 4; d++) {
            img.setRGB(cx - d, cy, RUNE);
            img.setRGB(cx + d - 1, cy, RUNE);
            img.setRGB(cx, cy - d, RUNE);
            img.setRGB(cx, cy + d - 1, RUNE);
        }
        for (int d = 1; d <= 2; d++) {
            img.setRGB(cx - d, cy - d, RUNE_LIGHT);
            img.setRGB(cx + d - 1, cy - d, RUNE_LIGHT);
            img.setRGB(cx - d, cy + d - 1, RUNE_LIGHT);
            img.setRGB(cx + d - 1, cy + d - 1, RUNE_LIGHT);
        }
        // 테두리 하이라이트
        fill(img, 0, 0, 16, 1, WOOD_LIGHT);
        fill(img, 0, 0, 1, 16, WOOD_LIGHT);
        return img;
    }

    /** 옆면: 세로 판자 + 위쪽 상판 띠 */
    private static BufferedImage side() {
        BufferedImage img = base(WOOD);
        // 판자 이음매 (4픽셀 간격 세로줄)
        for (int x = 3; x < 16; x += 4) {
            fill(img, x, 3, 1, 13, WOOD_DARK);
        }
        // 판자 하이라이트
        for (int x = 0; x < 16; x += 4) {
            fill(img, x, 3, 1, 13, WOOD_LIGHT);
        }
        // 상판 띠
        fill(img, 0, 0, 16, 2, WOOD_LIGHT);
        fill(img, 0, 2, 16, 1, WOOD_DARK);
        return img;
    }

    /** 아랫면: 어두운 판자 */
    private static BufferedImage bottom() {
        BufferedImage img = base(WOOD_DARK);
        for (int y = 3; y < 16; y += 4) {
            fill(img, 0, y, 16, 1, 0xFF2C2116);
        }
        return img;
    }

    private static BufferedImage base(int color) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        fill(img, 0, 0, 16, 16, color);
        return img;
    }

    private static void fill(BufferedImage img, int x, int y, int w, int h, int argb) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                img.setRGB(x + dx, y + dy, argb);
            }
        }
    }

    private static void write(File dir, String name, BufferedImage img) throws IOException {
        File file = new File(dir, name + ".png");
        ImageIO.write(img, "PNG", file);
        System.out.println("  " + file.getName());
    }
}
