import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

public class Steganography {
    public static void clearLow(Pixel p) {
        int r = (p.getRed() / 4) * 4;
        int g = (p.getGreen() / 4) * 4;
        int b = (p.getBlue() / 4) * 4;
        p.setColor(new Color(r,g,b));
    }

    public static Picture testClearLow(Picture p) {
        Picture copy = new Picture(p);
        Pixel[][] pixels = copy.getPixels2D();
        for (Pixel[] row : pixels) {
            for (Pixel pixel : row) {
                clearLow(pixel);
            }
        }
        return copy;
    }

    public static void setLow(Pixel p, Color c) {
        int r = (p.getRed() & 0b11111100) | (c.getRed() >> 6);
        int g = (p.getGreen() & 0b11111100) | (c.getGreen() >> 6);
        int b = (p.getBlue() & 0b11111100) | (c.getBlue() >> 6);
        p.setColor(new Color (r,g,b));
    }

    public static Picture testSetLow(Picture p, Color c) {
        Picture copy = new Picture(p);
        Pixel[][] pixels = copy.getPixels2D();
        for (Pixel[] row : pixels) {
            for (Pixel pixel : row) {
                setLow(pixel, c);
            }
        }
        return copy;
    }

    public static Picture revealPicture(Picture hidden) {
        Picture copy = new Picture(hidden);
        Pixel[][] pixels = copy.getPixels2D();
        Pixel[][] source = hidden.getPixels2D();
        for (int r = 0; r < pixels.length; r++){
            for (int c = 0; c < pixels[0].length; c++) {
                Color col = source[r][c].getColor();
                pixels[r][c].setColor(new Color(((pixels[r][c].getRed() & 0b00000011) << 6), ((pixels[r][c].getGreen() & 0b00000011) << 6), ((pixels[r][c].getBlue() & 0b00000011) << 6)));
            }
        }
        return copy;
    }

    public static boolean canHide(Picture source, Picture secret) {
        return source.getWidth() >= secret.getWidth() && source.getHeight() >= secret.getHeight();
    }

    public static Picture hidePicture(Picture source, Picture secret, int startRow, int startCol) {
        Picture combined = new Picture(source);
        Pixel[][] sourcePixels = combined.getPixels2D();
        Pixel[][] secretPixels = secret.getPixels2D();

        for (int r = 0; r<secretPixels.length; r++ ) {
            for (int c = 0; c<secretPixels[0].length; c++) {
                int sourceR = startRow + r;
                int sourceC = startCol + c;

                if (sourceR < sourcePixels.length && sourceC < sourcePixels[0].length) {
                    Color secretColor = secretPixels[r][c].getColor();
                    int rNew = (sourcePixels[sourceR][sourceC].getRed() & 0b11111100) | (secretColor.getRed() >> 6);
                    int gNew = (sourcePixels[sourceR][sourceC].getGreen() & 0b11111100) | (secretColor.getGreen() >> 6);
                    int bNew = (sourcePixels[sourceR][sourceC].getBlue() & 0b11111100) | (secretColor.getBlue() >> 6);

                    sourcePixels[sourceR][sourceC].setColor(new Color(rNew, gNew, bNew));
                }
            }
        }
        return combined;
    }

    public static boolean isSame(Picture p1, Picture p2) {
        if (p1.getWidth() != p2.getWidth() || p1.getHeight() != p2.getHeight()) {
            return false;
        }
        Pixel[][] p1Pixels = p1.getPixels2D();
        Pixel[][] p2Pixels = p2.getPixels2D();
        for (int r = 0; r < p1Pixels.length; r++ ){
            for (int c = 0; c < p1Pixels[0].length; c++) {
                if (!p1Pixels[r][c].getColor().equals(p2Pixels[r][c].getColor())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static ArrayList<Point> findDifferences(Picture p1, Picture p2) {
        ArrayList<Point> diffPoints = new ArrayList<>();
        if (p1.getWidth() != p2.getWidth() || p1.getHeight() != p2.getHeight()) {
            return diffPoints;
        }
        Pixel[][] p1Pixels = p1.getPixels2D();
        Pixel[][] p2Pixels = p2.getPixels2D();
        for (int r = 0; r < p1Pixels.length; r++ ){
            for (int c = 0; c < p1Pixels[0].length; c++) {
                if (!p1Pixels[r][c].getColor().equals(p2Pixels[r][c].getColor())) {
                    diffPoints.add(new Point(r, c));
                }
            }
        }
        return diffPoints;
    }

    public static Picture showDifferentArea(Picture pic, ArrayList<Point> differences) {
        Picture highlighted = new Picture(pic);
        if (differences.isEmpty()) {
            return highlighted;
        }

        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;

        for (Point p : differences) {
            int row = p.y;
            int col = p.x;
            minRow = Math.min(minRow, row);
            maxRow = Math.max(maxRow, row);
            minCol = Math.min(minCol, col);
            maxCol = Math.max(maxCol, col);
        }

        Graphics2D g = highlighted.createGraphics();
        g.setColor(Color.RED);
        g.drawRect(minRow, minCol, maxRow - minRow, maxCol - minCol);
        g.dispose();

        return highlighted;
    }

    public static ArrayList<Integer> encodeString(String s) {
        s = s.toUpperCase();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < s.length(); i++) {
            if (s.substring(i, i + 1).equals(" ")) {
                result.add(27);
            } else {
                result.add(alpha.indexOf(s.substring(i, i + 1)) + 1);
            }
        }
        result.add(0);
        return result;
    }

    public static String decodeString(ArrayList<Integer> codes) {
        String result = "";
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < codes.size(); i++) {
            if (codes.get(i) == 27) {
                result = result + " ";
            } else {
                result = result
                        + alpha.substring(codes.get(i) - 1, codes.get(i));
            }
        }
        return result;
    }

    private static int[] getBitPairs(int num) {
        int[] bits = new int[3];
        int code = num;
        for (int i = 0; i < 3; i++) {
            bits[i] = code % 4;
            code = code / 4;
        }
        return bits;
    }

    public static void hideText(Picture source, String s) {
        ArrayList<Integer> encoded = encodeString(s);
        Pixel[][] pixels = source.getPixels2D();
        int i = 0;
        for (int r = 0; r < pixels.length && i < encoded.size(); r++) {
            for (int c = 0; c < pixels[0].length && i < encoded.size(); c++) {
                int num = encoded.get(i);
                int[] bitPairs = getBitPairs(num);
                Pixel p = pixels[r][c];
                int red = (p.getRed() & 0b11111100) | bitPairs[0];
                int green = (p.getGreen() & 0b11111100) | bitPairs[1];
                int blue = (p.getBlue() & 0b11111100) | bitPairs[2];
                p.setColor(new Color(red, green, blue));
                i++;
            }
        }
    }

    public static String revealText(Picture source) {
        ArrayList<Integer> words = new ArrayList<>();
        Pixel[][] pixels = source.getPixels2D();
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                Pixel p = pixels[r][c];
                int red = p.getRed() & 0b00000011;
                int green = p.getGreen() & 0b00000011;
                int blue = p.getBlue() & 0b00000011;
                int letter = (blue << 4) | (green << 2) | red;
                if (letter == 0) {
                    return decodeString(words);
                }
                words.add(letter);
            }
        }
        return decodeString(words);
    }

    public static void main(String[] args) {
        // ----------------------- ACTIVITY 1 ----------------------- 
        Picture beach = new Picture ("beach.jpg");
        beach.explore();
        Picture copy = testClearLow(beach);
        copy.explore(); 
        Picture beach2 = new Picture ("beach.jpg");
        beach2.explore();
        Picture copy2 = testSetLow(beach2, Color.PINK);
        copy2.explore();
        Picture copy3 = revealPicture(copy2);
        copy3.explore();

        // ----------------------- ACTIVITY 2 ----------------------- 
        Picture arch = new Picture("arch.jpg");

        if (canHide(beach, arch)) {
            Picture hidden = hidePicture(beach, arch, 0, 0);
            hidden.explore();
            Picture revealed = revealPicture(hidden);
            revealed.explore();
        }

        // ----------------------- ACTIVITY 3 ----------------------- 
        Picture robot = new Picture("robot.jpg");
        Picture flower1 = new Picture("flower1.jpg");
        beach.explore();
        // these lines hide 2 pictures
        Picture hidden1 = hidePicture(beach, robot, 65, 208);
        Picture hidden2 = hidePicture(hidden1, flower1, 280, 110);
        hidden2.explore();
        Picture unhidden = revealPicture(hidden2);
        unhidden.explore(); 

        Picture swan = new Picture("swan.jpg");
        Picture swan2 = new Picture("swan.jpg");
        System.out.println("Swan and swan2 are the same: " + isSame(swan, swan2));
        swan = testClearLow(swan);
        System.out.println("Swan and swan2 are the same (after clearLow run on swan): " + isSame(swan, swan2));

        Picture arch1 = new Picture("arch.jpg");
        Picture arch2 = new Picture("arch.jpg");
        Picture koala = new Picture("koala.jpg") ;
        Picture robot1 = new Picture("robot.jpg");
        ArrayList<Point> pointList = findDifferences(arch1, arch2);
        System.out.println("PointList after comparing two identical pictures " + "has a size of " + pointList.size());
        pointList = findDifferences(arch1, koala);
        System.out.println("PointList after comparing two different sized pictures " + "has a size of " + pointList.size());
        arch2 = hidePicture(arch1, robot1, 65, 102);
        pointList = findDifferences(arch1, arch2);
        System.out.println("Pointlist after hiding a picture has a size of " + pointList.size());
        arch1.show();
        arch2.show();

        Picture hall = new Picture("femaleLionAndHall.jpg");
        Picture robot2 = new Picture("robot.jpg");
        Picture flower2 = new Picture("flower1.jpg");
        // hide pictures
        Picture hall2 = hidePicture(hall, robot2, 50, 300);
        Picture hall3 = hidePicture(hall2, flower2, 115, 275);
        hall3.explore();
        if (!isSame(hall, hall3)) {
            Picture hall4 = showDifferentArea(hall, findDifferences(hall, hall3));
            hall4.show();
            Picture unhiddenHall3 = revealPicture(hall3);
            unhiddenHall3.show();
        }

        // ----------------------- ACTIVITY 4 ----------------------- 
        Picture beach1 = new Picture("beach.jpg");
        hideText(beach1, "HELLO WORLD");
        String revealed = revealText(beach1);
        System.out.println("Hidden message: " + revealed);

        // ----------------------- ACTIVITY 4 ----------------------- 
        
    }
}
