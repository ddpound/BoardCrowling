package com.nhcb.manager;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 파일을 다운로드 할때 경로 및 설정을 담당하는 클래스
 * */
public class FileManager {



    public static void downloadAllFiles(String pageUrl,String fileGroupId) throws IOException {
        WebDriver driver = null;
        try {
            // WebDriverManager를 사용하여 ChromeDriver 설정
            WebDriverManager.chromedriver().setup();

            // Chrome 옵션 설정
            ChromeOptions options = new ChromeOptions();
            // 다운로드 경로 설정
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("download.default_directory", "sampleFilePath"); // 파일경로를 꼭 지정해주세요
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
                File downloadedFile = getLatestFileFromDir("sampleFilePath");
                if (downloadedFile != null) {
                    // UUID를 사용하여 새 파일 이름 생성
                    String newFileName = generateRandomFileName(originalFileName);
                    File renamedFile = new File("sampleFilePath/" + newFileName);

                    // 파일 이름 변경
                    if (downloadedFile.renameTo(renamedFile)) {
                        System.out.println("원본 파일 이름 1 : " + originalFileName);
                        System.out.println("파일 다운로드가 완료되었습니다 2 : " + renamedFile.getName());

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
            // 모든 크롬 프로세스를 강제 종료
            killChromeDriverProcess();
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

    private static void killChromeDriverProcess() {
        try {
            // ChromeDriver 프로세스를 명시적으로 종료
            Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
            Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
