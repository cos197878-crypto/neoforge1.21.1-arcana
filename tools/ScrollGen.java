import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 계열별 두루마리 텍스처(16x16)를 생성한다.
 * 손으로 16글자를 세다 틀리지 않도록, 행을 조각으로 조립하고 길이를 검증한다.
 */
public class ScrollGen {

    /** 양피지 안쪽 10칸. p=양피지 w=글씨 c=계열밝음 d=계열어둠 */
    private static final String[] SHEET = {
            "pppppppppp",
            "ppwwwwwwpp",
            "pppccccppp",
            "ppcddddcpp",
            "ppcddddcpp",
            "pppccccppp",
            "ppwwwwwwpp",
            "pppppppppp"
    };

    private static final int OUTLINE   = 0xFF3A2C1E;
    private static final int ROLL_LIT  = 0xFFD9C089;
    private static final int ROLL_DARK = 0xFFB59B68;
    private static final int PARCHMENT = 0xFFF0E3C0;
    private static final int INK       = 0xFF8A7350;

    /** 이름, 밝은색, 어두운색. 계열을 추가하려면 여기에 한 줄 늘린다. */
    private static final String[][] SCHOOLS = {
            {"fire",      "FFFF7A2E", "FFC2320A"},
            {"ice",       "FF7FE3FF", "FF2F87C7"},
            {"lightning", "FFFFE45C", "FFD9A016"},
            {"holy",      "FFC79433", "FFFFF8DC"},
            {"blood",     "FFC22B45", "FF6B0F20"},
            {"evocation", "FFD08BFF", "FF8A3FC0"}
    };

    public static void main(String[] args) throws IOException {
        File outDir = new File(args[0]);
        String[] map = buildMap();

        write(outDir, map, "scroll", PARCHMENT, PARCHMENT);
        for (String[] school : SCHOOLS) {
            write(outDir, map, "scroll_" + school[0],
                    (int) Long.parseLong(school[1], 16),
                    (int) Long.parseLong(school[2], 16));
        }
        System.out.println("done: " + (SCHOOLS.length + 1));
    }

    private static String[] buildMap() {
        String[] map = new String[16];
        map[0] = rep('.', 16);
        map[1] = "." + rep('o', 14) + ".";
        map[2] = ".o" + rep('r', 12) + "o.";
        map[3] = ".o" + rep('s', 12) + "o.";
        for (int i = 0; i < SHEET.length; i++) {
            if (SHEET[i].length() != 10) {
                throw new IllegalStateException("SHEET[" + i + "] len=" + SHEET[i].length());
            }
            map[4 + i] = "..o" + SHEET[i] + "o..";
        }
        map[12] = ".o" + rep('r', 12) + "o.";
        map[13] = ".o" + rep('s', 12) + "o.";
        map[14] = "." + rep('o', 14) + ".";
        map[15] = rep('.', 16);

        for (int y = 0; y < 16; y++) {
            if (map[y].length() != 16) {
                throw new IllegalStateException("row " + y + " len=" + map[y].length());
            }
        }
        return map;
    }

    private static String rep(char c, int n) {
        return String.valueOf(c).repeat(n);
    }

    private static void write(File dir, String[] map, String name, int bright, int dark) throws IOException {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                image.setRGB(x, y, colorFor(map[y].charAt(x), bright, dark));
            }
        }
        File file = new File(dir, name + ".png");
        ImageIO.write(image, "PNG", file);
        System.out.println("  " + file.getName());
    }

    private static int colorFor(char c, int bright, int dark) {
        return switch (c) {
            case 'o' -> OUTLINE;
            case 'r' -> ROLL_LIT;
            case 's' -> ROLL_DARK;
            case 'p' -> PARCHMENT;
            case 'w' -> INK;
            case 'c' -> bright;
            case 'd' -> dark;
            default  -> 0x00000000;
        };
    }
}
