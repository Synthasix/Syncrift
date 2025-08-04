package com.hexplatoon.syncrift_backend.service.battle;

import com.hexplatoon.syncrift_backend.dto.battle.Result;
import com.hexplatoon.syncrift_backend.dto.battle.config.Config;
import com.hexplatoon.syncrift_backend.dto.battle.config.CssConfig;
import com.hexplatoon.syncrift_backend.entity.Battle;
import com.hexplatoon.syncrift_backend.entity.Image;
import com.hexplatoon.syncrift_backend.repository.ImageRepository;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;

@Service
public class CssBattleHandlerService {

    private final BattleTimerService battleTimerService;
    private ImageRepository imageRepository;
    private BattleService battleService;

    // Use @Lazy to avoid circular dependencies if BattleService depends on this service
    CssBattleHandlerService(@Lazy BattleService battleService, ImageRepository imageRepository, BattleTimerService battleTimerService) {
        this.battleService = battleService;
        this.imageRepository = imageRepository;
        this.battleTimerService = battleTimerService;
    }

    private final Map<Long, Config> configMap = new ConcurrentHashMap<>();
    // This map stores ONLY the user's typed HTML code, not screenshot paths.
    private final Map<Long, Map<String, String>> userTextMap = new ConcurrentHashMap<>();

    /**
     * Creates and stores a CSS battle configuration based on a random image.
     * @param battleId The ID of the battle.
     * @return The battle configuration object.
     */
    public Config getConfig(Long battleId) {
        Image image = imageRepository.findRandomImage();
        Integer duration = battleService.getActiveBattleById(battleId).getDuration();
        Config config = CssConfig.builder()
                .imageUrl(image.getCloudinaryUrl())
                .duration(duration)
                .colorCode(image.getColorCode())
                .build();
        configMap.put(battleId, config);
        return config;
    }

    /**
     * Saves the HTML/CSS code typed by a user for a specific battle.
     * @param battleId The ID of the battle.
     * @param username The user's username.
     * @param text The HTML/CSS code submitted by the user.
     */
    public void saveUserText(Long battleId, String username, String text) {
        // Ensure the inner map exists for this battleId
        userTextMap.computeIfAbsent(battleId, k -> new ConcurrentHashMap<>())
                .put(username, text);
        if(userTextMap.get(battleId).size() == 2) {
            battleTimerService.cancelBattleTimer(battleId);
            battleService.endBattle(battleId);
        }
    }

    /**
     * Captures a screenshot of the user's submitted HTML/CSS code using a headless browser.
     * @param battleId The ID of the battle.
     * @param username The user's username.
     * @return The absolute file path of the captured screenshot.
     * @throws Exception if screenshot capture fails.
     */
    public String captureScreenshot(Long battleId, String username) throws Exception {
        String htmlContent = userTextMap.get(battleId).get(username);
        if (htmlContent == null || htmlContent.isEmpty()) {
            throw new IllegalArgumentException("No HTML content found for " + username + " in battle " + battleId);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=400,600");
        WebDriver driver = new ChromeDriver(options);

        String screenshotPath = null;
        try {
            // Encode the HTML content to a base64 data URI
            byte[] htmlBytes = htmlContent.getBytes(StandardCharsets.UTF_8);
            String base64Html = Base64.getEncoder().encodeToString(htmlBytes);
            String encodedHtml = "data:text/html;base64," + base64Html;

            driver.get(encodedHtml);

            // Wait a bit to ensure rendering is complete
            Thread.sleep(500);

            // Take the screenshot and get the file
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Create a unique filename using username and battleId
            File savedFile = new File("screenshots/" + username + "_" + battleId + ".png");
            savedFile.getParentFile().mkdirs(); // Create directory if it doesn't exist

            try (FileOutputStream fos = new FileOutputStream(savedFile)) {
                Files.copy(screenshotFile.toPath(), fos);
            }
            screenshotPath = savedFile.getAbsolutePath();

            System.out.println("Screenshot captured for " + username + " at: " + screenshotPath);
            return screenshotPath;

        } catch (Exception e) {
            System.err.println("Error capturing screenshot for " + username + ": " + e.getMessage());
            throw e;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * Downloads an image from a URI (local or web) and returns it as an OpenCV Mat object.
     * @param imageUrl The URI of the image.
     * @return The image as an OpenCV Mat.
     * @throws IOException if the image cannot be downloaded or decoded.
     */
    public Mat downloadImageAsMat(String imageUrl) throws IOException {
        if (imageUrl.startsWith("file://")) {
            String filePath = imageUrl.substring(7);
            Mat image = opencv_imgcodecs.imread(filePath);
            if (image.empty()) {
                throw new RuntimeException("❌ Failed to load image from local path: " + filePath);
            }
            return image;
        } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try (InputStream in = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    byteStream.write(buffer, 0, bytesRead);
                }
            }

            byte[] imageBytes = byteStream.toByteArray();
            BytePointer bytePointer = new BytePointer(imageBytes);
            Mat image = opencv_imgcodecs.imdecode(new Mat(bytePointer), opencv_imgcodecs.IMREAD_COLOR);
            if (image.empty()) {
                throw new RuntimeException("❌ Failed to decode image from URL: " + imageUrl);
            }
            return image;
        } else {
            throw new IllegalArgumentException("Unsupported image URI scheme: " + imageUrl);
        }
    }

    /**
     * Compares a user's screenshot with the target image for a battle.
     * @param battleId The ID of the battle.
     * @param userScreenshotPath The file path of the user's screenshot.
     * @return A similarity score between 0 and 100.
     * @throws IOException if an image file cannot be read.
     */
    public double compareImages(Long battleId, String userScreenshotPath) throws IOException {
        CssConfig config = (CssConfig) configMap.get(battleId);
        if (config == null) {
            throw new IllegalArgumentException("Config not found for battleId: " + battleId);
        }

        // Load the user's screenshot and the target image
        Mat img1 = opencv_imgcodecs.imread(userScreenshotPath);
        if (img1.empty()) {
            throw new RuntimeException("❌ Failed to load user screenshot from path: " + userScreenshotPath);
        }
        Mat img2 = downloadImageAsMat(config.getImageUrl());

        // Convert images to grayscale
        Mat gray1 = new Mat();
        Mat gray2 = new Mat();
        opencv_imgproc.cvtColor(img1, gray1, COLOR_BGR2GRAY);
        opencv_imgproc.cvtColor(img2, gray2, COLOR_BGR2GRAY);

        // Resize images if dimensions don't match
        if (gray1.size().width() != gray2.size().width() || gray1.size().height() != gray2.size().height()) {
            opencv_imgproc.resize(gray2, gray2, gray1.size());
        }

        // Compute the absolute difference between the images
        Mat diff = new Mat();
        opencv_core.absdiff(gray1, gray2, diff);

        // Calculate the mean of the difference to get a simple similarity score
        Scalar mean = opencv_core.mean(diff);
        double similarity = 100 - (mean.get(0)); // Normalize score to be a percentage

        // Release Mat objects to free native memory (crucial to prevent leaks)
        img1.release();
        img2.release();
        gray1.release();
        gray2.release();
        diff.release();

        return Math.max(similarity, 0); // Ensure score is not negative
    }

    /**
     * Calculates the final score for a user, combining image similarity and time taken.
     * @param battleId The ID of the battle.
     * @param userScreenshotPath The file path of the user's screenshot.
     * @return The final weighted score.
     * @throws IOException if image comparison fails.
     */
    public double finalScore(Long battleId, String userScreenshotPath) throws IOException {
        double ssimScore = compareImages(battleId, userScreenshotPath);
        Battle battle = battleService.getActiveBattleById(battleId);
        Integer time = battle.getDuration();

        // Formula to penalize for a longer duration. (30 minutes max)
        double timeTaken = (30.0 * 60.0 - time) / (3.0 * 6.0);
        if (timeTaken < 0) {
            timeTaken = 0;
        }

        // Apply weights
        double ssimWeight = 0.9;
        double timeTakenWeight = 0.1;

        double score = ssimWeight * ssimScore + timeTakenWeight * timeTaken;
        return score;
    }

    /**
     * Determines the winner of the battle by comparing the final scores.
     * @param battleId The ID of the battle.
     * @return The result object containing winner and loser information.
     * @throws IOException if there are issues during image processing or file operations.
     */
    public Result getResult(Long battleId) throws IOException {
        System.out.println("Get result of CSS called.");
        Battle battle = battleService.getActiveBattleById(battleId);
        String challengerUsername = battle.getChallenger().getUsername();
        String opponentUsername = battle.getOpponent().getUsername();

        String challengerScreenshotPath = null;
        String opponentScreenshotPath = null;

        try {
            // Capture and store the screenshot paths
            challengerScreenshotPath = captureScreenshot(battleId, challengerUsername);
            opponentScreenshotPath = captureScreenshot(battleId, opponentUsername);

            // Calculate final scores using the captured screenshot paths
            double challengerScore = finalScore(battleId, challengerScreenshotPath);
            double opponentScore = finalScore(battleId, opponentScreenshotPath);

            String winnerUsername, loserUsername;
            double winnerScore, loserScore;

            System.out.println("Final score " + challengerScore);
            System.out.println("Final score " + opponentScore);

            if (challengerScore >= opponentScore) {
                winnerUsername = challengerUsername;
                loserUsername = opponentUsername;
                winnerScore = challengerScore;
                loserScore = opponentScore;
            } else {
                winnerUsername = opponentUsername;
                loserUsername = challengerUsername;
                winnerScore = opponentScore;
                loserScore = challengerScore;
            }

            return Result.builder()
                    .winnerUsername(winnerUsername)
                    .loserUsername(loserUsername)
                    .winnerScore(String.format("%.2f", winnerScore) + " Points")
                    .loserScore(String.format("%.2f", loserScore) + " Points")
                    .build();

        } catch (Exception e) {
            System.err.println("Error calculating battle results for battleId " + battleId + ": " + e.getMessage());
            throw new IOException("Failed to process battle results.", e);
        } finally {
            // Clean up temporary screenshot files
            if (challengerScreenshotPath != null) {
                try {
                    Files.deleteIfExists(Paths.get(challengerScreenshotPath));
                    System.out.println("Deleted temporary screenshot file: " + challengerScreenshotPath);
                } catch (IOException e) {
                    System.err.println("Failed to delete screenshot file: " + challengerScreenshotPath + " - " + e.getMessage());
                }
            }
            if (opponentScreenshotPath != null) {
                try {
                    Files.deleteIfExists(Paths.get(opponentScreenshotPath));
                    System.out.println("Deleted temporary screenshot file: " + opponentScreenshotPath);
                } catch (IOException e) {
                    System.err.println("Failed to delete screenshot file: " + opponentScreenshotPath + " - " + e.getMessage());
                }
            }
        }
    }
}