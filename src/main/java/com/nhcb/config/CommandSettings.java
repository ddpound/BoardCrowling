package com.nhcb.config;

import com.nhcb.model.CommandModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * command setting : 특정한 명령어 읽기 클래스
 *
 * @author youseongjung
 * @apiNote
 * 1. targetURI ( ${} 의 개수당 target1 별로 이름 지정해주어서 개수를 맞춰야함 ${} 가 2개면 target1, target2  으로 지정)
 * 2.
 * */
public class CommandSettings {

    public void readCommands(CommandModel commandModel){
        try {
            List<String> lines = Files.readAllLines(Paths.get(StaticConfig.commandFilePath).resolve(StaticConfig.commandFileName));
            for (String line : lines) {

                // : 를 기준으로, 라인의 첫번째 : 를 이후로 문자열 추출
                System.out.println("command line " + line);
                int subStringIndex = 0;

                // find :
                for (int i = 0; i < line.length(); i++) {
                    if(line.charAt(i) == ':'){
                        subStringIndex = i;
                    }

                }

                String command = line.substring(subStringIndex);
                String[] split = line.split(":");

                if(split.length == 0){
                    System.out.println("not found command");
                }

                // targetUrI 일때
                if(split[0].equals("targetURI")){

                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
