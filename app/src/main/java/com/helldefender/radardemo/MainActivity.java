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

    private int raDra1 = 65;
    private int raDra2 = 45;
    private int raDra3 = 80;
    private int raDra4 = 60;
    private int raDra5 = 50;

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

        //存在的问题：
        //问题1：不同分区间有可能出现未填充区域（一条白线）
        //问题2：若起始地址不为区域一（eg：区域一内容为空，点集从区域二开始出现）
        //问题3：区域间突变的发生（eg:从区域3跳到区域2）
        //问题4：有的区域会贴到Bitmap上面           原因：问题点坐标中x过大（过于靠近图片边界），y为负值且过小
        //问题5：动态变换时，分区不明显（边界线改变）
        //问题6：对于过渡点的优化，可以解决问题5
        //问题7：对于分区的边界线的优化  可以尝试Paint抗锯齿

        setPoint(raDra1, raDra2, raDra3, raDra4, raDra5);
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
        mRaDarView.setANGLE_FIR(36);
        mRaDarView.setANGLE_SEC(45);
        mRaDarView.setANGLE_THI(45);
        mRaDarView.setANGLE_FOU(45);
        mRaDarView.setANGLE_FIF(36);
        mRaDarView.setLENGTH_1to2(35);
        mRaDarView.setLENGTH_2to3(50);
        mRaDarView.setLENGTH_3to4(50);
        mRaDarView.setLENGTH_4to5(35);
        mRaDarView.setDANGER_DISTANCE(20);
        mRaDarView.setSHORT_DISTANCE(40);
        mRaDarView.setLONG_DISTANCE(60);
        mRaDarView.setLENGTH_RADAR1(raDra1);
        mRaDarView.setLENGTH_RADAR2(raDra2);
        mRaDarView.setLENGTH_RADAR3(raDra3);
        mRaDarView.setLENGTH_RADAR4(raDra4);
        mRaDarView.setLENGTH_RADAR5(raDra5);
    }

    private void setPoint(double ra, double rb, double rc, double rd, double re) {
        mUpPointList.clear();
        mDownPointList.clear();

        CarPoint cp = new CarPoint(ra, rb, rc, rd, re, Math.PI / 4, Math.PI / 5, 20, 35, 50, 50, 35);
        RaDar rg = cp.getRangeAll();
        double end = rg.getEnd();

        try {
            for (double i = rg.getStart(); i < end; i += 20) {
                int x = (int) ((i + getScreenWidth() / 2));
                int y = -((int) (cp.caculate(i)));
                mUpPointList.add(new Point(x, y));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        CarPoint cp2 = new CarPoint(ra + LENGTH_RADIUS, rb + LENGTH_RADIUS, rc + LENGTH_RADIUS, rd + LENGTH_RADIUS, re + LENGTH_RADIUS, Math.PI / 4, Math.PI / 5, 20, 35, 50, 50, 35);
        RaDar rg2 = cp2.getRangeAll();
        double end2 = rg2.getEnd();

        try {
            for (double i = rg2.getStart(); i < end2; i += 30) {
                int x = (int) ((i + getScreenWidth() / 2));
                int y = -((int) (cp2.caculate(i)));
                mDownPointList.add(new Point(x, y));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

        mRaDarView.setLENGTH_RADAR1((int) ra);
        mRaDarView.setLENGTH_RADAR2((int) rb);
        mRaDarView.setLENGTH_RADAR3((int) rc);
        mRaDarView.setLENGTH_RADAR4((int) rd);
        mRaDarView.setLENGTH_RADAR5((int) re);

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
                    MainActivity.setPoint(65, 30, 20, 47, 30);
                    break;
                case 2:

                    MainActivity.setPoint(65, 50, 30, 40, 60);
                    break;
                case 3:
                    MainActivity.setPoint(60, 50, 60, 90, 70);
                    break;
                case 4:
                    MainActivity.setPoint(30, 40, 50, 37, 46);
                    break;
                case 5:
                    MainActivity.setPoint(45, 30, 48, 20, 50);
                    break;
                case 6:
                    MainActivity.setPoint(65, 45, 80, 60, 50);

                    MainActivity.flag = 0;
                    break;
                default:
                    MainActivity.flag = 0;
            }
        }
    }
}
