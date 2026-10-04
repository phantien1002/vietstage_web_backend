import os
path = "d:/Do_An/vietstage_web_backend/src/main/java/com/example/vietstage_web_be/service/impl/LearnerProgressServiceImpl.java"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

target1 = """LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Learner profile not found: " + learnerId));"""
content = content.replace(target1, "LearnerProfile profile = getOrCreateProfile(learnerId);")

target2 = "LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId).orElse(null);"
content = content.replace(target2, "LearnerProfile profile = getOrCreateProfile(learnerId);")

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
