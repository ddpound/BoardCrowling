package com.nhcb;


import com.nhcb.config.CommandSettings;
import com.nhcb.config.StaticConfig;
import com.nhcb.config.YamlConfig;
import com.nhcb.model.CommandModel;

/**
 * 메인에서 분리하기 위한 클래스
 * */
public class CrowlingRun {

    public void run(){
        System.out.println("make by youseong");
        System.out.println("program start....");

        System.out.println("setting start");

        new YamlConfig(); // 세팅을 위한 시작

        CommandSettings settings = new CommandSettings();
        CommandModel commandModel = new CommandModel();

        // 패스워드는 보안으로 세팅이 되어있는지 안되어있는지만 체크
        String passwordIs = StaticConfig.dbPassword != null ? "password setting complete" : "password is null";

        System.out.println("DB : " + StaticConfig.dataBaseUrl);
        System.out.println("username : " + StaticConfig.dbUsername);
        System.out.println("password : " + passwordIs);

        System.out.println("file location : " + StaticConfig.fileLocation);
        System.out.println("save dir : " + StaticConfig.saveDir);
        System.out.println("targetURI : " + StaticConfig.targetURI);

        settings.readCommands(commandModel);

        System.out.println("Setting end");
    }


}
