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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final int WINDOW_DAYS = 30;
    private static final int[] CLASS_HOURS = {8, 12, 16};

    @Override
    @Transactional
    public void run(String... args) {
        upsertAdminUser();
        seedBaseDataIfEmpty();
        ensureUpcomingClasses();
    }

    /**
     * Maintains a rolling window of gym classes so there are always classes available
     * from today through {@value #WINDOW_DAYS} days ahead. Idempotent: a day that already
     * has classes (seeded, topped up, or admin-created) is left untouched, so existing
     * classes and their bookings are never disturbed. Runs on every boot and daily.
     */
    void ensureUpcomingClasses() {
        List<ClassType> classTypes = classTypeRepository.findAll();
        List<Gym> gyms = gymRepository.findAll();
        List<User> instructors = userRepository.findByRole(UserRole.INSTRUCTOR);
        if (classTypes.isEmpty() || gyms.isEmpty() || instructors.isEmpty()) {
            log.warn("Skipping rolling class top-up — base data (class types, gyms, or instructors) missing.");
            return;
        }

        LocalDate today = LocalDate.now();
        Set<LocalDate> daysWithClasses = gymClassRepository
                .findByStartTimeBetween(today.atStartOfDay(), today.plusDays(WINDOW_DAYS).atTime(LocalTime.MAX))
                .stream()
                .map(gc -> gc.getStartTime().toLocalDate())
                .collect(Collectors.toSet());

        List<GymClass> newClasses = new ArrayList<>();
        for (int day = 0; day <= WINDOW_DAYS; day++) {
            LocalDate date = today.plusDays(day);
            if (daysWithClasses.contains(date)) {
                continue;
            }
            for (int hour : CLASS_HOURS) {
                Gym gym = gyms.get(random.nextInt(gyms.size()));
                ClassType type = classTypes.get(random.nextInt(classTypes.size()));
                User instructor = instructors.get(random.nextInt(instructors.size()));
                newClasses.add(createGymClass(type, instructor.getId(), gym.getId(),
                        date.atTime(hour, 0), 60, type.getDefaultCapacity()));
            }
        }

        if (newClasses.isEmpty()) {
            log.info("Rolling class window already covers today → +{} days; nothing to add.", WINDOW_DAYS);
            return;
        }
        gymClassRepository.saveAll(newClasses);
        log.info("Added {} classes across {} day(s) to maintain the rolling {}-day window.",
                newClasses.size(), newClasses.size() / CLASS_HOURS.length, WINDOW_DAYS);
    }

    /** Daily top-up so the rolling window advances as time passes, surviving reboots. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void dailyRollingClassTopUp() {
        log.debug("Running daily rolling class top-up...");
        ensureUpcomingClasses();
    }

    private void seedBaseDataIfEmpty() {
        if (gymRepository.count() > 0 || classTypeRepository.count() > 0) {
            log.info("Base demo data already present — skipping one-time seeding.");
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

        // Gym classes are created by ensureUpcomingClasses() so they always start "today".
        log.info("Base demo data seeding complete!");
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
