package Util.FileUtil;

import Util.TimeUtil.TimeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    /**
     * 打开文件
     * @param fileName
     * @return
     */
    public static File openFile(String fileName){
        File file = new File(fileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 格式化返回上一次文件修改时间(2020-01-12 19:50:44)
     * @param file
     * @return
     */
    public static String getLastModified(File file){
        return TimeUtil.getTime_Y_S(file.lastModified());
    }

    /**
     * 保存文件
     * @param fileName
     * @param bytes
     * @param offset
     * @param len
     */
    public static void saveFile(String fileName, byte[] bytes, int offset, int len){
        try {
            File file = openFile(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes, offset, len);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(String fileName, byte[] bytes){
        try {
            File file = openFile(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
