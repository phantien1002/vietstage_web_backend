const fs = require('fs');
const path = require('path');

const file = 'd:/Do_An/vietstage_web_backend/src/main/java/com/example/vietstage_web_be/controller/ProgressController.java';
let content = fs.readFileSync(file, 'utf8');

// Ensure import for User
if (!content.includes('import com.example.vietstage_web_be.entity.User;')) {
    content = content.replace(
        'import org.springframework.security.core.Authentication;',
        'import org.springframework.security.core.Authentication;\nimport org.springframework.security.core.annotation.AuthenticationPrincipal;\nimport com.example.vietstage_web_be.entity.User;'
    );
}

// Replace getLearnerProgress
content = content.replace(
    'Authentication authentication) {\n\n        Long currentLearnerId = Long.parseLong(authentication.getName());',
    '@AuthenticationPrincipal(expression = "user") User currentUser) {\n\n        Long currentLearnerId = currentUser.getId();'
);

// Replace getLearnerProgressSummary
content = content.replace(
    'Authentication authentication) {\n\n        Long currentLearnerId = Long.parseLong(authentication.getName());',
    '@AuthenticationPrincipal(expression = "user") User currentUser) {\n\n        Long currentLearnerId = currentUser.getId();'
);

// Replace getLearnerProgressByInstructor
content = content.replace(
    'Authentication authentication) {\n\n        Long currentInstructorId = Long.parseLong(authentication.getName());',
    '@AuthenticationPrincipal(expression = "user") User currentUser) {\n\n        Long currentInstructorId = currentUser.getId();'
);

fs.writeFileSync(file, content, 'utf8');
console.log('ProgressController updated');
