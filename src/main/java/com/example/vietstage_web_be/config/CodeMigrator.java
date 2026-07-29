package com.example.vietstage_web_be.config;

import com.example.vietstage_web_be.entity.Instrument;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.repository.InstrumentRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class CodeMigrator implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final InstrumentRepository instrumentRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Migrate Users
        List<User> usersWithoutCode = userRepository.findAll().stream()
                .filter(u -> u.getUserCode() == null || u.getUserCode().isEmpty())
                .toList();

        for (User user : usersWithoutCode) {
            String prefix = "HV";
            if (user.getRole() != null) {
                if ("ADMIN".equalsIgnoreCase(user.getRole().getName())) prefix = "AD";
                else if ("INSTRUCTOR".equalsIgnoreCase(user.getRole().getName())) prefix = "GV";
            }
            user.setUserCode(String.format("%s-%04d", prefix, new Random().nextInt(10000)));
            userRepository.save(user);
        }

        // Migrate Instruments
        List<Instrument> instrumentsWithoutCode = instrumentRepository.findAll().stream()
                .filter(i -> i.getInstrumentCode() == null || i.getInstrumentCode().isEmpty())
                .toList();
        for (Instrument instrument : instrumentsWithoutCode) {
            String shortName = instrument.getName().substring(0, Math.min(2, instrument.getName().length())).toUpperCase();
            instrument.setInstrumentCode(String.format("INS-%s-%03d", shortName, new Random().nextInt(1000)));
            instrumentRepository.save(instrument);
        }

        // Migrate Lesson
        List<Lesson> lessonsWithoutCode = lessonRepository.findAll().stream()
                .filter(l -> l.getLessonCode() == null || l.getLessonCode().isEmpty())
                .toList();
        for (Lesson lesson : lessonsWithoutCode) {
            String instrumentPart = (lesson.getInstrument() != null) ? 
                lesson.getInstrument().getName().substring(0, Math.min(2, lesson.getInstrument().getName().length())).toUpperCase() : "XX";
            String levelPart = (lesson.getSkillLevel() != null) ? lesson.getSkillLevel().getLevelCode() : "X";
            lesson.setLessonCode(String.format("LSN-%s-%s-%03d", instrumentPart, levelPart, new Random().nextInt(1000)));
            lessonRepository.save(lesson);
        }
    }
}

