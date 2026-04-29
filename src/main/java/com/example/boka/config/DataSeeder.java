package com.example.boka.config;

import com.example.boka.booking.domain.BookingRepository;
import com.example.boka.gym.application.GymService;
import com.example.boka.gym.domain.Gym;
import com.example.boka.gym.domain.GymInfo;
import com.example.boka.gym.domain.GymInfoRepository;
import com.example.boka.gym.domain.GymRepository;
import com.example.boka.gymclass.domain.ClassStatus;
import com.example.boka.gymclass.domain.ClassType;
import com.example.boka.gymclass.domain.GymClass;
import com.example.boka.gymclass.domain.ClassTypeRepository;
import com.example.boka.gymclass.domain.GymClassRepository;
import com.example.boka.user.domain.AuthProvider;
import com.example.boka.user.domain.User;
import com.example.boka.user.domain.UserRole;
import com.example.boka.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClassTypeRepository classTypeRepository;
    private final GymClassRepository gymClassRepository;
    private final BookingRepository bookingRepository;
    private final GymRepository gymRepository;
    private final GymService gymService;
    private final GymInfoRepository gymInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final Random random = new Random();

    @Value("${ADMIN_EMAIL:admin@boka.se}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${ADMIN_PASSWORD_FORCE_SYNC:false}")
    private boolean adminPasswordForceSync;

    @Override
    @Transactional
    public void run(String... args) {
        upsertAdminUser();

        if (gymRepository.count() > 0 || classTypeRepository.count() > 0) {
            log.info("Database already contains demo data — skipping seeding.");
            return;
        }

        log.info("Seeding database with dummy data...");

        // ── Gyms ────────────────────────────────────────────────
        List<Gym> gyms = new ArrayList<>();
        gyms.add(gymService.saveGym(createGymEntity("Boka Central", "Östra Hamngatan 16, 411 09 Göteborg", 57.7089, 11.9746)));
        gyms.add(gymService.saveGym(createGymEntity("Boka Majorna", "Karl Johansgatan 12, 414 59 Göteborg", 57.6931, 11.9281)));
        gyms.add(gymService.saveGym(createGymEntity("Boka Linné", "Linnégatan 5, 413 04 Göteborg", 57.6951, 11.9511)));
        gyms.add(gymService.saveGym(createGymEntity("Boka Hisingen", "Kvilletorget 2, 417 04 Göteborg", 57.7211, 11.9311)));
        gyms.add(gymService.saveGym(createGymEntity("Boka Johanneberg", "Gibraltargatan 10, 412 58 Göteborg", 57.6891, 11.9811)));
        gyms.add(gymService.saveGym(createGymEntity("Boka Olskroken", "Redbergsplatsen 1, 416 67 Göteborg", 57.7111, 12.0011)));

        // Synchronously populate the gym_info_cache for the gymclass module.
        // This avoids FK violations because the TransactionalEventListener won't run until this method commits.
        for (Gym gym : gyms) {
            gymInfoRepository.save(new GymInfo(gym.getId(), gym.getName(), gym.getAddress()));
        }

        // ── Users ────────────────────────────────────────────────
        User instructor1 = createUser("anna@boka.se", "Anna", "Johansson", UserRole.INSTRUCTOR, "070-222-2222");
        User instructor2 = createUser("erik@boka.se", "Erik", "Lindberg", UserRole.INSTRUCTOR, "070-333-3333");
        User instructor3 = createUser("sara@boka.se", "Sara", "Nilsson", UserRole.INSTRUCTOR, "070-444-4444");

        List<User> members = List.of(
            createUser("karl@example.com", "Karl", "Svensson", UserRole.MEMBER, "070-555-5555"),
            createUser("lisa@example.com", "Lisa", "Eriksson", UserRole.MEMBER, "070-666-6666"),
            createUser("oscar@example.com", "Oscar", "Berg", UserRole.MEMBER, "070-777-7777"),
            createUser("emma@example.com", "Emma", "Gustafsson", UserRole.MEMBER, "070-888-8888"),
            createUser("johan@example.com", "Johan", "Persson", UserRole.MEMBER, "070-999-9999")
        );

        userRepository.saveAll(List.of(instructor1, instructor2, instructor3));
        userRepository.saveAll(members);

        // ── Class Types ──────────────────────────────────────────
        ClassType yoga = createClassType("Yoga", "A calming practice focused on flexibility.", 20, 60);
        ClassType hiit = createClassType("HIIT", "High-intensity interval training.", 25, 45);
        ClassType strength = createClassType("Strength", "Progressive resistance training.", 15, 60);
        ClassType spinning = createClassType("Spinning", "Intense indoor cycling.", 30, 45);

        List<ClassType> classTypes = List.of(yoga, hiit, strength, spinning);
        classTypeRepository.saveAll(classTypes);

        // ── Gym Classes ──────────────────────────────────────────
        List<GymClass> allGymClasses = new ArrayList<>();
        LocalDateTime startBase = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        List<User> instructors = List.of(instructor1, instructor2, instructor3);

        for (int day = 0; day < 14; day++) {
            LocalDateTime dayDate = startBase.plusDays(day);
            for (int i = 0; i < 3; i++) {
                Gym randomGym = gyms.get(random.nextInt(gyms.size()));
                ClassType randomType = classTypes.get(random.nextInt(classTypes.size()));
                User randomInstructor = instructors.get(random.nextInt(instructors.size()));

                allGymClasses.add(createGymClass(randomType, randomInstructor.getId(), randomGym.getId(), dayDate.withHour(8 + i * 4), 60, randomType.getDefaultCapacity()));
            }
        }
        gymClassRepository.saveAll(allGymClasses);

        log.info("Database seeding complete!");
    }

    private static final String DEV_FALLBACK_PASSWORD = "password123";

    private void upsertAdminUser() {
        if (adminPassword == null || adminPassword.isBlank()) {
            boolean isDevOrTest = Arrays.stream(environment.getActiveProfiles())
                    .anyMatch(p -> p.equals("dev") || p.equals("test"));
            if (!isDevOrTest) {
                throw new IllegalStateException(
                        "ADMIN_PASSWORD environment variable must be set in non-dev/test profiles");
            }
            log.warn("ADMIN_PASSWORD not set — using insecure dev fallback. Do NOT use in production.");
            adminPassword = DEV_FALLBACK_PASSWORD;
        }

        userRepository.findByEmail(adminEmail).ifPresentOrElse(
            existing -> {
                if (passwordEncoder.matches(adminPassword, existing.getPasswordHash())) {
                    log.debug("Admin password unchanged — skipping write for: {}", adminEmail);
                    return;
                }
                if (!adminPasswordForceSync) {
                    log.warn("Admin password mismatch detected for {} but ADMIN_PASSWORD_FORCE_SYNC is false — skipping overwrite.", adminEmail);
                    return;
                }
                existing.setPasswordHash(passwordEncoder.encode(adminPassword));
                userRepository.save(existing);
                log.info("Admin password force-synced for: {}", adminEmail);
            },
            () -> {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setRole(UserRole.ADMIN);
                admin.setIsActive(true);
                admin.setAuthProvider(AuthProvider.LOCAL);
                userRepository.save(admin);
                log.info("Admin user created: {}", adminEmail);
            }
        );
    }

    private Gym createGymEntity(String name, String address, Double lat, Double lon) {
        Gym gym = new Gym();
        gym.setName(name);
        gym.setAddress(address);
        gym.setLatitude(lat);
        gym.setLongitude(lon);
        return gym;
    }

    private User createUser(String email, String firstName, String lastName, UserRole role, String phone) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phone);
        user.setRole(role);
        user.setIsActive(true);
        user.setAuthProvider(AuthProvider.LOCAL);
        return user;
    }

    private ClassType createClassType(String name, String description, int capacity, int duration) {
        ClassType ct = new ClassType();
        ct.setName(name);
        ct.setDescription(description);
        ct.setDefaultCapacity(capacity);
        ct.setDurationMinutes(duration);
        ct.setIsActive(true);
        return ct;
    }

    private GymClass createGymClass(ClassType type, Long instructorId, Long gymId, LocalDateTime start, int durationMinutes, int capacity) {
        GymClass gc = new GymClass();
        gc.setClassType(type);
        gc.setInstructorId(instructorId);
        gc.setGymId(gymId);
        gc.setStartTime(start);
        gc.setEndTime(start.plusMinutes(durationMinutes));
        gc.setCapacity(capacity);
        gc.setStatus(ClassStatus.SCHEDULED);
        return gc;
    }
}
