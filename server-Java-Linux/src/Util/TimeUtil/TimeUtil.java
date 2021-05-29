package Util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 返回标准时间格式
 */
public class TimeUtil {

    /**
     * 占位符：
     *
     * G　　"公元"
     * y　　四位数年份
     * M　　月
     * d　　日
     * h　　时 在上午或下午 (1~12)
     * H　　时 在一天中 (0~23)
     * m　　分
     * s　　秒
     * S　　毫秒
     *
     *
     * E　　一周中的周几
     * D　　一年中的第几天
     * w　　一年中第几个星期
     * a　　上午 / 下午 标记符
     * k 　　时(1~24)
     * K 　   时 在上午或下午 (0~11)
     */
    public final static String PAT_Y_S = "yyyy-MM-dd HH:mm:ss";
    public final static String PAT_MS = "HH:mm:ss.SSS";

    public final static SimpleDateFormat df_Y_S = new SimpleDateFormat(PAT_Y_S);
    public final static SimpleDateFormat df_MS = new SimpleDateFormat(PAT_MS);

    /**
     * 返回 年月日 时分秒(2021-02-09 17:14:31)
     * @return
     */
    public static String getTime_Y_S(){
        return df_Y_S.format(new Date());
    }

    /**
     * 返回 时分秒 毫秒(17:14:31.111)
     * @return
     */
    public static String getTime_MS(){
        return df_MS.format(new Date());
    }

    /**
     * 返回 年月日 时分秒(2021-02-09 17:14:31)
     * @return
     */
    public static String getTime_Y_S(Object o){
        return df_Y_S.format(o);
    }

    /**
     * 返回 时分秒 毫秒(17:14:31.111)
     * @return
     */
    public static String getTime_MS(Object o){
        return df_MS.format(o);
    }



}
