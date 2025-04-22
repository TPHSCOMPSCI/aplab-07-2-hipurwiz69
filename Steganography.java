import java.awt.Color;
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
                    int gNew = (sourcePixels[sourceR][sourceC].getRed() & 0b11111100) | (secretColor.getRed() >> 6);
                    int bNew = (sourcePixels[sourceR][sourceC].getRed() & 0b11111100) | (secretColor.getRed() >> 6);

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
        System.out.println(canHide(beach, arch));

        if (canHide(beach, arch)) {
            Picture hidden = hidePicture(beach, arch, 0, 0);
            hidden.explore();
            Picture revealed = revealPicture(hidden);
            revealed.explore();
        }
        // ----------------------- ACTIVITY 3 ----------------------- 
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
    }
}
