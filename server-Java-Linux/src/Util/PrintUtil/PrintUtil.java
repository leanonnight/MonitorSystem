package Util.PrintUtil;

import Util.TimeUtil.TimeUtil;

public class PrintUtil {

    /**
     * 不换行打印
     * @param s
     */
    public static void print(String s){
        System.out.print(s);
    }

    /**
     * 换行打印
     * @param s
     */
    public static void printIn(String s){
        System.out.println(s);
    }

    /**
     * 换行打印 + 年月日时分秒
     * @param s
     */
    public static void printIn_Y_S(String s){
        System.out.println(TimeUtil.getTime_Y_S() + " : " + s);
    }

    /**
     * 换行打印 + 时分秒毫秒
     * @param s
     */
    public static void printIn_MS(String s){
        System.out.println(TimeUtil.getTime_MS() + " : " + s);
    }
}
