package com.nhcb.manager;

import com.nhcb.config.YamlConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private Connection connection;

    public DatabaseManager() {
        YamlConfig config = new YamlConfig();
        try {
            connection = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertPost(String tableName, String cmmntyId, String title, String date, String content) {
        String sql = "INSERT INTO " + tableName
                + " (cmmnty_id , cnn_no , ntt_sbjt, date, ntt_cntn, pstg_trg, rgst_id, reg_dt, updt_dt, del_yn) "
                + "VALUES (?, COALESCE((SELECT MAX(ntt_no) + 1 FROM pts_cmmnty_detail_mstr), 1) ,?, ?, ?, 1, 'SYSTEM', now(), now(), '0')";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cmmntyId);
            pstmt.setString(2, title);
            pstmt.setString(3, date);
            pstmt.setString(4, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String generateInsertSQL(String title, String author, String date, String views, String content) {
        return String.format("INSERT INTO posts (title, author, date, views, content) VALUES ('%s', '%s', '%s', '%s', '%s');",
                title, author, date, views, content);
    }

    public void exportInsertSQLToFile(String filePath, String sql) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(sql + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
