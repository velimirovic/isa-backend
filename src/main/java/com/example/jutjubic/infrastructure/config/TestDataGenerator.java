package com.example.jutjubic.infrastructure.config;

import com.example.jutjubic.core.domain.VideoPostStatus;
import com.example.jutjubic.infrastructure.entity.TagEntity;
import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.repository.JpaTagRepository;
import com.example.jutjubic.infrastructure.repository.JpaUserRepository;
import com.example.jutjubic.infrastructure.repository.JpaVideoPostRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TestDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(TestDataGenerator.class);

    private final JpaVideoPostRepository videoPostRepository;
    private final JpaUserRepository userRepository;
    private final JpaTagRepository tagRepository;

    // Kontrola da li se pokreće generisanje (može se isključiti preko application.properties)
    @Value("${app.test-data.enabled:false}")
    private boolean testDataEnabled;

    @Value("${app.test-data.count:5000}")
    private int testDataCount;

    // Koordinate Evrope
    private static final double MIN_LAT = 35.0;  // Južna granica (jug Grčke)
    private static final double MAX_LAT = 70.0;  // Severna granica (sever Norveške)
    private static final double MIN_LNG = -10.0; // Zapadna granica (Portugal)
    private static final double MAX_LNG = 40.0;  // Istočna granica (Rusija)

    // Lista evropskih gradova za autentičnije naslove
    private static final String[] CITIES = {
            "Paris", "London", "Berlin", "Madrid", "Rome", "Amsterdam",
            "Vienna", "Prague", "Budapest", "Warsaw", "Belgrade", "Athens",
            "Stockholm", "Copenhagen", "Oslo", "Helsinki", "Dublin", "Lisbon",
            "Barcelona", "Munich", "Hamburg", "Brussels", "Zurich", "Edinburgh"
    };

    // Tipovi videa
    private static final String[] VIDEO_TYPES = {
            "Vlog", "Travel Guide", "Food Tour", "Street Walk", "Drone Footage",
            "Timelapse", "City Night", "Architecture", "Culture", "History",
            "Local Experience", "Hidden Gems", "Weekend Trip", "Day in the Life"
    };

    // Tagovi
    private static final String[] TAGS = {
            "travel", "europe", "vlog", "food", "city", "culture", "adventure",
            "architecture", "history", "nature", "tourism", "explore", "wanderlust"
    };

    @PostConstruct
    @Transactional
    public void init() {
        if (!testDataEnabled) {
            log.info("Test data generation is DISABLED. Set app.test-data.enabled=true to enable.");
            return;
        }

        log.info("Starting test data generation...");

        // Proveri da li već postoje test videi
        long existingCount = videoPostRepository.count();
        if (existingCount >= testDataCount) {
            log.info("Test data already exists ({} videos). Skipping generation.", existingCount);
            return;
        }

        try {
            // 1. Kreiraj test korisnika ako ne postoji
            UserEntity testUser = getOrCreateTestUser();

            // 2. Kreiraj tagove ako ne postoje
            Set<TagEntity> availableTags = createTags();

            // 3. Generiši test videe
            generateTestVideos(testUser, availableTags);

            log.info("Successfully generated {} test videos!", testDataCount);

        } catch (Exception e) {
            log.error("Error generating test data: {}", e.getMessage(), e);
        }
    }

    private UserEntity getOrCreateTestUser() {
        String testEmail = "test.data.generator@jutjubic.com";

        Optional<UserEntity> existingUser = userRepository.findByEmail(testEmail);
        if (existingUser.isPresent()) {
            log.info("Using existing test user: {}", testEmail);
            return existingUser.get();
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(testEmail);
        newUser.setUsername("TestDataGenerator");
        newUser.setPassword("$2a$10$dummyHashedPassword"); // Dummy hash
        newUser.setFirstName("Test");
        newUser.setLastName("Generator");
        newUser.setAddress("Test Address");
        newUser.setActivated(true);

        UserEntity saved = userRepository.save(newUser);
        log.info("Created new test user: {}", testEmail);
        return saved;
    }

    private Set<TagEntity> createTags() {
        Set<TagEntity> tags = new HashSet<>();

        for (String tagName : TAGS) {
            TagEntity existingTag = tagRepository.findByName(tagName);
            if (existingTag != null) {
                tags.add(existingTag);
            } else {
                TagEntity newTag = new TagEntity();
                newTag.setName(tagName);
                tags.add(tagRepository.save(newTag));
            }
        }

        log.info("Prepared {} tags", tags.size());
        return tags;
    }

    private void generateTestVideos(UserEntity author, Set<TagEntity> availableTags) {
        Random random = new Random();

        int batchSize = 100;
        int generatedCount = 0;

        for (int i = 0; i < testDataCount; i++) {
            VideoPostEntity video = new VideoPostEntity();

            // Osnovne informacije
            String city = CITIES[random.nextInt(CITIES.length)];
            String type = VIDEO_TYPES[random.nextInt(VIDEO_TYPES.length)];
            video.setTitle(type + " in " + city + " #" + (i + 1));
            video.setDescription("Exploring the beautiful city of " + city +
                    ". Join me on this amazing " + type.toLowerCase() + " adventure!");

            // Dummy putanje
            video.setVideoPath("test-data/dummy-video.mp4");
            video.setThumbnailPath("test-data/dummy-thumbnail.png");

            // Random lokacija u Evropi
            float latitude = (float) (MIN_LAT + (MAX_LAT - MIN_LAT) * random.nextDouble());
            float longitude = (float) (MIN_LNG + (MAX_LNG - MIN_LNG) * random.nextDouble());
            video.setLatitude(latitude);
            video.setLongitude(longitude);

            // Random broj pregleda
            video.setViewCount(random.nextInt(10000));

            // Random datum
            LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(365));
            video.setCreatedAt(createdAt);

            // Status
            video.setStatus(VideoPostStatus.PUBLISHED);
            video.setDraftId(UUID.randomUUID().toString());

            // Autor
            video.setAuthor(author);

            // Tagovi
            Set<TagEntity> videoTags = new HashSet<>();
            int numTags = 2 + random.nextInt(3); // 2 do 4 taga

            // Biramo random tag imena
            Set<String> selectedTagNames = new HashSet<>();
            for (int j = 0; j < numTags; j++) {
                String randomTagName = TAGS[random.nextInt(TAGS.length)];
                selectedTagNames.add(randomTagName);
            }

            // Učitavamo tagove iz baze (biće "managed" entiteti)
            for (String tagName : selectedTagNames) {
                TagEntity tag = tagRepository.findByName(tagName);
                if (tag != null) {
                    videoTags.add(tag);
                }
            }
            video.setTags(videoTags);

            // Sačuvaj
            videoPostRepository.save(video);
            generatedCount++;

            // Batch commit
            if (generatedCount % batchSize == 0) {
                videoPostRepository.flush();
                log.info("Generated {}/{} test videos...", generatedCount, testDataCount);
            }
        }

        videoPostRepository.flush();
        log.info("Finished generating {} videos", generatedCount);
    }
}