package com.nhcb.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 모든 setting 값을 담아둔 클래스
 * */
public class StaticConfig {

    public static String dataBaseUrl;
    public static String dbUsername;
    public static String dbPassword;
    public static String fileLocation; // DB에 저장할 파일 저장경로명
    public static String saveDir; // 실제 파일이 저장될 위치 (자기 컴퓨터, 혹은 서버 컴퓨터)

    public static String targetURI;

    // DB table, column data setting
    public static String tableName;
    public static ArrayList<String> columnNameList;

    public static String commandFilePath;
    public static String commandFileName;

}
