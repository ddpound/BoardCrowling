package com.nhcb.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public class YamlConfig {

    public YamlConfig() {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getResourceAsStream("/application.yml")) {
            Map<String, Object> config = yaml.load(in);

            // DB 세팅
            Map<String, String> dbConfig = (Map<String, String>) config.get("database");
            StaticConfig.dataBaseUrl = dbConfig.get("url");
            StaticConfig.dbUsername = dbConfig.get("username");
            StaticConfig.dbPassword = dbConfig.get("password");

            // 파일 세팅
            Map<String, String> fileConfig = (Map<String, String>) config.get("file");
            StaticConfig.dataBaseUrl = fileConfig.get("fileLocation");
            StaticConfig.dbUsername = fileConfig.get("saveDir");

            // 사이트 세팅
            Map<String, String> targetURICOnfig = (Map<String, String>) config.get("site");
            StaticConfig.targetURI = targetURICOnfig.get("targetURI");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
