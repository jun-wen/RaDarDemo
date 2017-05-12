package com.helldefender.radardemo;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final double LENGTH_RADIUS = 300;

    private int flag = 0;

    private List<Point> mUpPointList;

    private List<Point> mDownPointList;

    private RaDarView mRaDarView;

    private Thread mThread;

    private RaDarHandler raDarHandler = new RaDarHandler(new WeakReference<MainActivity>(this));

    private double raDarAngle = Math.PI * 8 / 9;

    //private int raDarOffsetAngle = 30;

    private int raDarNum = 5;

    private int raDarVerticalDistance = 80;

    private int raDar1 = 40;
    private int raDar2 = 55;
    private int raDar3 = 30;
    private int raDar4 = 22;
    private int raDar5 = 44;
    private int raDar6 = 60;

//    private int raDra1 = 55;
//    private int raDra2 = 40;
//    private int raDra3 = 30;
//    private int raDra4 = 37;
//    private int raDra5 = 50;

//    private int raDra1 = 65;
//    private int raDra2 = 40;
//    private int raDra3 = 20;
//    private int raDra4 = 47;
//    private int raDra5 = 80;

//    private int raDra1 = 65;
//    private int raDra2 = 30;
//    private int raDra3 = 25;
//    private int raDra4 = 47;
//    private int raDra5 = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRaDarView = (RaDarView) findViewById(R.id.radarView_main_display);

        mThread = new Thread(this);

        Button startBtn = (Button) findViewById(R.id.btn_main_start);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThread.start();
            }
        });

        setRaDarView();

        initPointList();

        setPoint(raDar1, raDar2, raDar3, raDar4, raDar5, raDar6);
//        setPoint(65, 30, 20, 47, 30);
//        setPoint(25, 50, 30, 40, 60);
//        setPoint(65, 40, 10, 20, 35);   //出现白线
//        setPoint(30, 40, 50, 37, 46);   //线跑到图片上方
//        setPoint(45, 30, 48, 20, 10);
//        setPoint(55, 40, 30, 37, 50);

    }

    private void initPointList() {
        mUpPointList = new ArrayList<>();
        mDownPointList = new ArrayList<>();
    }

    private void setRaDarView() {
        mRaDarView.setRaDarAngle(160);
        //mRaDarView.setRaDarOffsetAngle(raDarOffsetAngle);
        mRaDarView.setVerticalDistance(raDarVerticalDistance);
        mRaDarView.setRaDarNum(raDarNum);
        mRaDarView.setDangerDistance(20);
        mRaDarView.setShortDistance(40);
        mRaDarView.setLongDistance(60);
        mRaDarView.setRaDar1MeasureLength(raDar1);
        mRaDarView.setRaDar2MeasureLength(raDar2);
        mRaDarView.setRaDar3MeasureLength(raDar3);
        mRaDarView.setRaDar4MeasureLength(raDar4);
        mRaDarView.setRaDar5MeasureLength(raDar5);
        mRaDarView.setRaDar6MeasureLength(raDar6);
    }

    private void setPoint(double r1, double r2, double r3, double r4, double r5, double r6) {
        mUpPointList.clear();
        mDownPointList.clear();


//        CarPoint cp = new CarPoint(ra, rb, rc, rd, Math.PI / 6, Math.PI / 6, 20, 35, 25, 25, 35);
//        RaDar rg = cp.getRaDarAll();
//        double end = rg.getEnd();

        //double[] radius = {r1, r2, r3, r4, r5, r6};// 雷达实际距离
        double[] radius = {r1, r2, r3, r4, r5};// 雷达实际距离
        //double[] radius = {r1, r2, r3, r4};// 雷达实际距离
        //double[] radius = {r1, r2, r3};// 雷达实际距离
        CarPoint cp = new CarPoint(raDarNum, radius, raDarAngle, raDarVerticalDistance, 180);
        RaDar rg = cp.getRaDarAll();
        double end = rg.getEnd();
        try {
            for (double i = rg.getStart(); i < end; i += 3) {
                int x = (int) ((i + getScreenWidth() / 2));
                int y = -((int) (cp.caculate(i)));
                //Log.d("DAI", "UP" + "  X   " + x + "    Y   " + y);
                mUpPointList.add(new Point(x, y));
            }
        } catch (Exception e) {
            //Log.d("DAI", "UP EXCEPTION ");
            e.printStackTrace();
        }

        //double[] radius2 = {r1 + LENGTH_RADIUS, r2 + LENGTH_RADIUS, r3 + LENGTH_RADIUS, r4 + LENGTH_RADIUS, r5 + LENGTH_RADIUS, r6 + LENGTH_RADIUS};// 雷达实际距离
        double[] radius2 = {r1 + LENGTH_RADIUS, r2 + LENGTH_RADIUS, r3 + LENGTH_RADIUS, r4 + LENGTH_RADIUS, r5 + LENGTH_RADIUS};// 雷达实际距离
        //double[] radius2 = {r1 + LENGTH_RADIUS, r2 + LENGTH_RADIUS, r3 + LENGTH_RADIUS, r4 + LENGTH_RADIUS};// 雷达实际距离
        //double[] radius2 = {r1 + LENGTH_RADIUS, r2 + LENGTH_RADIUS, r3 + LENGTH_RADIUS};// 雷达实际距离
        //CarPoint cp2 = new CarPoint(ra + LENGTH_RADIUS, rb + LENGTH_RADIUS, rc + LENGTH_RADIUS, rd + LENGTH_RADIUS, Math.PI / 6, Math.PI / 6, 20, 35, 25, 25, 35);
        CarPoint cp2 = new CarPoint(raDarNum, radius2, raDarAngle, raDarVerticalDistance, 180);
        RaDar rg2 = cp2.getRaDarAll();
        double end2 = rg2.getEnd();

        try {
            for (double i = rg2.getStart(); i < end2; i += 3) {
                int x = (int) ((i + getScreenWidth() / 2));
                int y = -((int) (cp2.caculate(i)));
                //Log.d("DAI", "DOWN" + "  X   " + x + "    Y   " + y);
                mDownPointList.add(new Point(x, y));
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.d("DAI", "RaDar Down");
        } finally {

        }

        mRaDarView.setRaDar1MeasureLength((int) r1);
        mRaDarView.setRaDar2MeasureLength((int) r2);
        mRaDarView.setRaDar3MeasureLength((int) r3);
        mRaDarView.setRaDar4MeasureLength((int) r4);
        mRaDarView.setRaDar5MeasureLength((int) r5);
        mRaDarView.setRaDar6MeasureLength((int) r6);

        mRaDarView.setPointList(mUpPointList, mDownPointList);
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    @Override
    public void run() {
        while (!mThread.isInterrupted()) {
            try {
                Thread.sleep(1300);
                flag += 1;
                Message message = raDarHandler.obtainMessage();
                message.arg1 = flag;
                raDarHandler.sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private static class RaDarHandler extends Handler {

        private WeakReference<MainActivity> weakReference;

        public RaDarHandler(WeakReference<MainActivity> weakReference) {
            this.weakReference = weakReference;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            MainActivity MainActivity = weakReference.get();

            if (MainActivity == null) {
                return;
            }

            switch (msg.arg1) {
                case 1:
                    MainActivity.setPoint(65, 30, 20, 47, 30, 20);
                    break;
                case 2:

                    MainActivity.setPoint(65, 50, 30, 40, 60, 60);
                    break;
                case 3:
                    MainActivity.setPoint(60, 50, 60, 90, 70, 50);
                    break;
                case 4:
                    MainActivity.setPoint(30, 40, 50, 37, 46, 70);
                    break;
                case 5:
                    MainActivity.setPoint(45, 30, 48, 20, 50, 80);
                    break;
                case 6:
                    MainActivity.setPoint(65, 45, 80, 60, 50, 60);

                    MainActivity.flag = 0;
                    break;
                default:
                    MainActivity.flag = 0;
            }
        }
    }
}
