package com.nhcb.config;

/**
 * 타겟이 될 uri를 분석하고 유동적으로 변환해서 사용할 수 있도록 하는 클래스
 *
 * */
public class QueryStringConverter {




    public String findTarget(String uri){

        if(uri.contains("${")){
            // 변수 값이 있으니 실행

            for (int i = 0; i < uri.length(); i++) {
                uri.substring(i, i + 1);

            }

        }


        return null;
    }
}
