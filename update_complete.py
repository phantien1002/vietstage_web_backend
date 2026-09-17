import os
import re

impl_path = 'src/main/java/com/example/vietstage_web_be/service/impl/LearnerProgressServiceImpl.java'
with open(impl_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure we import CompletionAttempt and repository
if 'CompletionAttemptRepository completionAttemptRepository' not in content:
    content = content.replace(
        'private final com.example.vietstage_web_be.repository.LessonCompletionRepository lessonCompletionRepository;',
        'private final com.example.vietstage_web_be.repository.LessonCompletionRepository lessonCompletionRepository;\n    private final com.example.vietstage_web_be.repository.CompletionAttemptRepository completionAttemptRepository;\n    private final com.example.vietstage_web_be.repository.PointTransactionRepository pointTransactionRepository;'
    )

new_method_impl = """
    @Override
    @org.springframework.transaction.annotation.Transactional
    public LessonCompletionResponse completeLesson(Long learnerId, Long lessonId, LessonCompletionRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND, "Lesson not found"));

        if (request.getClientAttemptId() != null) {
            com.example.vietstage_web_be.entity.CompletionAttempt.CompletionAttemptId attemptId = 
                new com.example.vietstage_web_be.entity.CompletionAttempt.CompletionAttemptId(learnerId, request.getClientAttemptId());
            
            if (completionAttemptRepository.existsById(attemptId)) {
                // Already processed, return idempotently or skip
                return buildResponse(lessonCompletionRepository.findByLearnerIdAndLessonId(learnerId, lessonId)
                    .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND, "Completion not found")));
            }
            
            com.example.vietstage_web_be.entity.CompletionAttempt attempt = com.example.vietstage_web_be.entity.CompletionAttempt.builder()
                .learnerId(learnerId)
                .clientAttemptId(request.getClientAttemptId())
                .lessonCode(lesson.getLessonCode())
                .revision(lesson.getRevision())
                .score(request.getScore() != null ? request.getScore() : java.math.BigDecimal.ZERO)
                .completedAt(request.getCompletedAt() != null ? 
                    new java.sql.Timestamp(request.getCompletedAt().getTime()).toLocalDateTime() : 
                    java.time.LocalDateTime.now())
                .build();
            completionAttemptRepository.save(attempt);
        }

        LessonCompletion completion = lessonCompletionRepository.findByLearnerIdAndLessonId(learnerId, lessonId)
                .orElseGet(() -> {
                    LessonCompletion newComp = new LessonCompletion();
                    newComp.setLearner(userRepository.findById(learnerId).orElseThrow());
                    newComp.setLesson(lesson);
                    newComp.setBestScore(java.math.BigDecimal.ZERO);
                    newComp.setStars(0);
                    return newComp;
                });

        int oldStars = completion.getStars() != null ? completion.getStars() : 0;
        
        // Compute new stars based on score (e.g. >80 = 3, >50 = 2, else 1)
        int newStars = 1;
        if (request.getScore() != null) {
            if (request.getScore().compareTo(new java.math.BigDecimal("80")) >= 0) newStars = 3;
            else if (request.getScore().compareTo(new java.math.BigDecimal("50")) >= 0) newStars = 2;
        }
        
        int deltaStars = Math.max(0, newStars - oldStars);
        if (deltaStars > 0) {
            completion.setStars(newStars);
            
            // Give points in ledger for delta stars
            com.example.vietstage_web_be.entity.PointTransaction pt = com.example.vietstage_web_be.entity.PointTransaction.builder()
                .user(userRepository.findById(learnerId).orElseThrow())
                .sourceType("PRACTICE")
                .points(deltaStars * 10) // e.g. 10 points per star
                .build();
            pointTransactionRepository.save(pt);
        }

        if (request.getScore() != null && (completion.getBestScore() == null || request.getScore().compareTo(completion.getBestScore()) > 0)) {
            completion.setBestScore(request.getScore());
        }

        completion.setCompleted(true);
        completion.setCompletedAt(request.getCompletedAt() != null ? request.getCompletedAt() : new java.util.Date());
        completion.setUpdatedAt(new java.util.Date());
        completion.setLastClientAttemptId(request.getClientAttemptId());

        completion = lessonCompletionRepository.save(completion);
        
        return buildResponse(completion);
    }
    
    private LessonCompletionResponse buildResponse(LessonCompletion completion) {
        return LessonCompletionResponse.builder()
                .id(completion.getId())
                .status(completion.getStatus())
                .stars(completion.getStars())
                .bestScore(completion.getBestScore())
                .build();
    }
"""

content = re.sub(r'public LessonCompletionResponse completeLesson.*?}', new_method_impl.strip(), content, flags=re.DOTALL)

with open(impl_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated completeLesson logic in LearnerProgressServiceImpl")
