package com.helldefender.radardemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarPoint {
    private int raderNumber;
    private double[] radius;
    private double angle;// 雷达角度θ
    private double verticalDistance;// 雷达垂直距离
    private double carWidth;
    private List<RaDar> RaDars = new ArrayList<RaDar>();
    private double distanceRadius;

    Map<Integer, RaDar> repeatRaDarMap = new HashMap<Integer, RaDar>();// RaDarmap[0]表示雷达A和B波的重复区依次类推到RaDar[3]表示D和E波重复的区域(X轴的区域)
    Map<Integer, RaDar> intervalRaDarMap = new HashMap<Integer, RaDar>();// RaDarmap[0]表示雷达A和B波的间隔区依次类推到RaDar[3]表示D和E波重复的区域(X轴的区域)

    public CarPoint(int raderNumber, double[] radius, double angle, double verticalDistance, double carWidth) {

        this.raderNumber = raderNumber;
        this.radius = radius;
        this.angle = angle;
        this.verticalDistance = verticalDistance;
        this.carWidth = carWidth;
        this.distanceRadius = Math.sqrt(Math.pow(verticalDistance, 2) + Math.pow(carWidth / 2, 2));
        setXRaDars();// 初始化每个雷达的区间
        getOverlappingRaDar();// 获取雷达弧段之间交叉区对应的横坐标X范围
    }

    private void setXRaDars() {
        int flag = -1;
        if (this.raderNumber % 2 == 0) {
            flag = 0;// 偶数个雷达
        } else {
            flag = 1;// 奇数个雷达
        }
        double smallAngle = this.angle / raderNumber;
        for (int i = 0; i < this.raderNumber; i++) {
            RaDar RaDar = new RaDar();
            double r = this.distanceRadius + this.radius[i];// 加上虚拟距离后的半径
            if (flag == 0) {
                if (i <= raderNumber / 2 - 1) {
                    RaDar.setStart(-r * Math.sin(smallAngle * (this.raderNumber / 2 - i)));
                    RaDar.setEnd(-r * Math.sin(smallAngle * (this.raderNumber / 2 - i - 1)));
                    //System.out.println("雷达范围---" + RaDar);
                    this.RaDars.add(RaDar);
                } else {
                    RaDar.setStart(r * Math.sin(smallAngle * (i - this.raderNumber / 2)));
                    RaDar.setEnd(r * Math.sin(smallAngle * (i - this.raderNumber / 2 + 1)));
                    //System.out.println("雷达范围---" + RaDar);
                    this.RaDars.add(RaDar);
                }
            } else {
                if (i <= raderNumber / 2 - 1) {
                    RaDar.setStart(-r * Math.sin(smallAngle / 2 + smallAngle * (this.raderNumber / 2 - i)));
                    RaDar.setEnd(-r * Math.sin(smallAngle / 2 + smallAngle * (this.raderNumber / 2 - i - 1)));
                    //System.out.println("雷达范围---" + RaDar);
                    this.RaDars.add(RaDar);
                } else if (i >= raderNumber / 2 + 1) {
                    RaDar.setStart(r * Math.sin(smallAngle / 2 + smallAngle * (i - 1 - this.raderNumber / 2)));
                    RaDar.setEnd(r * Math.sin(smallAngle / 2 + smallAngle * (i - this.raderNumber / 2)));
                    //System.out.println("雷达范围---" + RaDar);
                    this.RaDars.add(RaDar);
                } else {
                    RaDar.setStart(-r * Math.sin(smallAngle / 2));
                    RaDar.setEnd(r * Math.sin(smallAngle / 2));
                    //System.out.println("雷达范围---" + RaDar);
                    this.RaDars.add(RaDar);
                }
            }
            // re
        }
    }

    // 获取雷达弧段之间交叉区对应的横坐标X范围 或者x不连续的区间
    private void getOverlappingRaDar() {
        for (int i = 0; i < RaDars.size() - 1; i++) {
            if (this.RaDars.get(i).getEnd() >= this.RaDars.get(i + 1).getStart()) {
                RaDar RaDar = new RaDar();
                RaDar.setStart(this.RaDars.get(i).getEnd());
                RaDar.setEnd(this.RaDars.get(i + 1).getStart());
                //System.out.println("重复范围---" + i + "----" + RaDar);
                this.repeatRaDarMap.put(i, RaDar);
            } else {
                RaDar RaDar = new RaDar();
                RaDar.setStart(this.RaDars.get(i).getEnd());
                RaDar.setEnd(this.RaDars.get(i + 1).getStart());
                //System.out.println("间隔范围---" + i + "----" + RaDar);
                this.intervalRaDarMap.put(i, RaDar);
            }
        }
    }

    // 给定x和雷达区间编号求Y
    private double getY(double x, int rader) {
        return -Math.sqrt(Math.pow(this.radius[rader] + this.distanceRadius, 2) - Math.pow(x, 2)) + verticalDistance;
    }

    // 判断x是否在给定的范围内
    private boolean isInRaDars(double x, RaDar rg) {
        if (x < rg.getStart() || x > rg.getEnd()) {
            return false;
        }
        return true;
    }

    // 返回X所在的雷达区域
    private int getCadarRg(double x) {
        int result = -1;
        for (int i = 0; i < RaDars.size(); i++) {
            RaDar RaDar = RaDars.get(i);
            if (isInRaDars(x, RaDar)) {
                result = i;
                break;
            }
        }

        return result;
    }

    // 获取整个取钱的x的取值空间
    public RaDar getRaDarAll() {
        RaDar rg = new RaDar();
        rg.setStart(this.RaDars.get(0).getStart());
        rg.setEnd(this.RaDars.get(this.raderNumber - 1).getEnd());
        //System.out.println("总共范围---" + rg);
        return rg;
    }

    public double caculate(double x) {

        int isInRepeatRegion = -1;// -1表示不再重复区，其他表示所在重复区
        int isInSeptalArea = -1;// -1表示不再间隔区，其他表示所在重复区
        int cadarX = -1;// 表示X 在哪个雷达管辖区域

        for (Map.Entry<Integer, RaDar> entry : this.repeatRaDarMap.entrySet()) {
            if (isInRaDars(x, entry.getValue())) {
                isInRepeatRegion = entry.getKey();// 重复区域编号
            }
        }
        for (Map.Entry<Integer, RaDar> entry : this.intervalRaDarMap.entrySet()) {
            if (isInRaDars(x, entry.getValue())) {
                isInSeptalArea = entry.getKey();// 间隔区域编号
            }
        }

        // 判断x是否在重复区
        if (isInRepeatRegion == -1) {
            cadarX = getCadarRg(x);

            if (cadarX != -1) {
                // 不在重复区域，不在间隔区域的
                return getY(x, cadarX);
            }

            if (cadarX == -1 && isInSeptalArea != -1) {
                // 间隔区
                // return optimizationSeptalAreaY(isInSeptalArea, x);
                RaDar RaDar = this.intervalRaDarMap.get(isInSeptalArea);
                double startY = getY(RaDar.getStart(), getCadarRg(RaDar.getStart()));
                double endY = getY(RaDar.getEnd(), getCadarRg(RaDar.getEnd()));
                return (startY - endY) / (RaDar.getStart() - RaDar.getEnd()) * x
                        + (RaDar.getStart() * endY - RaDar.getEnd() * startY) / (RaDar.getStart() - RaDar.getEnd());
            }
        }

        // 重复区
        double y1 = getY(x, isInRepeatRegion);
        double y2 = getY(x, isInRepeatRegion + 1);
        double result = y1;
        if (y1 < y2) {
            result = y2;
        }
        return result;
    }
}
