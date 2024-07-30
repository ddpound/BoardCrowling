package com.nhcb;

import com.nhcb.manager.DatabaseManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeOptions;
import org.yaml.snakeyaml.Yaml;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("프로그램 시작");
        BoardCrowling("https://www.ispf.or.kr/notice/106");

//        for (int i = 90; i < 112; i++) {
//            BoardCrowling("https://www.ispf.or.kr/notice/" + i);
//        }
    }

    public static void BoardCrowling(String url) {

        DatabaseManager dbManager = new DatabaseManager();
        boolean directInsert = true; // true면 직접 삽입, false면 SQL 파일로 내보내기
        String outputFilePath = "insert_statements.sql";
        int count = 0;

        try {
            Document doc = Jsoup.connect(url).get();

            if(isPageNotFound(doc)){
                System.out.println("글이 없습니다.");
                System.out.println("없는 글 url : " + url);
            }else{
                Elements posts = doc.select("#bo_v"); // 게시물 리스트 선택자
                System.out.println("=========================================================");
                System.out.println("크롤링 시작");
                System.out.println("접근 URL");

                for (Element post : posts) {
                    count++;
                    String category = post.select(".bview_cate").text();
                    String title = post.select(".bo_v_tit").text();
                    Element contentElement = post.select("#bo_v_con").first();
                    Elements writerInfoElements = post.select("div.writer_info .txt");

                    // '#bo_v_con' 요소의 자식들만 추출
                    Elements childElements = contentElement.children();

                    // 또는 자식 요소들 HTML을 하나의 문자열로 연결
                    StringBuilder contentHtml = new StringBuilder();
                    for (Element child : childElements) {
                        contentHtml.append(child.outerHtml());
                    }

                    // IMG 요소 처리
                    if (contentElement != null) {
                        Elements imgElements = contentElement.select("img");
                        for (Element imgElement : imgElements) {
                            String imgUrl = imgElement.attr("src");
                            String fileExtension = getFileExtension(imgUrl);
                            String newFileName = UUID.randomUUID() + "." + fileExtension;

                            // 이미지 태그 src 속성 수정
                            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

                            // 이미지 다운로드
                            String outputDir = "/WebUpload/ispf/" + today;

                            // 폴더가 없으면 생성
                            createDirectoryIfNotExists(outputDir);


                            downloadImage(imgUrl, outputDir + "/"+ newFileName);

                            imgElement.attr("src", "/ispf/data/editor/" + today + "/" + newFileName + "/show.ajax");
                        }
                    }
                    String content = contentElement != null ? contentElement.html() : "";
                    String escapedTitle = StringEscapeUtils.escapeHtml4(title); // HTML 내용 이스케이프
                    String escapedContent = StringEscapeUtils.escapeHtml4(String.valueOf(contentHtml)); // HTML 내용 이스케이프

                    // 날짜 요소를 가져옴
                    String date = writerInfoElements.get(1).text();
                    int inqCnt = Integer.parseInt(writerInfoElements.get(2).text()); // 조회수

                    /**
                     * 파일 리스트 추출
                     * fileslist class 중에
                     * https://www.ispf.or.kr/ 로 시작하는 링크가 있으면 다운로드 파일 생성해주자
                     * file root 경로에서 YYYYMMDD 형식 안에 파일이 들어감
                     * */
                    // 'fileslist' 클래스명을 가진 요소 찾기
                    Elements filesListElements = doc.getElementsByClass("fileslist");
                    List<String> fileList = new ArrayList<>(); // 다운로드 리스트
                    List<String> relatedLinks = new ArrayList<>(); // 관련링크

                    // 각 'fileslist' 요소의 자식 'a' 태그를 찾고 href 추출
                    for (Element filesListElement : filesListElements) {
                        Elements anchorElements = filesListElement.select("a");
                        for (Element anchor : anchorElements) {
                            String href = anchor.attr("href"); // href 속성 추출
                            if(href.startsWith("https://www.ispf.or.kr/bbs/download")) fileList.add(href); // href를 리스트에 추가
                            else relatedLinks.add(href); // 관련링크
                        }
                    }

                    String fileGroupId = null;

                    // 추출한 href 출력
                    if(!fileList.isEmpty() && directInsert) {
                        System.out.println("파일 다운로드 감지");
                        fileGroupId = DatabaseManager.getSeqFileNumber();
                        DatabaseManager.downloadAllFiles(url,fileGroupId);
                    }


                    if (directInsert) {
                        String categoryNumber = "";
                        if(category.equals("공고")) categoryNumber = "2024030001";
                        if(category.equals("알림")) categoryNumber = "2024030002";
                        if(category.equals("보도자료")) categoryNumber = "2024030003";

                        // 데이터베이스에 삽입
                        dbManager.insertPost("pts_cmmnty_detail_mstr",
                                categoryNumber,
                                escapedTitle,
                                date,
                                escapedContent,
                                relatedLinks,
                                fileList,
                                fileGroupId,
                                inqCnt);
                    } else {
                        // SQL INSERT 문 생성 및 파일로 내보내기
                        //String insertSQL = dbManager.generateInsertSQL(category, title, date, content);
                        //dbManager.exportInsertSQLToFile(outputFilePath, insertSQL);
                    }

                    System.out.println("Category: " + category);
                    System.out.println("Title: " + title);
                    System.out.println("Date: " + date);
                    System.out.println("Content: " + content);
                    System.out.println("count " + count);
                }
            }
            System.out.println("크롤링 섹션 종료");
            System.out.println("===================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isPageNotFound(Document doc) {
        // 페이지에 "글이 존재하지 않습니다" 메시지가 포함된 경우 처리
        Elements scriptElements = doc.select("script");
        for (Element script : scriptElements) {
            if (script.toString().contains("alert(\"글이 존재하지 않습니다.\"")) {
                return true;
            }
        }
        Elements noScriptElements = doc.select("noscript");
        for (Element noScript : noScriptElements) {
            if (noScript.toString().contains("글이 존재하지 않습니다.")) {
                return true;
            }
        }
        return false;
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


    private static String getFileExtension(String url) {
        return url.substring(url.lastIndexOf('.') + 1);
    }

    private static void downloadImage(String imgUrl, String destinationFile) throws IOException {
        try (InputStream in = new URL(imgUrl).openStream()) {
            Files.copy(in, Paths.get(destinationFile));
        }
    }

    private static void createDirectoryIfNotExists(String dirPath) throws IOException {
        Files.createDirectories(Paths.get(dirPath));
    }
}