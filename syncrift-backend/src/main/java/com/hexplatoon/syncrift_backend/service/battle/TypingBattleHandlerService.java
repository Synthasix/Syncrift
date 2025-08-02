package com.hexplatoon.syncrift_backend.service.battle;

import com.github.javafaker.Faker;
import com.hexplatoon.syncrift_backend.dto.battle.Result;
import com.hexplatoon.syncrift_backend.dto.battle.config.Config;
import com.hexplatoon.syncrift_backend.dto.battle.config.TypingConfig;
import com.hexplatoon.syncrift_backend.entity.Battle;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TypingBattleHandlerService {

    private final Map<Long, Config> configMap = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, String>> userTextMap = new ConcurrentHashMap<>();
    private final BattleService battleService;
    private final Faker faker = new Faker();
    private final Random random = new Random();

    TypingBattleHandlerService(@Lazy BattleService battleService){
        this.battleService = battleService;
    }

    public Config getConfig(Long battleId) {

        String text = getRandomText(150);
        Integer duration = battleService.getActiveBattleById(battleId).getDuration();

        Config config = TypingConfig.builder()
                .text(text)
                .duration(duration)
                .build();

        configMap.put(battleId, config);
        return config;
    }

    public void saveUserText(Long battleId, String username, String text) {
        userTextMap.putIfAbsent(battleId, new ConcurrentHashMap<>());
        userTextMap.get(battleId).put(username, text);
    }

    public Result getResult(Long battleId) {
        System.out.println("getResult");
        // TODO : Faulty result calculation. No accuracy check
        // TODO : require error checks
        Battle battle = battleService.findBattleById(battleId);
        Config config = configMap.remove(battle.getId());

        String challengerUsername = battle.getChallenger().getUsername();
        String opponentUsername = battle.getOpponent().getUsername();
        Map<String, String> textMap = userTextMap.remove(battleId);
        String challengerText = textMap.get(challengerUsername);
        String opponentText = textMap.get(opponentUsername);
        String originalText = ((TypingConfig)config).getText();
        Integer durationInSeconds = ((TypingConfig)config).getDuration();

        double challengerWPM = calculateWPM(challengerText, originalText, durationInSeconds);
        double opponentWPM = calculateWPM(opponentText, originalText, durationInSeconds);
        String winnerUsername, loserUsername;
        double winnerScore, loserScore;

        if (challengerWPM >= opponentWPM) {
            winnerUsername = challengerUsername;
            loserUsername = opponentUsername;
            winnerScore = challengerWPM;
            loserScore = opponentWPM;
        } else {
            winnerUsername = opponentUsername;
            loserUsername = challengerUsername;
            winnerScore = opponentWPM;
            loserScore = challengerWPM;
        }

        return Result.builder()
                .winnerUsername(winnerUsername)
                .loserUsername(loserUsername)
                .winnerScore((int)winnerScore + " WPM")
                .loserScore((int)loserScore + " WPM")
                .build();
    }

    private double calculateWPM(String typedText, String originalText, int durationInSeconds) {
        String[] typedWords = typedText.trim().split("\\s+");
        String[] originalWords = originalText.trim().split("\\s+");

        int correctWords = 0;
        int totalWords = Math.min(typedWords.length, originalWords.length);

        for (int i = 0; i < totalWords; i++) {
            if (typedWords[i].equals(originalWords[i])) {
                correctWords++;
            }
        }

        System.out.println("correctWords: " + correctWords);

        // WPM calculation
        double minutes = durationInSeconds / 60.0;
        return correctWords / minutes;
    }


    public String getRandomText(int minWordCount) {
        List<String> words = new ArrayList<>();

        while (words.size() < minWordCount) {
            String sentence;
            switch (random.nextInt(15)) {
                case 0 -> sentence = faker.hitchhikersGuideToTheGalaxy().quote();
                case 1 -> sentence = faker.book().title() + " by " + faker.book().author();
                case 2 -> sentence = faker.company().catchPhrase();
                case 3 -> sentence = faker.shakespeare().kingRichardIIIQuote();
                case 4 -> sentence = faker.shakespeare().asYouLikeItQuote();
                case 5 -> sentence = faker.shakespeare().hamletQuote();
                case 6 -> sentence = faker.shakespeare().romeoAndJulietQuote();
                case 7 -> sentence = faker.yoda().quote();
                case 8 -> sentence = faker.chuckNorris().fact();
                case 9 -> sentence = faker.friends().quote();
                case 10 -> sentence = faker.gameOfThrones().quote();
                case 11 -> sentence = faker.harryPotter().quote();
                case 12 -> sentence = faker.lordOfTheRings().character();
                case 13 -> sentence = faker.artist().name() + " plays " + faker.music().instrument();
                case 14 -> sentence = faker.superhero().name() + " can " + faker.superhero().power();
                default -> sentence = faker.educator().course();
            }

            // Clean and split the sentence into words
            String[] rawWords = sentence.replaceAll("[.,!?':\"’;+π/0123456789%=–…‘—#()\\-]", "")
                    .toLowerCase()
                    .split("\\s+");

            // Filter and add words <= 7 characters
            for (String word : rawWords) {
                if (word.length() <= 7) {
                    words.add(word);
                    if (words.size() >= minWordCount) break;
                }
            }
        }

        return String.join(" ", words).trim();
    }
}
