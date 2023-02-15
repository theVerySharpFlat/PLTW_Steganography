package steganography;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

public class Steganography {
    public static void main(String[] args) {
        Picture beach = new Picture("beach.jpg");
        Picture robot = new Picture("robot.jpg");

        Picture hidden = hidePicture(beach, robot, 100, 100);
        showDifferentArea(beach, findDifferences(beach, hidden)).show();;

        Picture copy3  = revealPicture(hidden);
        copy3.explore();

        Picture beach2 = new Picture("beach.jpg");
        Picture hiddenTextPicture = hideText(beach2, "Hello World");
        System.out.println("Revealed: \"" + revealText(hiddenTextPicture) + "\"");
    }

    public static boolean isSame(Picture a, Picture b) {
        if(a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }
        
        for(int r = 0; r < a.getHeight(); r++) {
            for(int c = 0; c < a.getWidth(); c++) {
                if(!a.getPixel(c, r).equals(b.getPixel(c, r)))
                    return false;
            }
        }

        return true;
    }

    public static Picture showDifferentArea(Picture picture, ArrayList<Point> pointDifferences) {
        Picture copy = new Picture(picture);
        
        int minX = picture.getWidth();
        int minY = picture.getHeight();
        int maxX = 0;
        int maxY = 0;

        System.out.println("Point differences: " + pointDifferences.size());

        for(Point point : pointDifferences) {
            if(point.x < minX) {
                minX = point.x;
            }

            if(point.x > maxX) {
                maxX = point.x;
            }

            if(point.y < minY) {
                minY = point.y;
            }

            if(point.y > maxY) {
                maxY = point.y;
            }
        }

        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                copy.getPixel(x, y).setColor(new Color(0, 0, 0));
            }
        }

        return copy;
    }

    public static ArrayList<Integer> encodeString(String s) {
        s = s.toUpperCase();
        ArrayList<Integer> result = new ArrayList<>();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYX";
        for(int i = 0; i < s.length(); i++) {
            if(s.substring(i, i + 1).equals(" ")){
                result.add(27);
            } else {
                result.add(alpha.indexOf(s.substring(i, i + 1)) + 1);
                System.out.println(result.get(result.size() - 1));
            }
        }

        result.add(0);
        return result;
    }

    public static String decodeString(ArrayList<Integer> codes) {
        String result = "";
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYX";
        for(int i = 0; i < codes.size(); i++) {
            if(codes.get(i) == 27) {
                result += " ";
            } else {
                result += alpha.substring(codes.get(i) - 1, codes.get(i));
            }
        }

        return result;
    }

    public static int[] getBitPairs(int num) {
        int[] bits = new int[3];
        int code = num;
        for(int i = 0; i < 3; i++) {
            bits[i] = code % 4;
            code = code / 4;
        }

        return bits;
    }

    public static Picture hideText(Picture source, String s) {
        Picture copy = new Picture(source);
        int colIndex = 0;
        for(Integer code : encodeString(s)) {
            int[] bits = getBitPairs(code);

            Pixel p = copy.getPixel(colIndex, 0);
            clearLow(p);

            p.setRed(p.getRed() | bits[0]);
            p.setGreen(p.getGreen() | bits[1]);
            p.setBlue(p.getBlue() | bits[2]);
            System.out.println("bits: " + Integer.toBinaryString(bits[0]) + " " + Integer.toBinaryString(bits[1]) + " " + Integer.toBinaryString(bits[2]));

            colIndex++;
        }

        return copy;
    }

    public static String revealText(Picture source) {
        String result = "";
        int colIndex = 0;
        while(true) {
            Pixel p = source.getPixel(colIndex, 0);

            int b0 = p.getRed() & 0b11;

            int b1 = p.getGreen() & 0b11;
            b1 = b1 << 2;

            int b2 = p.getBlue() & 0b11;
            b2 = b2 << 4;

            int num = b0 | b1 | b2;
            System.out.println("recv bit: " + Integer.toBinaryString(num));

            if(num == 0) {
                break;
            }

            String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYX";
            if(num == 27) {
                result += " ";
            } else {
                result += alpha.charAt(num - 1);
            }

            colIndex++;
        }
        
        return result;
    }

    public static ArrayList<Point> findDifferences(Picture a, Picture b) {
        if(a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            System.out.println("Images are of different sizes!");
            return null;
        }

        ArrayList<Point> differences = new ArrayList<>();
        for(int r = 0; r < a.getHeight(); r++) {
            for(int c = 0; c < a.getWidth(); c++) {
                if(!a.getPixel(c, r).equals(b.getPixel(c, r))) {
                    Point p = new Point();
                    p.x = c;
                    p.y = r;
                    differences.add(p);
                }
            }
        }

        return differences;
    }

    public static boolean canHide(Picture source, Picture secret) {
        return source.getWidth() >= secret.getWidth() && source.getHeight() >= secret.getHeight();
    }

    public static Picture hidePicture(Picture source, Picture secret, int rstart, int cstart) {
        Picture copy = new Picture(source);
        for(int r = rstart; r < rstart + secret.getHeight(); r++) {
            for(int c = cstart; c < cstart + secret.getWidth(); c++) {
                setLow(copy.getPixels2D()[r][c], secret.getPixels2D()[r - rstart][c - cstart].getColor());
            }
        }

        return copy;
    }

    public static Picture revealPicture(Picture hidden) {
        Picture copy = new Picture(hidden);
        Pixel[][] pixels = copy.getPixels2D();
        Pixel[][] source = hidden.getPixels2D();
        for(int r = 0; r < pixels.length; r++) {
            for(int c = 0; c < pixels[0].length; c++) {
                Color col = source[r][c].getColor();
                
                pixels[r][c].setRed(
                    (((col.getRed() & 0b11) << 6)) //& 0xff)
                );

                pixels[r][c].setGreen(
                    (((col.getGreen() & 0b11) << 6))// & 0xff)
                );

                pixels[r][c].setBlue(
                    (((col.getBlue() & 0b11) << 6))// & 0xff)
                );
            }
        }

        return copy;
    }

    public static Picture testClearLow(Picture picture) {
        for(Pixel p : picture.getPixels()) {
            clearLow(p);
        }

        return picture;
    }

    public static Picture testSetLow(Picture picture, Color c) {
        for(Pixel p : picture.getPixels()) {
            setLow(p, c);
        }

        return picture;
    }

    public static void clearLow(Pixel p) {
        p.setRed(p.getRed() & ~(0b11));
        p.setBlue(p.getBlue() & ~(0b11));
        p.setGreen(p.getGreen() & ~(0b11));
    }

    public static void setLow(Pixel P, Color c) {
        clearLow(P);

        //System.out.println("Begin Color: ");
        //System.out.println(Integer.toBinaryString(c.getRed()));
        //System.out.println(Integer.toBinaryString(c.getGreen()));
        //System.out.println(Integer.toBinaryString(c.getBlue()));
        //System.out.println("After Color: ");

        //System.out.println("Red Before: " + Integer.toBinaryString(P.getRed()));
        int bits = c.getRed() & (0b11 << 6); // get the bits
        bits = bits >> 6; // shift them over
        P.setRed(P.getRed() | bits);
        //System.out.println("Red After: " + Integer.toBinaryString(P.getRed()));

        //System.out.println("Green Before: " + Integer.toBinaryString(P.getGreen()));
        bits = c.getGreen() & (0b11 << 6); // get the bits
        bits = bits >> 6; // shift them over
        P.setGreen(P.getGreen() | bits);
        //System.out.println("Green After: " + Integer.toBinaryString(P.getGreen()));

        //System.out.println("Blue Before: " + Integer.toBinaryString(P.getBlue()));
        bits = c.getBlue() & (0b11 << 6); // get the bits
        bits = bits >> 6; // shift them over
        P.setBlue(P.getBlue() | bits);
        //System.out.println("Blue After: " + Integer.toBinaryString(P.getBlue()));
    }
}
