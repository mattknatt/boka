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
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding database with dummy data...");

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
            
            // Morning classes (7:00, 8:00, 9:00)
            allGymClasses.add(createGymClass(classTypes.get(random.nextInt(classTypes.size())), instructors.get(0), dayDate.withHour(7), 60, 20));
            allGymClasses.add(createGymClass(classTypes.get(random.nextInt(classTypes.size())), instructors.get(1), dayDate.withHour(8), 45, 25));
            
            // Lunch classes (11:30, 12:00)
            allGymClasses.add(createGymClass(classTypes.get(random.nextInt(classTypes.size())), instructors.get(2), dayDate.withHour(12), 45, 30));
            
            // Afternoon/Evening classes (16:00, 17:00, 18:00, 19:00)
            allGymClasses.add(createGymClass(classTypes.get(random.nextInt(classTypes.size())), instructors.get(random.nextInt(instructors.size())), dayDate.withHour(17), 60, 15));
            allGymClasses.add(createGymClass(classTypes.get(random.nextInt(classTypes.size())), instructors.get(random.nextInt(instructors.size())), dayDate.withHour(18), 60, 20));
            
            // Late evening (Monday & Wednesday)
            if (dayDate.getDayOfWeek().getValue() == 1 || dayDate.getDayOfWeek().getValue() == 3) {
                allGymClasses.add(createGymClass(classTypes.get(random.nextInt(classTypes.size())), instructors.get(0), dayDate.withHour(20), 45, 20));
            }
        }
        
        gymClassRepository.saveAll(allGymClasses);

        // ── Random Bookings ─────────────────────────────────────
        // Let's seed some initial bookings to make it look active
        List<Booking> seedBookings = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            User randomMember = members.get(random.nextInt(members.size()));
            GymClass randomClass = allGymClasses.get(random.nextInt(allGymClasses.size()));
            
            // Only book if not already booked
            boolean alreadyBooked = seedBookings.stream()
                .anyMatch(b -> b.getUser().getEmail().equals(randomMember.getEmail()) && 
                               b.getGymClass().getId().equals(randomClass.getId()));
            
            if (!alreadyBooked) {
                seedBookings.add(createBooking(randomMember, randomClass, BookingStatus.CONFIRMED));
            }
        }
        bookingRepository.saveAll(seedBookings);

        log.info("Database seeding complete! Created {} users, {} class types, {} gym classes, {} bookings.",
                userRepository.count(), classTypeRepository.count(),
                gymClassRepository.count(), bookingRepository.count());
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

    private GymClass createGymClass(ClassType type, User instructor, LocalDateTime start, int durationMinutes, int capacity) {
        GymClass gc = new GymClass();
        gc.setClassType(type);
        gc.setInstructor(instructor);
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
