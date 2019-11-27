package com.baemin.touchorder.printer.util;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * @author seungmin
 */
public class ImageUrlToZplConvertorTest {

    @Test
    public void convert() throws IOException {
        URL url = new URL("https://cf-simple-s3-origin-touch-order-prod-contents-760831942475.s3.ap-northeast-2.amazonaws.com/qrcode/13029682/qr-13029682-0-20190903140444.png");
        BufferedImage originalImage = ImageIO.read(url);

        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        BufferedImage dimg = new BufferedImage(w / 8, h / 8, originalImage.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, w / 8, h / 8, 0, 0, w, h, null);
        g.dispose();

        ImageUrlToZplConverter converter = new ImageUrlToZplConverter();
        converter.setCompressHex(true);
        System.out.println(converter.convertFromImg(dimg, 30, 30));
    }

}
