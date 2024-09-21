package com.example._pill_20102669_datastruc;

import javafx.scene.shape.Rectangle;

public class Rect {
    private int minXCoord;
    private int minYCoord;
    private Rectangle rect;


    public Rect(Rectangle rect, int minXCoord, int minYCoord) {
        this.minXCoord = minXCoord;
        this.minYCoord = minYCoord;
        this.rect = rect;

    }


    public int getMinXCoord() {
        return minXCoord;
    }

    public int getMinYCoord() {
        return minYCoord;
    }
    public Rectangle getRect() {
        return rect;
    }

}
