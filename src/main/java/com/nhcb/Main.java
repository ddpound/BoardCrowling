package com.nhcb;

import com.nhcb.manager.DatabaseManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

public class Main {
    public static void main(String[] args) {
        System.out.println("프로그램 시작");
        String url = "https://www.ispf.or.kr/notice/94";
        DatabaseManager dbManager = new DatabaseManager();
        boolean directInsert = true; // true면 직접 삽입, false면 SQL 파일로 내보내기
        String outputFilePath = "insert_statements.sql";
        int count = 0;

        try {
            Document doc = Jsoup.connect(url).get();
            Elements posts = doc.select("#bo_v"); // 게시물 리스트 선택자

            System.out.println("크롤링 시작");

            for (Element post : posts) {
                count++;
                String category = post.select(".bview_cate").text();
                String title = post.select(".bo_v_tit").text();
                Element contentElement = post.select("#bo_v_con").first();
                Elements writerInfoElements = post.select("div.writer_info .txt");

                // IMG 요소 처리
                if (contentElement != null) {
                    Elements imgElements = contentElement.select("img");
                    for (Element imgElement : imgElements) {
                        String imgUrl = imgElement.attr("src");
                        String base64Image = convertImageToBase64("https://www.ispf.or.kr/data/editor/",imgUrl);
                        imgElement.attr("src", "data:image/png;base64," + base64Image);
                    }
                }
                String content = contentElement != null ? contentElement.html() : "";
                String escapedContent = StringEscapeUtils.escapeHtml4(content); // HTML 내용 이스케이프

                // 날짜 요소를 가져옴
                String date = writerInfoElements.get(1).text();;

                if (directInsert) {
                    String categoryNumber = "";
                    if(category.equals("공고")) categoryNumber = "2024030001";
                    if(category.equals("알림")) categoryNumber = "2024030002";
                    if(category.equals("보도자료")) categoryNumber = "2024030003";
                    // 데이터베이스에 삽입
                    dbManager.insertPost("pts_cmmnty_detail_mstr", categoryNumber, title, date, content);
                } else {
                    // SQL INSERT 문 생성 및 파일로 내보내기
                    //String insertSQL = dbManager.generateInsertSQL(category, title, date, content);
                    //dbManager.exportInsertSQLToFile(outputFilePath, insertSQL);
                }

                System.out.println("Category: " + category);
                System.out.println("Title: " + title);
                System.out.println("Date: " + date);
                System.out.println("Content: " + escapedContent);
                System.out.println("count " + count);
                System.out.println("===================================");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String convertImageToBase64(String baseUrl, String imageUrl) {
        try {
            // 상대 경로인 경우 baseUrl을 추가하여 절대 경로로 변환
            URL url = new URL(new URL(baseUrl), imageUrl);
            BufferedImage image = ImageIO.read(url);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}