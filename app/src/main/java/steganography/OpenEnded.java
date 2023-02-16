package steganography;

import org.python.util.PythonInterpreter;
import static steganography.Steganography.clearLow;

public class OpenEnded {
    public static void main(String[] args) {
        String code = "print(\"hello world\")";

        Picture beach = new Picture("beach.jpg");
        Picture encoded = encodePicture(beach, code);
        encoded.show();
        
        String decoded = decodePicture(encoded);
        try(PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec(decoded);
        }
    }

    public static Picture encodePicture(Picture source, String code) {
        Picture copy = new Picture(source);

        String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ()*/+_-=.<>\n:\" ";

        for(int i = 0; i < code.length(); i++) {
            char character = code.charAt(i);

            int index = charset.indexOf(character);

            if(index == -1) {
                System.exit(1);
            }


            index += 1;
            
            int b0 = index & 0b11;
            int b1 = (index & (0b11 << 2)) >> 2;
            int b2 = (index & (0b111 << 4)) >> 4;


            Pixel pixel = copy.getPixels()[i];
            clearLow(pixel);
            pixel.setBlue(pixel.getBlue() & ~(0b111));
            pixel.setRed(pixel.getRed() | b0);
            pixel.setGreen(pixel.getGreen() | b1);
            pixel.setBlue(pixel.getBlue() | b2);
        }

        copy.getPixels()[code.length()].setRed(0);
        copy.getPixels()[code.length()].setGreen(0);
        copy.getPixels()[code.length()].setBlue(0);


        return copy;
    }

    public static String decodePicture(Picture source) {
        String ret = "";
        String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ()*/+_-=.<>\n:\" ";

        for (int i = 0; ; i++) {
            Pixel pixel = source.getPixels()[i];

            int b0 = pixel.getRed() & 0b11;
            int b1 = (pixel.getGreen() & 0b11) << 2;
            int b2 = (pixel.getBlue() & 0b111) << 4;

            int index = b0 | b1 | b2;

            if(index == 0) {
                break;
            }


            index -= 1;

            ret += charset.charAt(index);
        }
        return ret;
    }
}
