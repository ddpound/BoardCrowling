package com.nhcb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhcb.model.CommandModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        // ObjectMapper 인스턴스를 생성합니다
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File inputJsonFile = new File(String.valueOf(Paths.get(StaticConfig.commandFilePath).resolve(StaticConfig.commandFileName)));
            CommandModel jacksonCommandModel = objectMapper.readValue(inputJsonFile, CommandModel.class);

            System.out.println("json file read : " + jacksonCommandModel);

            ArrayList<Object> targetList = jacksonCommandModel.getTargetList();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
