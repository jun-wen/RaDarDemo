package com.helldefender.radardemo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class CarPoint {
    private double ra;//雷达A获取的障碍物距离测最近的距离
    private double rb;//雷达B获取的障碍物距离测最近的距离
    private double rc;
    private double rd;
    private double re;

    private double radarAngle;// 雷达角度θ
    private double placementAngle;// 放置角度α
    private double verticalDistance;// 雷达垂直距离
    private double horizontalDistance_AB;// 雷达A和B的水平距离
    private double horizontalDistance_BC;
    private double horizontalDistance_CD;
    private double horizontalDistance_DE;
    Map<Integer,RaDar> rangemap = new HashMap<Integer, RaDar>();//rangemap[0]表示雷达A和B波的重复区依次类推到Range[3]表示D和E波重复的区域(X轴的区域)
    Map<Integer,RaDar> rangemap2 = new HashMap<Integer, RaDar>();//rangemap[0]表示雷达A和B波的间隔区依次类推到Range[3]表示D和E波重复的区域(X轴的区域)
    private RaDar radarA = new RaDar();// 雷达A
    private RaDar radarB = new RaDar();
    private RaDar radarC = new RaDar();
    private RaDar radarD = new RaDar();
    private RaDar radarE = new RaDar();

    public CarPoint(double ra, double rb, double rc, double rd, double re,
                    double radarAngle, double placementAngle,
                    double verticalDistance, double horizontalDistance_AB,
                    double horizontalDistance_BC, double horizontalDistance_CD,
                    double horizontalDistance_DE) {

        this.ra = ra;
        this.rb = rb;
        this.rc = rc;
        this.rd = rd;
        this.re = re;
        this.radarAngle = radarAngle;
        this.placementAngle = placementAngle;
        this.verticalDistance = verticalDistance;
        this.horizontalDistance_AB = horizontalDistance_AB;
        this.horizontalDistance_BC = horizontalDistance_BC;
        this.horizontalDistance_CD = horizontalDistance_CD;
        this.horizontalDistance_DE = horizontalDistance_DE;

        setXRange();//初始化每个雷达的区间
        getOverlappingRange();//获取雷达弧段之间交叉区对应的横坐标X范围
    }
    private void setXRange(){

        setXRangeA();//设置A雷达X的区间
        setXRangeB();//设置B雷达X的区间
        setXRangeC();//设置C雷达X的区间
        setXRangeD();//设置D雷达X的区间
        setXRangeE();//设置E雷达X的区间
    }
    private void setXRangeA(){
        radarA.setStart(-horizontalDistance_CD-horizontalDistance_DE-ra*Math.sin(radarAngle+placementAngle));
        radarA.setEnd(-horizontalDistance_CD-horizontalDistance_DE-ra*Math.sin(placementAngle));
    }
    private void setXRangeB(){
        radarB.setStart(-horizontalDistance_CD-rb*Math.sin(radarAngle/2));
        radarB.setEnd(-horizontalDistance_CD+rb*Math.sin(radarAngle/2));
    }
    private void setXRangeC(){
        radarC.setStart(-rc*Math.sin(radarAngle/2));
        radarC.setEnd(rc*Math.sin(radarAngle/2));

    }
    private void setXRangeD(){
        radarD.setStart(horizontalDistance_CD-rd*Math.sin(radarAngle/2));
        radarD.setEnd(horizontalDistance_CD+rd*Math.sin(radarAngle/2));
    }
    private void setXRangeE(){
        radarE.setStart(horizontalDistance_CD+horizontalDistance_DE+re*Math.sin(placementAngle));
        radarE.setEnd(horizontalDistance_CD+horizontalDistance_DE+re*Math.sin(placementAngle+radarAngle));
    }

    private double getYFromRadarA(double x){
        return -Math.sqrt(Math.pow(ra, (double)2) - Math.pow(x+horizontalDistance_CD+horizontalDistance_DE, (double)2)) + verticalDistance;
    }
    private double getYFromRadarB(double x){
        return -Math.sqrt(Math.pow(rb, (double)2) - Math.pow(x+horizontalDistance_CD, (double)2));
    }
    private double getYFromRadarC(double x){
        return -Math.sqrt(Math.pow(rc, (double)2) - Math.pow(x, (double)2));
    }
    private double getYFromRadarD(double x){
        return -Math.sqrt(Math.pow(rd, (double)2) - Math.pow(x-horizontalDistance_CD, (double)2));
    }
    private double getYFromRadarE(double x){
        return -Math.sqrt(Math.pow(re, (double)2) - Math.pow(x-horizontalDistance_CD-horizontalDistance_DE, (double)2)) + verticalDistance;
    }

    //判断x是否在给定的范围内
    private boolean isInRanges(double x, RaDar rg){
        if(x<rg.getStart() || x >rg.getEnd()){
            return false;
        }
        return true;
    }
    //获取雷达弧段之间交叉区对应的横坐标X范围 或者x不连续的区间
    private void getOverlappingRange(){
        if(this.radarA.getEnd() > this.radarB.getStart()){
            RaDar temp = new RaDar();
            temp.setStart(this.radarB.getStart());
            temp.setEnd(this.radarA.getEnd());
            rangemap.put(0,temp);
        }else if(this.radarA.getEnd() != this.radarB.getStart()){
            RaDar temp = new RaDar();
            temp.setStart(this.radarA.getEnd());
            temp.setEnd(this.radarB.getStart());
            rangemap2.put(0,temp);
        }

        if(this.radarB.getEnd() > this.radarC.getStart()){
            RaDar temp = new RaDar();
            temp.setStart(this.radarC.getStart());
            temp.setEnd(this.radarB.getEnd());
            rangemap.put(1,temp);
        }else if(this.radarB.getEnd() != this.radarC.getStart()){
            RaDar temp = new RaDar();
            temp.setStart(this.radarB.getEnd());
            temp.setEnd(this.radarC.getStart());
            rangemap2.put(1,temp);
        }

        if(this.radarC.getEnd() > this.radarD.getStart()){
            RaDar temp = new RaDar();
            temp.setStart(this.radarD.getStart());
            temp.setEnd(this.radarC.getEnd());
            rangemap.put(2,temp);
        }else if(this.radarC.getEnd() != this.radarD.getStart()){
            RaDar temp = new RaDar();
            temp.setStart(this.radarC.getEnd());
            temp.setEnd(this.radarD.getStart());
            rangemap2.put(2,temp);
        }

        if(this.radarD.getEnd() > this.radarE.getStart()){
            RaDar temp = new RaDar();
            temp.setStart(this.radarE.getStart());
            temp.setEnd(this.radarD.getEnd());
            rangemap.put(3,temp);
        }else if(this.radarD.getEnd() != this.radarE.getStart()){
            RaDar temp = new RaDar();
            temp.setStart(this.radarD.getEnd());
            temp.setEnd(this.radarE.getStart());
            rangemap2.put(3,temp);
        }
    }

    //获取非重复区的Y值
    private double getNonRepeatRegionY(
            double x, //横坐标x的值
            int cadar) throws Exception{//cadar表示所在的雷达标识在0到4之间
        double y = 0.0;
        switch(cadar){
            case 0:
                y = getYFromRadarA(x);
                break;
            case 1:
                y = getYFromRadarB(x);
                break;
            case 2:
                y = getYFromRadarC(x);
                break;
            case 3:
                y = getYFromRadarD(x);
                break;
            case 4:
                y = getYFromRadarE(x);
                break;
            default:
                throw new Exception("错误的雷达标识");
        }
        return y;
    }

    //返回X所在的雷达区域
    private int getCadarRg(double x){
        if(isInRanges(x,this.radarA)){
            return 0;
        }else if(isInRanges(x,this.radarB)){
            return 1;
        }else if(isInRanges(x,this.radarC)){
            return 2;
        }else if(isInRanges(x,this.radarD)){
            return 3;
        }else if(isInRanges(x,this.radarE)){
            return 4;
        }
        return -1;
    }

    //重叠取点的优化
    private double optimizationYCore(
            RaDar rg,//重叠区域X的范围
            int cadar1,//重叠雷达1标识
            int cadar2,//重叠雷达2标识
            double x) throws Exception{//横坐标点X的值
        return ((x-rg.getStart())/(rg.getEnd()-rg.getStart())) * getNonRepeatRegionY(x,cadar1)
                + (1-(x- rg.getStart())/(rg.getEnd()-rg.getStart())) * getNonRepeatRegionY(x,cadar2);
    }

    private double optimizationY(
            int pst,//重叠区域标识
            double x) throws Exception{//坐标点X的值
        double y = 0.0;
        switch(pst){
            case 0:
                y = optimizationYCore(rangemap.get(0),0,1,x);
                break;
            case 1:
                y = optimizationYCore(rangemap.get(1),1,2,x);
                break;
            case 2:
                y = optimizationYCore(rangemap.get(2),2,3,x);
                break;
            case 3:
                y = optimizationYCore(rangemap.get(3),3,4,x);
                break;
            default:
                throw new Exception("无效重叠区域标识");
        }
        return y;
    }
    //间隔区点的优化
    private double optimizationSeptalAreaYCore(
            RaDar rg,//间隔区域X的范围
            int cadar1,//间隔雷达1标识
            int cadar2,//间隔雷达2标识
            double x) throws Exception{//横坐标点X的值

        double starty = getNonRepeatRegionY(rg.getStart(),cadar1);
        double endy = getNonRepeatRegionY(rg.getEnd(),cadar2);
        double maxy = Math.max(starty, endy);
        double distancey = Math.abs(endy - starty);
        double distancex = Math.abs(rg.getStart() - rg.getEnd());

//		return maxy - (Math.abs((x - rg.getStart())/distancex)*distancey);
        return starty*(1-Math.abs((x - rg.getStart())/distancex))+endy*(Math.abs((x - rg.getStart())/distancex));
    }

    private double optimizationSeptalAreaY(
            int pst,//间隔区域标识
            double x) throws Exception{//坐标点X的值
        double y = 0.0;
        switch(pst){
            case 0:
                y = optimizationSeptalAreaYCore(rangemap2.get(0),0,1,x);
                break;
            case 1:
                y = optimizationSeptalAreaYCore(rangemap2.get(1),1,2,x);
                break;
            case 2:
                y = optimizationSeptalAreaYCore(rangemap2.get(2),2,3,x);
                break;
            case 3:
                y = optimizationSeptalAreaYCore(rangemap2.get(3),3,4,x);
                break;
            default:
                throw new Exception("无效重叠区域标识");
        }
        return y;
    }

    //获取整个取钱的x的取值空间
    public RaDar getRangeAll(){
        RaDar rg = new RaDar();
        rg.setStart(this.radarA.getStart());
        rg.setEnd(this.radarE.getEnd());
        return rg;
    }

    public double caculate(double x) throws Exception {

        int isInRepeatRegion = -1;//-1表示不再重复区，其他表示所在重复区
        int isInSeptalArea = -1;//-1表示不再间隔区，其他表示所在重复区
        int cadarX = -1;//表示X 在哪个雷达管辖区域

        for (Map.Entry<Integer, RaDar> entry : rangemap.entrySet()) {
            if(isInRanges(x, entry.getValue())){
                isInRepeatRegion = entry.getKey();
            }
        }
        for (Map.Entry<Integer, RaDar> entry : rangemap2.entrySet()) {
            if(isInRanges(x, entry.getValue())){
                isInSeptalArea = entry.getKey();
            }
        }

        //判断x是否在重复区
        if(isInRepeatRegion == -1){
            cadarX = getCadarRg(x);

            if(cadarX != -1){
                return getNonRepeatRegionY(x,cadarX);
            }

            if(cadarX == -1 && isInSeptalArea != -1){//如果没有找到X所在的雷达区域/暂时没有处理该情况
                return optimizationSeptalAreaY(isInSeptalArea,x);
            }else{
                throw new Exception("X 不在该雷达群的管辖范围内");
            }
        }

        return optimizationY(isInRepeatRegion,x);
    }

    public static void main(String[] args) throws FileNotFoundException {
        // TODO Auto-generated method stub

        //测试
        //第一条线
        CarPoint cp = new CarPoint(34, 25, 24, 30, 35, Math.PI/4, Math.PI/7,10, 10, 10, 10, 10);
        RaDar rg = cp.getRangeAll();
        double end = rg.getEnd();
        double h = 15.0;//要展示扇形的固定长度

        //第二线
        CarPoint cp2 = new CarPoint(34+h, 25+h, 24+h, 30+h, 35+h, Math.PI/4, Math.PI/7,10, 10, 10, 10, 10);
        RaDar rg2 = cp2.getRangeAll();
        double end2 = rg2.getEnd();

        FileOutputStream out = new FileOutputStream("C:/Users/wenjun/Desktop/result.txt");//自己设置文件位置
        PrintStream print =new PrintStream(out);
        try{
            for(double i = rg.getStart(); i<end; i+=2){
                print.println(i);
                System.out.print(i);
                System.out.print(" ");
            }

            System.out.println();
            print.println();
            for(double i = rg.getStart(); i<end; i+=2){
                double temp = cp.caculate(i);
                print.println(temp);
                System.out.print(temp);
                System.out.print(" ");
            }


            //第二条线也就是底线 ，通过设置展示扇形的固定长度来去出
            System.out.println("第二条线的点集展示区");
            print.println("第二条线的点集展示区");


            for(double i = rg2.getStart(); i<end2; i+=2){
                print.println(i);
                System.out.print(i);
                System.out.print(" ");
            }
            print.println();
            System.out.println();
            for(double i = rg2.getStart(); i<end2; i+=2){
                double temp = cp2.caculate(i);
                print.println(temp);
                System.out.print(temp);
                System.out.print(" ");
            }

            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{

        }
    }
}
