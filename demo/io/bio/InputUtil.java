package com.github.utils.io;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Author: Zer01ne
 * @Date: 2019/3/1 10:04
 * @Version 1.0
 */
public class InputUtil {

    //从键盘读
    public static final BufferedReader KEYWORD_READER = new BufferedReader(new InputStreamReader(System.in));

    private InputUtil(){}
    /**
     * 从键盘读取字符串
     * @param keyword :
     * @return java.lang.String
     * @throws
     */
    public static String getString(String keyword){

        String str = null;
        //读写开关
        boolean flag = true;
        while (flag){
            System.out.println(keyword);
            try {
                str = KEYWORD_READER.readLine();
                if (StringUtils.isBlank(str)){
                    System.out.println("输入不能为空!");
                }else {
                    flag = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return str;
    }
}
