package com.example.boka.config;

import com.example.boka.entity.*;
import com.example.boka.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClassTypeRepository classTypeRepository;
    private final GymClassRepository gymClassRepository;
    private final BookingRepository bookingRepository;
    private final GymRepository gymRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0 || classTypeRepository.count() > 0 || gymRepository.count() > 0) {
            log.info("Database already contains data (users, class types, or gyms) — skipping seeding.");
            return;
        }

        log.info("Seeding database with dummy data...");

        // ── Gyms (Gothenburg Area) ──────────────────────────────
        List<Gym> gyms = List.of(
            createGym("Boka Central", "Östra Hamngatan 16, 411 09 Göteborg", 57.7089, 11.9746),
            createGym("Boka Majorna", "Karl Johansgatan 12, 414 59 Göteborg", 57.6931, 11.9281),
            createGym("Boka Linné", "Linnégatan 5, 413 04 Göteborg", 57.6951, 11.9511),
            createGym("Boka Hisingen", "Kvilletorget 2, 417 04 Göteborg", 57.7211, 11.9311),
            createGym("Boka Johanneberg", "Gibraltargatan 10, 412 58 Göteborg", 57.6891, 11.9811),
            createGym("Boka Olskroken", "Redbergsplatsen 1, 416 67 Göteborg", 57.7111, 12.0011)
        );
        gymRepository.saveAll(gyms);

        // ── Users ────────────────────────────────────────────────
        User admin = createUser("admin@boka.se", "Admin", "Adminsson", UserRole.ADMIN, "070-111-1111");
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

        userRepository.saveAll(List.of(admin, instructor1, instructor2, instructor3));
        userRepository.saveAll(members);

        // ── Class Types ──────────────────────────────────────────
        ClassType yoga = createClassType("Yoga", "A calming practice focused on flexibility and mindfulness.", 20, 60);
        ClassType hiit = createClassType("HIIT", "High-intensity interval training for maximum calorie burn.", 25, 45);
        ClassType strength = createClassType("Strength", "Progressive resistance training to build muscle.", 15, 60);
        ClassType spinning = createClassType("Spinning", "Intense indoor cycling workout.", 30, 45);
        ClassType pilates = createClassType("Pilates", "Core strength, flexibility, and body awareness.", 18, 50);
        ClassType boxing = createClassType("Boxing", "High-energy boxing techniques and cardio.", 20, 60);
        ClassType zumba = createClassType("Zumba", "Dance-fitness party with international music.", 35, 55);
        ClassType crossfit = createClassType("CrossFit", "Varied functional fitness at high intensity.", 16, 60);

        List<ClassType> classTypes = List.of(yoga, hiit, strength, spinning, pilates, boxing, zumba, crossfit);
        classTypeRepository.saveAll(classTypes);

        // ── Gym Classes (Rolling 14-day schedule) ────────────────
        List<User> instructors = List.of(instructor1, instructor2, instructor3);
        List<GymClass> allGymClasses = new ArrayList<>();

        LocalDateTime startBase = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        for (int day = 0; day < 14; day++) {
            LocalDateTime dayDate = startBase.plusDays(day);

            // Generate 5 random classes per day at random gyms
            for (int i = 0; i < 5; i++) {
                Gym randomGym = gyms.get(random.nextInt(gyms.size()));
                ClassType randomType = classTypes.get(random.nextInt(classTypes.size()));
                User randomInstructor = instructors.get(random.nextInt(instructors.size()));
                int hour = 7 + random.nextInt(14); // 7:00 to 21:00

                allGymClasses.add(createGymClass(randomType, randomInstructor, randomGym, dayDate.withHour(hour), 60, randomType.getDefaultCapacity()));
            }
        }

        gymClassRepository.saveAll(allGymClasses);

        // ── Random Bookings ─────────────────────────────────────
        List<Booking> seedBookings = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            User randomMember = members.get(random.nextInt(members.size()));
            GymClass randomClass = allGymClasses.get(random.nextInt(allGymClasses.size()));

            boolean alreadyBooked = seedBookings.stream()
                .anyMatch(b -> b.getUser().getEmail().equals(randomMember.getEmail()) &&
                               b.getGymClass().getId().equals(randomClass.getId()));

            if (!alreadyBooked && !randomClass.isFull()) {
                seedBookings.add(createBooking(randomMember, randomClass, BookingStatus.CONFIRMED));
            }
        }
        bookingRepository.saveAll(seedBookings);

        log.info("Database seeding complete! Created {} gyms, {} users, {} class types, {} gym classes, {} bookings.",
                gymRepository.count(), userRepository.count(), classTypeRepository.count(),
                gymClassRepository.count(), bookingRepository.count());
    }

    private Gym createGym(String name, String address, Double lat, Double lon) {
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

    private GymClass createGymClass(ClassType type, User instructor, Gym gym, LocalDateTime start, int durationMinutes, int capacity) {
        GymClass gc = new GymClass();
        gc.setClassType(type);
        gc.setInstructor(instructor);
        gc.setGym(gym);
        gc.setStartTime(start);
        gc.setEndTime(start.plusMinutes(durationMinutes));
        gc.setCapacity(capacity);
        gc.setStatus(ClassStatus.SCHEDULED);
        return gc;
    }

    private Booking createBooking(User user, GymClass gymClass, BookingStatus status) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setGymClass(gymClass);
        booking.setStatus(status);
        if (status == BookingStatus.CANCELLED) {
            booking.setCancelledAt(LocalDateTime.now());
        }
        return booking;
    }
}
