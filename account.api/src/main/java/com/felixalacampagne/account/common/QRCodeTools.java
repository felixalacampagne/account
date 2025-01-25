package com.felixalacampagne.account.common;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import io.nayuki.qrcodegen.QrCode;

public class QRCodeTools
{

   public static BufferedImage createQRImage(String msg, int scale, int border)
   {
      QrCode qr0 = QrCode.encodeText(msg, QrCode.Ecc.MEDIUM);
      BufferedImage img = toImage(qr0, scale, border);  // See QrCodeGeneratorDemo
      return img;
   }

   // NB. outs should be something that can also be read, eg a byte buffer
   // WARNING: outs is NOT closed by this method sinze this might interfere with the reading
   public static byte[] createQRPNG(String msg) throws IOException
   {
      ByteArrayOutputStream byteouts = new ByteArrayOutputStream();
      // TODO: RenderedImage is an awt class - should maybe consider using other ways to generate the image
      RenderedImage img = createQRImage(msg, 12, 0);
      ImageIO.write(img, "png", byteouts);
      byteouts.close();
      return byteouts.toByteArray();
   }
   
   public static void createQRPNG(String msg, String fname) throws IOException
   {

      RenderedImage img = createQRImage(msg, 12, 0);
      ImageIO.write(img, "png", new File(fname));

//    // Manual operation
//    List<QrSegment> segs = QrSegment.makeSegments("3141592653589793238462643383");
//    QrCode qr1 = QrCode.encodeSegments(segs, QrCode.Ecc.HIGH, 5, 5, 2, false);
//    for (int y = 0; y < qr1.size; y++) {
//       for (int x = 0; x < qr1.size; x++) {
//          (... paint qr1.getModule(x, y) ...)
//       }
//    }
   }

   // From https://github.com/nayuki/QR-Code-generator/blob/master/java/QrCodeGeneratorDemo.java?ts=4
   static BufferedImage toImage(QrCode qr, int scale, int border) {
      return toImage(qr, scale, border, 0xFFFFFF, 0x000000);
   }

   static BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
      Objects.requireNonNull(qr);
      if (scale <= 0 || border < 0)
         throw new IllegalArgumentException("Value out of range");
      if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
         throw new IllegalArgumentException("Scale or border too large");

      BufferedImage result = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale, BufferedImage.TYPE_INT_RGB);
      for (int y = 0; y < result.getHeight(); y++) {
         for (int x = 0; x < result.getWidth(); x++) {
            boolean color = qr.getModule(x / scale - border, y / scale - border);
            result.setRGB(x, y, color ? darkColor : lightColor);
         }
      }
      return result;
   }
}
