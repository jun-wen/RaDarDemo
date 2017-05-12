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

    private int dangerDistance;

    private int shortDistance;

    private int longDistance;

    private int raDar1MeasureLength;

    private int raDar2MeasureLength;

    private int raDar3MeasureLength;

    private int raDar4MeasureLength;

    private int raDar5MeasureLength;

    private int raDar6MeasureLength;

    private int raDarNum;

    private int verticalDistance;

    private int raDarAngle;

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
        //int regionAngle = (raDarAngle + raDarOffsetAngle) * 2 / raDarNum;
        int angle = 90 - getRegionAngle();
        int dx = getRegionLocation() - point.x;
        //Log.d("DAI", "FLAG " + FLAG + "  getRegionAngle  " + getRegionAngle() + "  RegionLocation  " + getRegionLocation());

        if (raDarNum % 2 == 0) {
            if (FLAG < (raDarNum / 2)) {
                judgeUpPoint(dx, angle, point, canvas, false, true, false);
            } else if (FLAG == (raDarNum / 2)) {
                //int angle = 0;
                if (point.x < getRegionLocation()) {
                    judgeUpPoint(dx, getRegionAngle(), point, canvas, false, false, false);
                } else {
                    judgeUpPoint(dx, getRegionAngle(), point, canvas, true, false, false);
                }
            } else if (FLAG != raDarNum) {
                if (point.x <= getRegionLocation()) {
                    judgeUpPoint(dx, angle, point, canvas, false, false, false);
                } else if (point.y > (int) (Math.abs(dx) * Math.tan(Math.toRadians(angle)))) {
                    judgeUpPoint(dx, angle, point, canvas, false, false, false);
                } else {
                    judgeUpPoint(dx, angle, point, canvas, true, false, false);
                }
            } else {
                mRegionUpPointList.add(point);

                if (point.y < shaderStartY) {
                    shaderStartX = point.x;
                    shaderStartY = point.y;
                }
            }
        } else {
            if (FLAG <= (raDarNum / 2)) {
                //int angle = 90 - getRegionAngle();
                judgeUpPoint(dx, angle, point, canvas, false, true, false);
            } else if (FLAG != raDarNum) {
                //int angle = 90 - getRegionAngle();
                if (point.x <= getRegionLocation()) {
                    judgeUpPoint(dx, angle, point, canvas, false, false, true);
                } else if (point.y > (int) (Math.abs(dx) * Math.tan(Math.toRadians(angle)))) {
                    judgeUpPoint(dx, angle, point, canvas, false, false, true);
                } else {
                    judgeUpPoint(dx, angle, point, canvas, true, false, false);
                }
            } else {
                mRegionUpPointList.add(point);

                if (point.y < shaderStartY) {
                    shaderStartX = point.x;
                    shaderStartY = point.y;
                }
            }
        }
    }

    private void judgeUpPoint(int dx, int angle, Point point, Canvas canvas, boolean isCenter, boolean isLeft, boolean isAdd) {
        int borderValue = (int) (Math.abs(dx) * Math.tan(Math.toRadians(angle)));

        //Log.d("DAI", "UP  " + FLAG + "  " + " borderValue " + borderValue + "   X  " + point.x + "  Y  " + point.y + "  angle  " + angle + "  dx  " + dx);

        if (isLeft ? point.y < borderValue : point.y > borderValue && !isCenter || isAdd) {

            mRegionUpPointList.add(point);

            if (point.y < shaderStartY) {
                shaderStartX = point.x;
                shaderStartY = point.y;
            }
        } else {
            //处理过渡点
            int borderX = (lastUpX + point.x) / 2;
            int borderY = (lastUpY + point.y) / 2;

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
        //int regionAngle = raDarAngle / raDarNum;
        int angle = 90 - getRegionAngle();
        int dx = getRegionLocation() - point.x;

        if (raDarNum % 2 == 0) {
            if (FLAG < (raDarNum / 2)) {
                return judgeDownPoint(dx, angle, point, canvas, i, false, true, false);
            } else if (FLAG == (raDarNum / 2)) {
                if (point.x < getRegionLocation()) {
                    return judgeDownPoint(dx, getRegionAngle(), point, canvas, i, false, false, false);
                } else {
                    return judgeDownPoint(dx, getRegionAngle(), point, canvas, i, true, false, false);
                }
            } else if (FLAG != raDarNum) {
                if (point.x <= getRegionLocation()) {
                    return judgeDownPoint(dx, angle, point, canvas, i, false, false, false);
                } else if (point.y > (int) (Math.abs(dx) * Math.tan(Math.toRadians(angle)))) {
                    return judgeDownPoint(dx, angle, point, canvas, i, false, false, false);
                } else {
                    return judgeDownPoint(dx, angle, point, canvas, i, true, false, false);
                }
            } else {
                return false;
            }
        } else {
            if (FLAG <= (raDarNum / 2)) {
                return judgeDownPoint(dx, angle, point, canvas, i, false, true, false);
            } else if (FLAG != raDarNum) {
                if (point.x <= getRegionLocation()) {
                    return judgeDownPoint(dx, angle, point, canvas, i, false, false, true);
                } else if (point.y > (int) (Math.abs(dx) * Math.tan(Math.toRadians(angle)))) {
                    return judgeDownPoint(dx, angle, point, canvas, i, false, false, true);
                } else {
                    return judgeDownPoint(dx, angle, point, canvas, i, true, false, false);
                }
            } else {
                return false;
            }
        }
    }

    private boolean judgeDownPoint(int dx, int angle, Point point, Canvas canvas, int deleteLength, boolean isCenter, boolean isLeft, boolean isAdd) {
        int borderValue = (int) (Math.abs(dx) * Math.tan(Math.toRadians(angle)));

        //Log.d("DAI", "DOWN" + "   borderValue  " + borderValue + "   X  " + point.x + "  Y  " + point.y);

        if (isLeft ? point.y < borderValue : point.y > borderValue && !isCenter || isAdd) {
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

    private int getRegionLocation() {
        int regionAngle = raDarAngle / raDarNum;

        //雷达个数为偶数
        if (raDarNum % 2 == 0) {
            if (FLAG < (raDarNum / 2)) {
                return (int) (getMeasuredWidth() / 2 - verticalDistance * Math.tan(Math.toRadians((raDarNum / 2 - FLAG) * regionAngle)));
            } else if (FLAG == (raDarNum / 2)) {
                return getMeasuredWidth() / 2;
            } else {
                return (int) (getMeasuredWidth() / 2 + verticalDistance * Math.tan(Math.toRadians((FLAG - raDarNum / 2) * regionAngle)));
            }
        } else {
            if (FLAG <= (raDarNum / 2)) {
                return (int) (getMeasuredWidth() / 2 - verticalDistance * Math.tan(Math.toRadians((raDarNum - 2 * FLAG) * regionAngle / 2)));
            }
            //绘制完成进行数值重置
            return (int) (getMeasuredWidth() / 2 + verticalDistance * Math.tan(Math.toRadians((2 * FLAG - raDarNum) * regionAngle / 2)));
        }
    }

    private int getRegionAngle() {
        int regionAngle = raDarAngle / raDarNum;

        //Log.d("DAI", "RegionAngle  " + regionAngle);
        //雷达个数为偶数
        if (raDarNum % 2 == 0) {
            if (FLAG <= (raDarNum / 2)) {
                return (raDarNum / 2 - FLAG) * regionAngle;
            } else {
                return (FLAG - raDarNum / 2) * regionAngle;
            }
        } else {
            if (FLAG <= (raDarNum / 2)) {
                return (raDarNum - 2 * FLAG) * regionAngle / 2;
            }
            return (2 * FLAG - raDarNum) * regionAngle / 2;
        }
    }


    private void setPreference(int FLAG) {
        switch (FLAG) {
            case 1:
                //mPaint.setColor(Color.GREEN);
                setShader(raDar1MeasureLength);
                break;
            case 2:
                //mPaint.setColor(Color.RED);
                setShader(raDar2MeasureLength);
                break;
            case 3:
                //mPaint.setColor(Color.BLUE);
                setShader(raDar3MeasureLength);
                break;
            case 4:
                //mPaint.setColor(Color.CYAN);
                setShader(raDar4MeasureLength);
                break;
            case 5:
                //mPaint.setColor(Color.WHITE);
                setShader(raDar5MeasureLength);
                break;
            case 6:
                //mPaint.setColor(Color.YELLOW);
                setShader(raDar6MeasureLength);
                break;
        }
    }


    private void setShader(int radarLength) {
        if (radarLength <= dangerDistance) {
            linearGradient = new LinearGradient(shaderEndX, shaderStartY, shaderEndX, shaderEndY, DANGER_DISTANCE_COLOR, Color.WHITE, Shader.TileMode.REPEAT);

            mPaint.setShader(linearGradient);

            setText("停止");
            textFlag = false;
        } else if (radarLength <= shortDistance) {
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

    public void setVerticalDistance(int verticalDistance) {
        this.verticalDistance = verticalDistance;
    }

    public void setRaDarAngle(int raDarAngle) {
        this.raDarAngle = raDarAngle;
    }

    public void setDangerDistance(int dangerDistance) {
        this.dangerDistance = dangerDistance;
    }

    public void setShortDistance(int shortDistance) {
        this.shortDistance = shortDistance;
    }

    public void setLongDistance(int longDistance) {
        this.longDistance = longDistance;
    }

    public void setRaDar1MeasureLength(int raDar1MeasureLength) {
        this.raDar1MeasureLength = raDar1MeasureLength;
    }

    public void setRaDar2MeasureLength(int raDar2MeasureLength) {
        this.raDar2MeasureLength = raDar2MeasureLength;
    }

    public void setRaDar3MeasureLength(int raDar3MeasureLength) {
        this.raDar3MeasureLength = raDar3MeasureLength;
    }

    public void setRaDar4MeasureLength(int raDar4MeasureLength) {
        this.raDar4MeasureLength = raDar4MeasureLength;
    }

    public void setRaDar5MeasureLength(int raDar5MeasureLength) {
        this.raDar5MeasureLength = raDar5MeasureLength;
    }

    public void setRaDar6MeasureLength(int raDar6MeasureLength) {
        this.raDar6MeasureLength = raDar6MeasureLength;
    }

    public void setRaDarNum(int raDarNum) {
        this.raDarNum = raDarNum;
    }
}
