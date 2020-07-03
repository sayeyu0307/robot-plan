package com.lee.robot;

import com.google.common.collect.Lists;
import com.lee.robot.entity.KeyImage;
import com.melloware.jintellitype.JIntellitype;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class AppClient {

    private static List<BufferedImage> keyImages = Lists.newArrayList();           //查找目标图片

    private static volatile boolean flag = true;

    private static int scrShotImgWidth;              //屏幕截图宽度
    private static int scrShotImgHeight;             //屏幕截图高度

    private static int[][] screenShotImageRGBData;   //屏幕截图RGB数据
    private static List<KeyImage> keyImageRGBData = Lists.newArrayList();          //查找目标图片RGB数据


    public static void main(String[] args) throws IOException {
        String path = "C:\\Users\\DELL-LEE\\Desktop\\yys工具\\robot-plan\\src\\main\\resources\\pic";
        for (File file : new File(path).listFiles()) {
            keyImages.add(ImageIO.read(file));
        }
        keyImages.forEach(s -> keyImageRGBData.add(getImageGRB(s)));
        final int EXIT_KEY_MARK = 1;
        JIntellitype.getInstance().registerHotKey(EXIT_KEY_MARK, JIntellitype.MOD_ALT, (int)'Q');
        JIntellitype.getInstance().addHotKeyListener(hotKey -> {if(hotKey == EXIT_KEY_MARK)flag=false;});
        while(flag){
            keyImageRGBData.forEach(AppClient::findImage);
        }
        JOptionPane.showMessageDialog(null, "停止扫描点击");
        System.exit(1);
    }

    /**
     * 全屏截图
     * @return 返回BufferedImage
     */
    public static BufferedImage getFullScreenShot() {
        BufferedImage bfImage = null;
        int width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        try {
            Robot robot = new Robot();
            bfImage = robot.createScreenCapture(new Rectangle(0, 0, width, height));
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return bfImage;
    }

    /**
     * 查找图片
     */
    public static void findImage(KeyImage keyImage) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BufferedImage fullScreenShot = getFullScreenShot();

        KeyImage imageGRB = getImageGRB(fullScreenShot);
        screenShotImageRGBData = imageGRB.getKeyImageRGBData();
        scrShotImgWidth = imageGRB.getKeyImgWidth();
        scrShotImgHeight = imageGRB.getKeyImgHeight();

        int keyImgHeight = keyImage.getKeyImgHeight();
        int keyImgWidth = keyImage.getKeyImgWidth();
        int[][] keyImageRGBData = keyImage.getKeyImageRGBData();
        int[][][] findImgData = new int[keyImgHeight][keyImgWidth][2];
        //遍历屏幕截图像素点数据
        for(int y=0; y<scrShotImgHeight-keyImgHeight; y++) {
            for(int x=0; x<scrShotImgWidth-keyImgWidth; x++) {
                //根据目标图的尺寸，得到目标图四个角映射到屏幕截图上的四个点，
                //判断截图上对应的四个点与图B的四个角像素点的值是否相同，
                //如果相同就将屏幕截图上映射范围内的所有的点与目标图的所有的点进行比较。
                if((keyImageRGBData[0][0]^screenShotImageRGBData[y][x])==0
                        && (keyImageRGBData[0][keyImgWidth-1]^screenShotImageRGBData[y][x+keyImgWidth-1])==0
                        && (keyImageRGBData[keyImgHeight-1][keyImgWidth-1]^screenShotImageRGBData[y+keyImgHeight-1][x+keyImgWidth-1])==0
                        && (keyImageRGBData[keyImgHeight-1][0]^screenShotImageRGBData[y+keyImgHeight-1][x])==0) {

                    boolean isFinded = isMatchAll(y, x, keyImgHeight, keyImgWidth, keyImageRGBData);
                    //如果比较结果完全相同，则说明图片找到，填充查找到的位置坐标数据到查找结果数组。
                    if(isFinded) {
                        keyImage.setX(x);
                        keyImage.setY(y);
                        try {
                            move(keyImage);
                        } catch (AWTException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
            }
        }
    }


    /**
     * 判断屏幕截图上目标图映射范围内的全部点是否全部和小图的点一一对应。
     * @param y - 与目标图左上角像素点想匹配的屏幕截图y坐标
     * @param x - 与目标图左上角像素点想匹配的屏幕截图x坐标
     * @return
     */
    public static boolean isMatchAll(int y, int x,int keyImgHeight,int keyImgWidth,int[][] keyImageRGBData) {
        int biggerY = 0;
        int biggerX = 0;
        int xor = 0;
        for(int smallerY=0; smallerY<keyImgHeight; smallerY++) {
            biggerY = y+smallerY;
            for(int smallerX=0; smallerX<keyImgWidth; smallerX++) {
                biggerX = x+smallerX;
                if(biggerY>=scrShotImgHeight || biggerX>=scrShotImgWidth) {
                    return false;
                }
                xor = keyImageRGBData[smallerY][smallerX]^screenShotImageRGBData[biggerY][biggerX];
                if(xor!=0) {
                    return false;
                }
            }
            biggerX = x;
        }
        return true;
    }


    public static KeyImage getImageGRB(BufferedImage bfImage) {
        int width = bfImage.getWidth();
        int height = bfImage.getHeight();
        int[][] result = new int[height][width];
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                //使用getRGB(w, h)获取该点的颜色值是ARGB，而在实际应用中使用的是RGB，所以需要将ARGB转化成RGB，即bufImg.getRGB(w, h) & 0xFFFFFF。
                result[h][w] = bfImage.getRGB(w, h) & 0xFFFFFF;
            }
        }
        return new KeyImage(result,height,width,0,0);
    }


    public static void move(KeyImage keyImage) throws AWTException {
        Random b=new Random();
        int y = keyImage.getY();
        int x = keyImage.getX();
        int randomX=keyImage.getKeyImgWidth();
        int randomY=keyImage.getKeyImgHeight();
        if(x!=0&&y!=0){
            Robot myRobot = new Robot();
            // 移动鼠标到坐标（x,y)处，并点击左键
            myRobot.mouseMove(x+b.nextInt(randomX), y+b.nextInt(randomY)); // 移动鼠标到坐标（x,y）处
            myRobot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
            myRobot.setAutoDelay(200+b.nextInt(100));
            myRobot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);  // 模拟按下鼠标左键
        }
    }


}
