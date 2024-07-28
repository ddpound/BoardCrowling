package com.nhcb.manager;

import com.nhcb.config.YamlConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DatabaseManager {

    private static Connection connection;

    private static String saveFilePath;

    private static String saveDir;

    // WebDriver 초기화
    private static WebDriver driver;

    public DatabaseManager() {
        YamlConfig config = new YamlConfig();
        try {
            connection = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
            Yaml yaml = new Yaml();
            try (InputStream in = getClass().getResourceAsStream("/application.yml")) {
                Map<String, Object> configData = yaml.load(in);
                Map<String, String> fileConfig = (Map<String, String>) configData.get("file");
                this.saveFilePath = fileConfig.get("fileloc");
                this.saveDir = fileConfig.get("saveDir");
                System.out.println("file config check : " + saveFilePath);
                System.out.println("file config check : " + saveDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertPost(String tableName,
                           String cmmntyId,
                           String title,
                           String date,
                           String content,
                           List<String> linkList,
                           List<String> fileList,
                           String fileGroupId,
                           int inqCnt) {
        // 추출
        String datePart = date.substring(0, 8); // "24-04-23"

        // 변환을 위한 포맷터 정의
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // LocalDate 객체로 변환
        LocalDate changeDate = LocalDate.parse(datePart, inputFormatter);

        // 원하는 형식으로 변환
        String formattedDate = changeDate.format(outputFormatter);

        // 현재 날짜와 1년 뒤 날짜를 LocalDate 객체로 얻기
        LocalDate today = LocalDate.now();
        LocalDate oneYearLater = today.plusYears(1);

        // YYYYMMDD 형식으로 변환
        String pstgBgngDate = today.format(outputFormatter);
        String pstgEndDate = oneYearLater.format(outputFormatter);

        String link1 = null;
        String link2 = null;

        // 링크 리스트 추출
        if(linkList != null && !linkList.isEmpty()){
            link1 = linkList.get(0) != null ? linkList.get(0) : null;
            if(linkList.size() > 1) link2 = linkList.get(1) != null ? linkList.get(0) : null;
        }

        // 파일이 있다는 의미니 파일 업로드진행
        if(fileList != null && !fileList.isEmpty()){

        }

        String sql = "INSERT INTO " + tableName
                + " (cmmnty_id , ntt_no , ntt_sbjt, reg_ymd, ntt_cntn, pstg_trgt, ntcr_id ,rgst_id,updn_id , reg_dt, updt_dt, del_yn, pstg_bgng_date, pstg_end_date, link_cntn_1, link_cntn_2, atch_file_group_id, ntcr_nm, inq_cnt) "
                + "VALUES (?, COALESCE((SELECT MAX(ntt_no) + 1 FROM pts_cmmnty_detail_mstr), 1) ,?, ?, ?, 1, 'SYSTEM','SYSTEM', 'SYSTEM', now(), now(), '0', ?, ?, ? ,? , ?, 'SYSTEM', ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cmmntyId);
            pstmt.setString(2, title);
            pstmt.setString(3, formattedDate);
            pstmt.setString(4, content);
            pstmt.setString(5, pstgBgngDate);
            pstmt.setString(6, pstgEndDate);
            pstmt.setString(7, link1);
            pstmt.setString(8, link2);
            pstmt.setString(9, fileGroupId);
            pstmt.setInt(10, inqCnt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 파일 그룹 아이디 시퀸스를 따옴 */
    public static String getSeqFileNumber() {
        String seqFileNumber = null;
        String sql = "SELECT TO_CHAR(NOW(), 'YYYYMMDD') || LPAD((SELECT NEXTVAL('PTS_SEQ_FILE_ID'))::TEXT, 3, '0') AS file_id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                seqFileNumber = rs.getString("file_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return seqFileNumber;
    }

    public static void downloadAllFiles(String pageUrl,String fileGroupId) throws IOException {
        WebDriver driver = null;
        try {
            // WebDriverManager를 사용하여 ChromeDriver 설정
            WebDriverManager.chromedriver().setup();

            // Chrome 옵션 설정
            ChromeOptions options = new ChromeOptions();
            // 다운로드 경로 설정
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("download.default_directory", saveDir+saveFilePath);
            prefs.put("download.prompt_for_download", false);
            prefs.put("download.directory_upgrade", true);
            prefs.put("safebrowsing.enabled", true);
            options.setExperimentalOption("prefs", prefs);

            // 기타 Chrome 옵션 설정
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--headless"); // headless 모드 (브라우저 UI 없이 실행)

            driver = new ChromeDriver(options);

            // 페이지로 이동
            driver.get(pageUrl);

            // 다운로드 링크들 찾기
            List<WebElement> downloadLinks = driver.findElements(By.cssSelector("a.view_file_download"));

            // 파일 그룹 채번
            int sersNo = 1;
            // 각 링크 클릭하여 다운로드
            for (WebElement downloadLink : downloadLinks) {

                String href = downloadLink.getAttribute("href");
                System.out.println("다운로드 링크 클릭: " + href);
                downloadLink.click();

                String originalFileName = downloadLink.getText();
                // 새로운 파일 이름 생성 (UUID 사용)

                // 파일 다운로드 대기 (필요에 따라 시간 조정)
                Thread.sleep(1000);

                // 다운로드된 파일 확인
                File downloadedFile = getLatestFileFromDir(saveDir+saveFilePath);
                if (downloadedFile != null) {
                    // UUID를 사용하여 새 파일 이름 생성
                    String newFileName = generateRandomFileName(originalFileName);
                    File renamedFile = new File(saveDir+saveFilePath + newFileName);

                    // 파일 이름 변경
                    if (downloadedFile.renameTo(renamedFile)) {
                        System.out.println("원본 파일 이름 1 : " + originalFileName);
                        System.out.println("파일 다운로드가 완료되었습니다 2 : " + renamedFile.getName());

                        // 이름 변경에 성공했으면 DB에 저장하도록하자
                        insertFileInfomation(fileGroupId,sersNo,renamedFile.getName(),originalFileName,"SYSTEM","SYSTEM");

                        sersNo++; // 저장후 채번 증가
                    } else {
                        System.out.println("파일 이름 변경에 실패했습니다.");
                    }
                } else {
                    System.out.println("파일 다운로드에 실패했습니다.");
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt(); // InterruptedException 발생 시 현재 스레드 상태를 복구
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }



    // 다운로드된 파일 중 가장 최근 파일을 반환하는 메서드
    private static File getLatestFileFromDir(String dirPath) {
        Path dir = Paths.get(dirPath);
        try {
            return Files.list(dir)
                    .filter(f -> !Files.isDirectory(f))
                    .max((f1, f2) -> Long.compare(f1.toFile().lastModified(), f2.toFile().lastModified()))
                    .map(Path::toFile)
                    .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 원래 파일 이름을 기반으로 랜덤한 파일 이름 생성
    private static String generateRandomFileName(String originalFileName) {
        // 파일 확장자 추출
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex); // 파일 확장자
        }

        // 랜덤 UUID 생성 및 확장자 추가
        String randomFileName = UUID.randomUUID().toString() + extension;
        return randomFileName;
    }

    /**
     * 파일 정보 저장하는 곳
     * file information data save
     * */
    public static void insertFileInfomation(String ATCH_FILE_GROUP_ID,
                                            int SERS_NO,
                                            String STRE_FILE_NM,
                                            String ORGINL_FILE_NM,
                                            String RGST_ID,
                                            String UPDN_ID){

        String pathInfo = saveFilePath;
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        pathInfo = pathInfo + now.format(formatter);


        String sql = "INSERT INTO PTS_FILE_MNG_MSTR"
                + " (ATCH_FILE_GROUP_ID , SERS_NO , FILE_LOC, STRE_FILE_NM, ORGINL_FILE_NM, REG_DT, RGST_ID ,UPDT_DT,UPDN_ID) "
                + "VALUES (?,?,?,?,?,now(),?,now(),?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ATCH_FILE_GROUP_ID);
            pstmt.setInt(2, SERS_NO);
            pstmt.setString(3, pathInfo);
            pstmt.setString(4, STRE_FILE_NM);
            pstmt.setString(5, ORGINL_FILE_NM);
            pstmt.setString(6, RGST_ID);
            pstmt.setString(7, UPDN_ID);
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
