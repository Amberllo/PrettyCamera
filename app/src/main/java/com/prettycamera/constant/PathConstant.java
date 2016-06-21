package com.prettycamera.constant;

/**
 * Created by apple on 16/6/21.
 */
public class PathConstant {

    private final static String Package = "PrettyCamera";
    private static final String Root = android.os.Environment.getExternalStorageDirectory().getPath() + "/"+Package;
    public final static String Image = Root + "/Image";
    public final static String Temp = Root + "/Temp";
}
