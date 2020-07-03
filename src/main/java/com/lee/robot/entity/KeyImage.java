package com.lee.robot.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyImage {
    private int[][] keyImageRGBData;
    private int keyImgHeight;
    private int keyImgWidth;

    private int x;
    private int y;
}
