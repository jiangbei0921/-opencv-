package car.bkrc.com.car2023.Utils.OtherUtil;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.palette.graphics.Palette;


public class TrafficUtil {

    /**
     * 第一步 像素处理背景变为白色，红、绿、蓝、黄、品、青、黑色，白色不变
     *
     * @param bip
     * @return
     */
    public static Bitmap convertToLight(Bitmap bip) {
        int width = bip.getWidth();
        int height = bip.getHeight();
        int[] pixels = new int[width * height];
        bip.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] pl = new int[bip.getWidth() * bip.getHeight()];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                int pixel = pixels[offset + x];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                int bright = (int) (0.4 * r + 0.59 * g + 0.11 * b);
                if (bright < 256 / 2)
                    pl[offset + x] = 0xff000000;
                else
                    pl[offset + x] = pixel;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);//把颜色值重新赋给新建的图片 图片的宽高为以前图片的值
        result.setPixels(pl, 0, width, 0, 0, width, height);
        changeRGB(result);
        return result;
    }


    /**
     * 第二步：识别图片主体颜色，识别逻辑可调整
     *
     * @param newBitmap 传入图片
     */
    private static void changeRGB(Bitmap newBitmap) {
        if (newBitmap != null) {
            Palette.from(newBitmap).generate(palette -> {
//                Palette.Swatch vibrant = palette.getVibrantSwatch();  //获取到充满活力的色调
                    Palette.Swatch vibrant = palette.getLightVibrantSwatch(); //获取充满活力的亮
                if (vibrant == null) {
                    for (Palette.Swatch swatch : palette.getSwatches()) {  // 提取识别到的颜色数据到调色板中
                        vibrant = swatch;
                        break;
                    }
                }
                // 这样获取的颜色可以进行改变。
                assert vibrant != null;
                int rbg = vibrant.getRgb();  // 获取RGB的值
                // 设置按钮背景色为图片主色
                Log.e("This Color ", "" + +Color.red(rbg) + " " + Color.green(rbg) + " " + Color.blue(rbg));
                String colorApproximation = getColorApproximation(Color.red(rbg), Color.green(rbg), Color.blue(rbg));
                System.out.println("颜色趋近：" + colorApproximation);
            });
        }
    }

    public static String trafficResult;
    public static String sort(){
        return trafficResult;

    }

    // 判断颜色趋近值
    public static String getColorApproximation(int red, int green, int blue) {
        double[] lab = RGBtoLab(red, green, blue);

        // 以红色、绿色、黄色的Lab值为参考
        double[] redLab = RGBtoLab(255, 0, 0);
        double[] greenLab = RGBtoLab(0, 255, 0);
        double[] yellowLab = RGBtoLab(255, 255, 0);

        // 计算当前颜色与参考颜色的色差
        double redDiff = deltaE(lab, redLab);
        double greenDiff = deltaE(lab, greenLab);
        double yellowDiff = deltaE(lab, yellowLab);

        // 根据最小色差判断颜色趋近值
        if (redDiff < greenDiff && redDiff < yellowDiff && green < 100) {
            return trafficResult = "交通灯颜色趋近红色\n" + "R:" + red + " G:" + green + " B:" + blue + "\n识别结果仅供参考";
        } else if (greenDiff < redDiff && greenDiff < yellowDiff) {
            return trafficResult = "交通灯颜色趋近绿色\n" + "R:" + red + " G:" + green + " B:" + blue + "\n识别结果仅供参考";
        } else if (yellowDiff < redDiff && yellowDiff < greenDiff) {
            return trafficResult = "交通灯颜色趋近黄色\n" + "R:" + red + " G:" + green + " B:" + blue + "\n识别结果仅供参考";
        } else {
            return trafficResult = "不趋近特定颜色\n" + "R:" + red + " G:" + green + " B:" + blue + "\n识别结果仅供参考";
        }
    }

    // 将RGB颜色转换到Lab颜色空间
    private static double[] RGBtoLab(int red, int green, int blue) {
        double r = red / 255.0;
        double g = green / 255.0;
        double b = blue / 255.0;

        // 利用经验公式将RGB转换到XYZ空间
        double x = r * 0.4124564 + g * 0.3575761 + b * 0.1804375;
        double y = r * 0.2126729 + g * 0.7151522 + b * 0.0721750;
        double z = r * 0.0193339 + g * 0.1191920 + b * 0.9503041;

        // 转换到Lab空间
        double xFrac = x / 0.950456;
        double yFrac = y / 1.000000;
        double zFrac = z / 1.088754;

        double xPow = Math.pow(xFrac, 1.0 / 3.0);
        double yPow = Math.pow(yFrac, 1.0 / 3.0);
        double zPow = Math.pow(zFrac, 1.0 / 3.0);

        double Ll = 116.0 * yPow - 16.0;
        double al = 500.0 * (xPow - yPow);
        double bl = 200.0 * (yPow - zPow);

        return new double[]{Ll, al, bl};
    }

    // 计算CIEDE2000色差
    private static double deltaE(double[] lab1, double[] lab2) {
        double deltaL = lab2[0] - lab1[0];
        double deltaa = lab2[1] - lab1[1];
        double deltab = lab2[2] - lab1[2];

        double c1 = Math.sqrt(lab1[1] * lab1[1] + lab1[2] * lab1[2]);
        double c2 = Math.sqrt(lab2[1] * lab2[1] + lab2[2] * lab2[2]);
        double deltaC = c2 - c1;

        double deltaH = Math.sqrt(deltaa * deltaa + deltab * deltab - deltaC * deltaC);

        double SL = 1.0;
        double KC = 1.0;
        double KH = 1.0;

        double SC = 1.0 + 0.045 * c1;
        double SH = 1.0 + 0.015 * c1;

        double deltaLKlsl = deltaL / (SL * 1.0);
        double deltaCkcsc = deltaC / (KC * SC);
        double deltaHkhsh = deltaH / (KH * SH);

        double deltaE = Math.sqrt(deltaLKlsl * deltaLKlsl + deltaCkcsc * deltaCkcsc + deltaHkhsh * deltaHkhsh);

        return deltaE;
    }
}
