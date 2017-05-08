package com.helldefender.radardemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Helldefender on 2017/5/2.
 */

public class RaDarView extends View {

    private static final int DANGER_DISTANCE_COLOR = Color.parseColor("#FF9900");

    private static final int SHORT_DISTANCE_COLOR = Color.parseColor("#FF9933");

    private static final int LONG_DISTANCE_COLOR = Color.parseColor("#FFCC00");

    private int DANGER_DISTANCE;

    private int SHORT_DISTANCE;

    private int LONG_DISTANCE;

    private final int REGION_FIR = 1;

    private final int REGION_SEC = 2;

    private final int REGION_THI = 3;

    private final int REGION_FOU = 4;

    private final int REGION_FIF = 5;

    private int LENGTH_RADAR1;

    private int LENGTH_RADAR2;

    private int LENGTH_RADAR3;

    private int LENGTH_RADAR4;

    private int LENGTH_RADAR5;

    private int LENGTH_1to2;

    public int LENGTH_2to3;

    public int LENGTH_3to4;

    public int LENGTH_4to5;

    public int ANGLE_FIR;

    public int ANGLE_SEC;

    public int ANGLE_THI;

    public int ANGLE_FOU;

    public int ANGLE_FIF;

    private int FLAG = 1;

    private int BITMAP_WIDTH;

    private int BITMAP_HEIGHT;

    private int textSize = 60;

    private float lineSmoothness = 0.2f;

    private List<Point> mUpPointList;

    private List<Point> mDownPointList;

    private List<Point> mRegionUpPointList;

    private List<Point> mRegionDownPointList;

    private Path mPath;

    private Paint mPaint;

    private Paint textPaint;

    private Rect textRect;

    private Bitmap carBitmap;

    private int mScreenWidth, mScreenHeight;

    private LinearGradient linearGradient;

    private int lastUpX;

    private int lastUpY;

    private int lastDownX;

    private int lastDownY;

    private int shaderStartX;

    private int shaderStartY = Integer.MAX_VALUE;

    private int shaderEndX;

    private int shaderEndY = Integer.MIN_VALUE;

    private int lastRaDarLength = Integer.MAX_VALUE;

    private String text = "";

    private boolean textFlag = true;

    public RaDarView(Context context) {
        this(context, null);
    }

    public RaDarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RaDarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPainter();
    }

    private void initPainter() {
        mPath = new Path();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setColor(Color.WHITE);

        textRect = new Rect();

        carBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        BITMAP_WIDTH = carBitmap.getWidth();
        BITMAP_HEIGHT = carBitmap.getHeight();

        mRegionUpPointList = new ArrayList<Point>();
        mRegionDownPointList = new ArrayList<Point>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;
        mScreenHeight = h;
    }

    public void setPointList(List<Point> upPointList, List<Point> downPointList) {
        mUpPointList = upPointList;
        mDownPointList = downPointList;

        reset();

        invalidate();
    }

    private void reset() {
        mPath.reset();
        mRegionUpPointList.clear();
        mRegionDownPointList.clear();

        FLAG = 1;

        textFlag = true;
        lastRaDarLength = Integer.MAX_VALUE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mUpPointList == null || mDownPointList == null)
            return;

        drawCar(canvas);

        canvas.translate(0, (getMeasuredHeight() + BITMAP_HEIGHT) / 2);

        drawRadar(canvas);

        drawText(canvas);
    }

    private void drawCar(Canvas canvas) {
        canvas.drawBitmap(carBitmap, (mScreenWidth - BITMAP_WIDTH) / 2, (mScreenHeight - BITMAP_HEIGHT) / 2, mPaint);
    }

    private void drawRadar(Canvas canvas) {

        //假如不是从第一个区域开始绘制
        //假如不是在最后一个区域结束绘制

        for (int i = 0; i < mUpPointList.size(); i++) {
            Point point = mUpPointList.get(i);
            getUpRegion(point, canvas);
        }

        //绘制剩余部分
        measurePath();

        for (int i = 1; i <= mDownPointList.size(); i++) {
            Point downPoint = mDownPointList.get(mDownPointList.size() - i);
            mPath.lineTo(downPoint.x, downPoint.y);

            if (downPoint.y > shaderEndY) {
                shaderEndX = downPoint.x;
                shaderEndY = downPoint.y;
            }
        }

        for (int i = 1; i <= mRegionDownPointList.size(); i++) {
            Point downPoint = mRegionDownPointList.get(mRegionDownPointList.size() - i);
            mPath.lineTo(downPoint.x, downPoint.y);

            if (downPoint.y > shaderEndY) {
                shaderEndX = downPoint.x;
                shaderEndY = downPoint.y;
            }
        }

        mPath.close();

        setPreference(FLAG);

        canvas.drawPath(mPath, mPaint);
    }

    private void drawText(Canvas canvas) {
        textPaint.getTextBounds(getText(), 0, getText().length(), textRect);
        int textWidth = textRect.width();
        int textHeight = textRect.height();
        float xCoordinate = (getMeasuredWidth() - textWidth) / 2;
        float yCoordinate = ((getMeasuredHeight() - BITMAP_HEIGHT) / 3 + textHeight) / 2;
        canvas.drawText(getText(), xCoordinate, yCoordinate, textPaint);
        canvas.save();
    }

    private void getUpRegion(Point point, Canvas canvas) {
        switch (FLAG) {
            case REGION_FIR:
                int dxTo2 = getMeasuredWidth() / 2 - LENGTH_2to3 - point.x;
                judgeUpPoint(dxTo2, ANGLE_SEC, point, true, canvas);
                break;
            case REGION_SEC:
                int dxTo3 = getMeasuredWidth() / 2 - point.x;
                judgeUpPoint(dxTo3, ANGLE_THI, point, true, canvas);
                break;
            case REGION_THI:
                int dxTo3Right = point.x - getMeasuredWidth() / 2;
                judgeUpPoint(dxTo3Right, ANGLE_THI, point, false, canvas);
                break;
            case REGION_FOU:
                int dxTo4Right = getMeasuredWidth() / 2 + LENGTH_3to4 - point.x;
                judgeUpPoint(dxTo4Right, ANGLE_FOU, point, false, canvas);
                break;
            case REGION_FIF:
                mRegionUpPointList.add(point);

                if (point.y < shaderStartY) {
                    shaderStartX = point.x;
                    shaderStartY = point.y;
                }
        }
    }

    private void judgeUpPoint(int dx, int angle, Point point, boolean left, Canvas canvas) {
        int borderValue = (int) (Math.abs(dx) * Math.tan(Math.toRadians(90 - angle / 2)));

        if ((left ? point.y < borderValue : point.y > borderValue) && point.x < getRaDarPosition()) {
//            if (FLAG == 2 || FLAG == 3 || FLAG == 4) {
//                int raDarX = getMeasuredWidth() / 2 + getRaDarDistance();
//                int borderY = (int) (Math.abs(raDarX - point.x) * Math.tan(Math.toRadians(90 - angle / 2)));
//                if (point.y > borderY) {
//                    mRegionUpPointList.add(point);
//                }
//            } else {
//                mRegionUpPointList.add(point);
//            }
            mRegionUpPointList.add(point);

            if (point.y < shaderStartY) {
                shaderStartX = point.x;
                shaderStartY = point.y;
            }
        } else {
            //处理过渡点
            int borderX = (lastUpX + point.x) / 2;
            int borderY = (lastUpY + point.y) / 2;

//            double tempBorder = point.x + borderValue * Math.tan(Math.toRadians(angle / 2)) - (getRaDarPosition() * Math.tan(Math.toRadians(90 - angle / 2)) * Math.tan(Math.toRadians(angle / 2)));
//            int borderX = (int) (tempBorder / (1 - Math.tan(Math.toRadians(90 - angle / 2)) * Math.tan(Math.toRadians(angle / 2))));
//            int borderY = (int) (Math.tan(Math.toRadians(90 - angle / 2)) * Math.abs(getRaDarPosition() - borderX));

            mRegionUpPointList.add(new Point(borderX, borderY));

            if (borderY < shaderStartY) {
                shaderStartX = borderX;
                shaderStartY = borderY;
            }

            measurePath();

            drawDownLine(canvas);

            mRegionUpPointList.clear();
            mRegionUpPointList.add(new Point(borderX, borderY));
            mRegionUpPointList.add(point);

            if (borderY < point.y) {
                shaderStartX = borderX;
                shaderStartY = borderY;
            } else {
                shaderStartX = point.x;
                shaderStartY = point.y;
            }

            FLAG++;
        }
        lastUpX = point.x;
        lastUpY = point.y;
    }

    private void drawDownLine(Canvas canvas) {
        for (int i = 0; i < mDownPointList.size(); i++) {
            Point point = mDownPointList.get(i);
            if (getDownRegion(point, canvas, i + 1)) {
                break;
            }
        }
    }

    private boolean getDownRegion(Point point, Canvas canvas, int i) {
        switch (FLAG) {
            case REGION_FIR:
                int dxTo2 = getMeasuredWidth() / 2 - LENGTH_2to3 - point.x;
                return judgeDownPoint(dxTo2, ANGLE_SEC, point, true, canvas, i);
            case REGION_SEC:
                int dxTo3 = getMeasuredWidth() / 2 - point.x;
                return judgeDownPoint(dxTo3, ANGLE_THI, point, true, canvas, i);
            case REGION_THI:
                int dxTo3Right = point.x - getMeasuredWidth() / 2;
                return judgeDownPoint(dxTo3Right, ANGLE_THI, point, false, canvas, i);
            case REGION_FOU:
                int dxTo4Right = getMeasuredWidth() / 2 + LENGTH_3to4 - point.x;
                return judgeDownPoint(dxTo4Right, ANGLE_FOU, point, false, canvas, i);
            default:
                return false;
        }
    }

    private boolean judgeDownPoint(int dx, int angle, Point point, boolean left, Canvas canvas, int deleteLength) {
        int borderValue = (int) (Math.abs(dx) * Math.tan(Math.toRadians(90 - angle / 2)));

        //对于区域 2,3,4中的跳跃点进行处理，如果为跳跃点不加入到集合中
        if (left ? point.y < borderValue : point.y > borderValue) {

//            if (FLAG == 2 || FLAG == 3 || FLAG == 4) {
//                int raDarX = getMeasuredWidth() / 2 + getRaDarDistance();
//                //这里的角度可能不对，如果现在是区域2，angle为雷达3的角度  如果每个雷达的张角都是一样的话，就不用考虑
//                int borderY = (int) (Math.abs(raDarX - point.x) * Math.tan(Math.toRadians(90 - angle / 2)));
//                if (point.y > borderY) {
//                    mRegionDownPointList.add(point);
//                }
//            } else {
//                mRegionDownPointList.add(point);
//            }

            mRegionDownPointList.add(point);

            if (point.y > shaderEndY) {
                shaderEndX = point.x;
                shaderEndY = point.y;
            }

            lastDownX = point.x;
            lastDownY = point.y;

            return false;
        } else {
            //对过渡点的处理
            int borderX = (lastDownX + point.x) / 2;
            int borderY = (lastDownY + point.y) / 2;

//            double tempBorder = point.x + borderValue * Math.tan(Math.toRadians(angle / 2)) - (getRaDarPosition() * Math.tan(Math.toRadians(90 - angle / 2)) * Math.tan(Math.toRadians(angle / 2)));
//            int borderX = (int) (tempBorder / (1 - Math.tan(Math.toRadians(90 - angle / 2)) * Math.tan(Math.toRadians(angle / 2))));
//            int borderY = (int) (Math.tan(Math.toRadians(90 - angle / 2)) * Math.abs(getRaDarPosition() - borderX));

            mRegionDownPointList.add(new Point(borderX, borderY));

            for (int i = 1; i <= mRegionDownPointList.size(); i++) {
                Point downPoint = mRegionDownPointList.get(mRegionDownPointList.size() - i);
                mPath.lineTo(downPoint.x, downPoint.y);
            }

            mPath.close();


            if (borderY > shaderEndY) {
                shaderEndX = borderX;
                shaderEndY = borderY;
            }

            setPreference(FLAG);

            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mPath, mPaint);

            mPath.reset();

            //删除下界线点集中已经使用过的部分点
            for (int i = 0; i < deleteLength; i++) {
                mDownPointList.remove(0);
            }

            mRegionDownPointList.clear();
            mRegionDownPointList.add(new Point(borderX, borderY));
            mRegionDownPointList.add(point);

            if (borderY > point.y) {
                shaderEndX = borderX;
                shaderEndY = borderY;
            } else {
                shaderEndX = point.x;
                shaderEndY = point.y;
            }

            lastDownX = point.x;
            lastDownY = point.y;

            return true;
        }
    }

    private int getRaDarPosition() {
        switch (FLAG) {
            case 1:
                return getMeasuredWidth() / 2 - LENGTH_2to3;
            case 2:
                return getMeasuredWidth() / 2;
            case 3:
                return getMeasuredWidth() / 2;
            case 4:
                return getMeasuredWidth() / 2 + LENGTH_3to4;
            default:
                return getMeasuredWidth() / 2;
        }
    }


    private void setPreference(int FLAG) {
        switch (FLAG) {
            case REGION_FIR:
                setShader(LENGTH_RADAR1);
                break;
            case REGION_SEC:
                setShader(LENGTH_RADAR2);
                break;
            case REGION_THI:
                setShader(LENGTH_RADAR3);
                break;
            case REGION_FOU:
                setShader(LENGTH_RADAR4);
                break;
            case REGION_FIF:
                setShader(LENGTH_RADAR5);
                break;
        }
    }


    private void setShader(int radarLength) {
        if (radarLength <= DANGER_DISTANCE) {
            linearGradient = new LinearGradient(shaderEndX, shaderStartY, shaderEndX, shaderEndY, DANGER_DISTANCE_COLOR, Color.WHITE, Shader.TileMode.REPEAT);

            mPaint.setShader(linearGradient);

            setText("停止");
            textFlag = false;
        } else if (radarLength <= SHORT_DISTANCE) {
            linearGradient = new LinearGradient(shaderEndX, shaderStartY, shaderEndX, shaderEndY, SHORT_DISTANCE_COLOR, Color.WHITE, Shader.TileMode.REPEAT);

            mPaint.setShader(linearGradient);

            if (radarLength < lastRaDarLength && textFlag) {
                setText(radarLength + "cm");
                lastRaDarLength = radarLength;
            }
        } else {
            linearGradient = new LinearGradient(shaderEndX, shaderStartY, shaderEndX, shaderEndY, LONG_DISTANCE_COLOR, Color.WHITE, Shader.TileMode.REPEAT);

            mPaint.setShader(linearGradient);

            if (radarLength < lastRaDarLength && textFlag) {
                setText(radarLength + "cm");
                lastRaDarLength = radarLength;
            }
        }
    }

    private void setText(String text) {
        this.text = text;
    }

    private String getText() {
        return text;
    }

    private void measurePath() {
        float prePreviousPointX = Float.NaN;
        float prePreviousPointY = Float.NaN;
        float previousPointX = Float.NaN;
        float previousPointY = Float.NaN;
        float currentPointX = Float.NaN;
        float currentPointY = Float.NaN;
        float nextPointX;
        float nextPointY;

        final int lineSize = mRegionUpPointList.size();
        for (int valueIndex = 0; valueIndex < lineSize; ++valueIndex) {
            if (Float.isNaN(currentPointX)) {
                Point point = mRegionUpPointList.get(valueIndex);
                currentPointX = point.x;
                currentPointY = point.y;
            }
            if (Float.isNaN(previousPointX)) {
                //是否是第一个点
                if (valueIndex > 0) {
                    Point point = mRegionUpPointList.get(valueIndex - 1);
                    previousPointX = point.x;
                    previousPointY = point.y;
                } else {
                    //是的话就用当前点表示上一个点
                    previousPointX = currentPointX;
                    previousPointY = currentPointY;
                }
            }

            if (Float.isNaN(prePreviousPointX)) {
                //是否是前两个点
                if (valueIndex > 1) {
                    Point point = mRegionUpPointList.get(valueIndex - 2);
                    prePreviousPointX = point.x;
                    prePreviousPointY = point.y;
                } else {
                    //是的话就用当前点表示上上个点
                    prePreviousPointX = previousPointX;
                    prePreviousPointY = previousPointY;
                }
            }

            // 判断是不是最后一个点了
            if (valueIndex < lineSize - 1) {
                Point point = mRegionUpPointList.get(valueIndex + 1);
                nextPointX = point.x;
                nextPointY = point.y;
            } else {
                //是的话就用当前点表示下一个点
                nextPointX = currentPointX;
                nextPointY = currentPointY;
            }

            if (valueIndex == 0) {
                // 将Path移动到开始点
                mPath.moveTo(currentPointX, currentPointY);
            } else {
                // 求出控制点坐标
                final float firstDiffX = (currentPointX - prePreviousPointX);
                final float firstDiffY = (currentPointY - prePreviousPointY);
                final float secondDiffX = (nextPointX - previousPointX);
                final float secondDiffY = (nextPointY - previousPointY);
                final float firstControlPointX = previousPointX + (lineSmoothness * firstDiffX);
                final float firstControlPointY = previousPointY + (lineSmoothness * firstDiffY);
                final float secondControlPointX = currentPointX - (lineSmoothness * secondDiffX);
                final float secondControlPointY = currentPointY - (lineSmoothness * secondDiffY);
                //画出曲线
                mPath.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                        currentPointX, currentPointY);
                //将控制点保存到辅助路径上
            }

            // 更新值,
            prePreviousPointX = previousPointX;
            prePreviousPointY = previousPointY;
            previousPointX = currentPointX;
            previousPointY = currentPointY;
            currentPointX = nextPointX;
            currentPointY = nextPointY;
        }
    }

    public int getLENGTH_RADAR1() {
        return LENGTH_RADAR1;
    }

    public void setLENGTH_RADAR1(int LENGTH_RADAR1) {
        this.LENGTH_RADAR1 = LENGTH_RADAR1;
    }

    public int getLENGTH_RADAR2() {
        return LENGTH_RADAR2;
    }

    public void setLENGTH_RADAR2(int LENGTH_RADAR2) {
        this.LENGTH_RADAR2 = LENGTH_RADAR2;
    }

    public int getLENGTH_RADAR3() {
        return LENGTH_RADAR3;
    }

    public void setLENGTH_RADAR3(int LENGTH_RADAR3) {
        this.LENGTH_RADAR3 = LENGTH_RADAR3;
    }

    public int getLENGTH_RADAR4() {
        return LENGTH_RADAR4;
    }

    public void setLENGTH_RADAR4(int LENGTH_RADAR4) {
        this.LENGTH_RADAR4 = LENGTH_RADAR4;
    }

    public int getLENGTH_RADAR5() {
        return LENGTH_RADAR5;
    }

    public void setLENGTH_RADAR5(int LENGTH_RADAR5) {
        this.LENGTH_RADAR5 = LENGTH_RADAR5;
    }

    public int getLENGTH_1to2() {
        return LENGTH_1to2;
    }

    public void setLENGTH_1to2(int LENGTH_1to2) {
        this.LENGTH_1to2 = LENGTH_1to2;
    }

    public int getLENGTH_2to3() {
        return LENGTH_2to3;
    }

    public void setLENGTH_2to3(int LENGTH_2to3) {
        this.LENGTH_2to3 = LENGTH_2to3;
    }

    public int getLENGTH_3to4() {
        return LENGTH_3to4;
    }

    public void setLENGTH_3to4(int LENGTH_3to4) {
        this.LENGTH_3to4 = LENGTH_3to4;
    }

    public int getLENGTH_4to5() {
        return LENGTH_4to5;
    }

    public void setLENGTH_4to5(int LENGTH_4to5) {
        this.LENGTH_4to5 = LENGTH_4to5;
    }

    public int getANGLE_FIR() {
        return ANGLE_FIR;
    }

    public void setANGLE_FIR(int ANGLE_FIR) {
        this.ANGLE_FIR = ANGLE_FIR;
    }

    public int getANGLE_FIF() {
        return ANGLE_FIF;
    }

    public void setANGLE_FIF(int ANGLE_FIF) {
        this.ANGLE_FIF = ANGLE_FIF;
    }

    public int getANGLE_SEC() {
        return ANGLE_SEC;
    }

    public void setANGLE_SEC(int ANGLE_SEC) {
        this.ANGLE_SEC = ANGLE_SEC;
    }

    public int getANGLE_THI() {
        return ANGLE_THI;
    }

    public void setANGLE_THI(int ANGLE_THI) {
        this.ANGLE_THI = ANGLE_THI;
    }

    public int getANGLE_FOU() {
        return ANGLE_FOU;
    }

    public void setANGLE_FOU(int ANGLE_FOU) {
        this.ANGLE_FOU = ANGLE_FOU;
    }

    public int getDANGER_DISTANCE() {
        return DANGER_DISTANCE;
    }

    public void setDANGER_DISTANCE(int DANGER_DISTANCE) {
        this.DANGER_DISTANCE = DANGER_DISTANCE;
    }

    public int getSHORT_DISTANCE() {
        return SHORT_DISTANCE;
    }

    public void setSHORT_DISTANCE(int SHORT_DISTANCE) {
        this.SHORT_DISTANCE = SHORT_DISTANCE;
    }

    public int getLONG_DISTANCE() {
        return LONG_DISTANCE;
    }

    public void setLONG_DISTANCE(int LONG_DISTANCE) {
        this.LONG_DISTANCE = LONG_DISTANCE;
    }
}
