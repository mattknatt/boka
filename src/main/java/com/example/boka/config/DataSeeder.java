package com.example.boka.config;

import com.example.boka.entity.*;
import com.example.boka.repository.*;
import com.example.boka.service.ClassSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClassTypeRepository classTypeRepository;
    private final GymClassRepository gymClassRepository;
    private final BookingRepository bookingRepository;
    private final ClassSearchService classSearchService;

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
        User member1 = createUser("karl@example.com", "Karl", "Svensson", UserRole.MEMBER, "070-555-5555");
        User member2 = createUser("lisa@example.com", "Lisa", "Eriksson", UserRole.MEMBER, "070-666-6666");
        User member3 = createUser("oscar@example.com", "Oscar", "Berg", UserRole.MEMBER, "070-777-7777");
        User member4 = createUser("emma@example.com", "Emma", "Gustafsson", UserRole.MEMBER, "070-888-8888");
        User member5 = createUser("johan@example.com", "Johan", "Persson", UserRole.MEMBER, "070-999-9999");

        userRepository.saveAll(List.of(admin, instructor1, instructor2, instructor3,
                member1, member2, member3, member4, member5));

        // ── Class Types ──────────────────────────────────────────
        ClassType yoga = createClassType("Yoga",
                "A calming practice focused on flexibility, balance, and mindfulness. "
                        + "Improve your posture, reduce stress, and connect body and mind through "
                        + "controlled breathing and gentle stretching poses. Suitable for all levels.",
                20, 60);

        ClassType hiit = createClassType("HIIT",
                "High-intensity interval training designed to maximize calorie burn and "
                        + "boost cardiovascular fitness. Short bursts of explosive exercises followed "
                        + "by brief rest periods. Great for fat loss and building endurance.",
                25, 45);

        ClassType strength = createClassType("Strength Training",
                "Build muscle, increase power, and improve bone density with progressive "
                        + "resistance training using free weights, barbells, and machines. "
                        + "Focus on compound movements like squats, deadlifts, and bench press.",
                15, 60);

        ClassType spinning = createClassType("Spinning",
                "An intense indoor cycling workout set to energizing music. "
                        + "Burn calories, strengthen your legs and core, and improve cardiovascular "
                        + "health with guided sprints, hill climbs, and interval rides.",
                30, 45);

        ClassType pilates = createClassType("Pilates",
                "A low-impact workout emphasizing core strength, flexibility, and body awareness. "
                        + "Controlled movements improve posture, muscle tone, and joint mobility. "
                        + "Perfect for rehabilitation and injury prevention.",
                18, 50);

        ClassType boxing = createClassType("Boxing Fitness",
                "A high-energy workout combining boxing techniques with cardio conditioning. "
                        + "Learn jabs, hooks, and uppercuts while building speed, agility, coordination, "
                        + "and total-body strength. Great stress relief.",
                20, 60);

        ClassType zumba = createClassType("Zumba",
                "A fun dance-fitness party with Latin and international music. "
                        + "Easy-to-follow choreography makes it feel like a night out rather than a workout. "
                        + "Burns calories while improving rhythm, coordination, and mood.",
                35, 55);

        ClassType crossfit = createClassType("CrossFit",
                "A varied functional fitness program combining weightlifting, gymnastics, "
                        + "and metabolic conditioning. Constantly varied, high-intensity workouts "
                        + "designed to improve overall physical preparedness and athleticism.",
                16, 60);

        List<ClassType> classTypes = List.of(yoga, hiit, strength, spinning, pilates, boxing, zumba, crossfit);
        classTypeRepository.saveAll(classTypes);

        // Generate vector embeddings for each class type description
        log.info("Generating vector embeddings for class types...");
        for (ClassType ct : classTypes) {
            try {
                classSearchService.updateEmbedding(ct);
                classTypeRepository.save(ct);
                log.info("  ✓ Embedded: {}", ct.getName());
            } catch (Exception e) {
                log.warn("  ✗ Failed to embed {}: {}", ct.getName(), e.getMessage());
            }
        }

        // ── Gym Classes (next 5 days) ───────────────────────────
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);

        // Day 1
        GymClass yogaMon = createGymClass(yoga, instructor1, tomorrow.withHour(7), 60, 20);
        GymClass hiitMon = createGymClass(hiit, instructor2, tomorrow.withHour(9), 45, 25);
        GymClass spinMon = createGymClass(spinning, instructor3, tomorrow.withHour(12), 45, 30);
        GymClass strengthMon = createGymClass(strength, instructor2, tomorrow.withHour(17), 60, 15);

        // Day 2
        GymClass pilatesTue = createGymClass(pilates, instructor1, tomorrow.plusDays(1).withHour(8), 50, 18);
        GymClass boxingTue = createGymClass(boxing, instructor3, tomorrow.plusDays(1).withHour(10), 60, 20);
        GymClass zumbaTue = createGymClass(zumba, instructor1, tomorrow.plusDays(1).withHour(18), 55, 35);

        // Day 3
        GymClass crossfitWed = createGymClass(crossfit, instructor2, tomorrow.plusDays(2).withHour(7), 60, 16);
        GymClass yogaWed = createGymClass(yoga, instructor1, tomorrow.plusDays(2).withHour(12), 60, 20);
        GymClass hiitWed = createGymClass(hiit, instructor3, tomorrow.plusDays(2).withHour(17), 45, 25);

        // Day 4
        GymClass spinThu = createGymClass(spinning, instructor3, tomorrow.plusDays(3).withHour(9), 45, 30);
        GymClass strengthThu = createGymClass(strength, instructor2, tomorrow.plusDays(3).withHour(11), 60, 15);
        GymClass pilatesThu = createGymClass(pilates, instructor1, tomorrow.plusDays(3).withHour(16), 50, 18);

        // Day 5
        GymClass boxingFri = createGymClass(boxing, instructor3, tomorrow.plusDays(4).withHour(8), 60, 20);
        GymClass zumbaFri = createGymClass(zumba, instructor1, tomorrow.plusDays(4).withHour(17), 55, 35);
        GymClass crossfitFri = createGymClass(crossfit, instructor2, tomorrow.plusDays(4).withHour(18), 60, 16);

        List<GymClass> gymClasses = List.of(
                yogaMon, hiitMon, spinMon, strengthMon,
                pilatesTue, boxingTue, zumbaTue,
                crossfitWed, yogaWed, hiitWed,
                spinThu, strengthThu, pilatesThu,
                boxingFri, zumbaFri, crossfitFri
        );
        gymClassRepository.saveAll(gymClasses);

        // ── Bookings ─────────────────────────────────────────────
        // member1 likes: Yoga, Spinning, CrossFit
        // member2 likes: Yoga, Strength
        // member3 likes: HIIT, Pilates
        bookingRepository.saveAll(List.of(
                createBooking(member1, yogaMon, BookingStatus.CONFIRMED),
                createBooking(member2, yogaMon, BookingStatus.CONFIRMED),
                createBooking(member3, hiitMon, BookingStatus.CONFIRMED),
                createBooking(member4, hiitMon, BookingStatus.CONFIRMED),
                createBooking(member5, hiitMon, BookingStatus.CONFIRMED),
                createBooking(member1, spinMon, BookingStatus.CONFIRMED),
                createBooking(member2, strengthMon, BookingStatus.CONFIRMED),
                createBooking(member3, pilatesTue, BookingStatus.CONFIRMED),
                createBooking(member4, boxingTue, BookingStatus.CONFIRMED),
                createBooking(member5, zumbaTue, BookingStatus.CONFIRMED),
                createBooking(member1, crossfitWed, BookingStatus.CONFIRMED),
                createBooking(member2, yogaWed, BookingStatus.CONFIRMED),
                createBooking(member3, boxingFri, BookingStatus.CONFIRMED)
        ));

        log.info("Database seeding complete! Created {} users, {} class types, {} gym classes, {} bookings.",
                userRepository.count(), classTypeRepository.count(),
                gymClassRepository.count(), bookingRepository.count());
    }

    // ── Factory helpers ──────────────────────────────────────────

    private User createUser(String email, String firstName, String lastName, UserRole role, String phone) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("$2a$10$dummyHashedPasswordForSeeding"); // BCrypt placeholder
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