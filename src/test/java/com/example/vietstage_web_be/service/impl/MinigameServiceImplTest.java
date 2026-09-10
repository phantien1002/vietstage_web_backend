package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.MinigameChallengeRequest;
import com.example.vietstage_web_be.entity.Instrument;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.MinigameChallenge;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.repository.AppConfigRepository;
import com.example.vietstage_web_be.repository.LearnerProfileRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.MediaAssetRepository;
import com.example.vietstage_web_be.repository.MinigameAttemptRepository;
import com.example.vietstage_web_be.repository.MinigameChallengeRepository;
import com.example.vietstage_web_be.service.ILeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinigameServiceImplTest {
    @Mock private MinigameChallengeRepository challengeRepository;
    @Mock private MinigameAttemptRepository attemptRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private MediaAssetRepository mediaAssetRepository;
    @Mock private ILeaderboardService leaderboardService;
    @Mock private AppConfigRepository appConfigRepository;
    @Mock private LearnerProfileRepository learnerProfileRepository;
    @InjectMocks private MinigameServiceImpl service;

    private User instructor;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        instructor = User.builder().id(5L).fullName("Instructor").build();
        lesson = Lesson.builder()
                .id(12L)
                .title("Đàn tranh cơ bản")
                .createdBy(instructor)
                .instrument(Instrument.builder().id(1L).instrumentCode("dan_tranh").build())
                .build();
        when(lessonRepository.findById(12L)).thenReturn(Optional.of(lesson));
    }

    @Test
    void createsAuthoredRhythmRoundsWithMatchingNotesAndBeats() {
        when(challengeRepository.save(any(MinigameChallenge.class))).thenAnswer(invocation -> {
            MinigameChallenge challenge = invocation.getArgument(0);
            challenge.setId(99L);
            return challenge;
        });
        var response = service.createMinigame(instructor, 12L, request("{\"rounds\":[{\"title\":\"Vòng 1\",\"tempo_bpm\":100,\"events\":[{\"note\":\"Sol1\",\"mode\":\"SAMPLE\",\"duration_beats\":1},{\"note\":\"La1\",\"mode\":\"TARGET\",\"duration_beats\":1}]},{\"title\":\"Vòng 2\",\"tempo_bpm\":120,\"events\":[{\"note\":\"Sol4\",\"mode\":\"SAMPLE\",\"duration_beats\":0.5},{\"note\":\"La4\",\"mode\":\"TARGET\",\"duration_beats\":0.5}]}]}"));

        assertThat(response.getId()).isEqualTo(99L);
        ArgumentCaptor<MinigameChallenge> captor = ArgumentCaptor.forClass(MinigameChallenge.class);
        verify(challengeRepository).save(captor.capture());
        assertThat(captor.getValue().getContentJson()).contains("Vòng 1", "Sol1", "Vòng 2");
    }

    @Test
    void rejectsMissingOrMismatchedNotesAndUnorderedBeats() {
        assertThatThrownBy(() -> service.createMinigame(instructor, 12L, request("{\"tempo_bpm\":100,\"rounds\":[{\"beats\":[1,2],\"notes\":[]}]}")))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> service.createMinigame(instructor, 12L, request("{\"tempo_bpm\":100,\"rounds\":[{\"beats\":[1,1],\"notes\":[\"Đô2\",\"Rê2\"]}]}")))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> service.createMinigame(instructor, 12L, request("{\"tempo_bpm\":100,\"rounds\":[{\"beats\":[1,2],\"notes\":[\"Fa1\",\"Si1\"]}]}")))
                .isInstanceOf(AppException.class);
    }

    private MinigameChallengeRequest request(String contentJson) {
        MinigameChallengeRequest request = new MinigameChallengeRequest();
        request.setTitle("Luyện nhịp");
        request.setChallengeType("RHYTHM_MATCH");
        request.setContentJson(contentJson);
        request.setDifficulty("BEGINNER");
        request.setMaxScore(100);
        request.setOrderIndex(1);
        return request;
    }
}
