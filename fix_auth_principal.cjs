const fs = require('fs');
const path = require('path');

const controllersDir = 'd:/Do_An/vietstage_web_backend/src/main/java/com/example/vietstage_web_be/controller';

const files = fs.readdirSync(controllersDir).filter(f => f.endsWith('.java'));

files.forEach(file => {
    const filePath = path.join(controllersDir, file);
    let content = fs.readFileSync(filePath, 'utf8');
    let originalContent = content;

    if (content.includes('@AuthenticationPrincipal User ')) {
        content = content.replace(/@AuthenticationPrincipal\s+User\s+/g, '@AuthenticationPrincipal(expression = "user") User ');
    }

    if (content !== originalContent) {
        fs.writeFileSync(filePath, content, 'utf8');
        console.log(`Updated ${file}`);
    }
});
