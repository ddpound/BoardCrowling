package com.nhcb.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class YamlConfig {

    private String url;
    private String username;
    private String password;

    public YamlConfig() {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getResourceAsStream("/application.yml")) {
            Map<String, Object> config = yaml.load(in);
            Map<String, String> dbConfig = (Map<String, String>) config.get("database");
            this.url = dbConfig.get("url");
            this.username = dbConfig.get("username");
            this.password = dbConfig.get("password");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
