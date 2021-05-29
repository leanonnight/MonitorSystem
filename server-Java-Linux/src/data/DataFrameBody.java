package data;

/**
 * 数据结构 帧体(body)
 */
public class DataFrameBody {

    /**
     * 关键字结构
     */
    //帧体(body)
    byte keyNameLen = 0;   //关键字长度
    String key = "";       //关键字
    int valueLen = 0;      //关键字内容长度
    byte[] value = null;   //关键字内容


    /**
     * 辅助字段
     */
    int keyLen = 0;        //整个关键字长度


    /**
     * 构造函数
     * @param key
     * @param value
     */
    DataFrameBody(String key, byte[] value){
        this.key = key;
        this.value = value;
        this.keyNameLen = (byte)key.length();
        this.valueLen = value.length;
        keyLen = getKeyLen();
    }

    /**
     * 获得整个关键字长度
     * @return
     */
    int getKeyLen(){
        keyLen = 1 + keyNameLen + 4 + valueLen;
        return keyLen;
    }
}
