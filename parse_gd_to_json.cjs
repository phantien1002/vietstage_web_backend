const fs = require('fs');

function parseGd(file) {
    let content = fs.readFileSync(file, 'utf8');
    
    // Add stub for preload
    content = "const preload = (x) => null;\n" + content;
    
    // Remove 'extends RefCounted' and 'class_name'
    content = content.replace(/^extends\s+.*$/gm, '');
    content = content.replace(/^class_name\s+.*$/gm, '');
    
    // Remove static func and everything after it
    let staticFuncIdx = content.indexOf('static func');
    if (staticFuncIdx !== -1) {
        content = content.substring(0, staticFuncIdx);
    }
    
    // GDScript comments '#' to JS comments '//'
    // But be careful not to replace '#' inside strings.
    // A simple regex might break if '#' is in strings, but let's hope it's fine.
    // Better: only replace '#' if it's at start of line or after spaces
    content = content.replace(/(^|\s)#/gm, '$1//');
    
    // Replace const NAME := with const NAME =
    content = content.replace(/const\s+(\w+)\s*:=\s*/g, 'const $1 = ');
    // Replace const NAME: Array[...] = with const NAME =
    content = content.replace(/const\s+(\w+)\s*:\s*[A-Za-z0-9_\[\]]+\s*=\s*/g, 'const $1 = ');
    // Replace const NAME: Dictionary = with const NAME =
    content = content.replace(/const\s+(\w+)\s*:\s*[A-Za-z0-9_]+\s*=\s*/g, 'const $1 = ');
    
    // Extract const names
    let constNames = [];
    let regex = /const\s+(\w+)\s*=/g;
    let match;
    while ((match = regex.exec(content)) !== null) {
        constNames.push(match[1]);
    }
    
    content += '\nmodule.exports = { ' + constNames.join(', ') + ' };\n';
    
    let tempFile = file.replace('.gd', '.cjs');
    fs.writeFileSync(tempFile, content);
    
    try {
        let data = require(tempFile);
        return data;
    } catch (e) {
        console.error("Error evaluating " + file, e);
        return {};
    }
}

let baseDir = "d:\\Do_An\\vietstage_web_backend\\Authoritative Input Files (Doc)\\vietstage_curriculum_backend_handoff\\scripts\\";

let dt = parseGd(baseDir + "DanTranhBundledLessonData.gd");
let st = parseGd(baseDir + "SaoTrucBundledLessonData.gd");
let dt_course = parseGd(baseDir + "DanTranhCourseData.gd");
let st_course = parseGd(baseDir + "SaoTrucCourseData.gd");

fs.writeFileSync('parsed_data.json', JSON.stringify({
    DanTranhBundledLessonData: dt,
    SaoTrucBundledLessonData: st,
    DanTranhCourseData: dt_course,
    SaoTrucCourseData: st_course
}, null, 2));

console.log("Done");
