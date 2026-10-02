const fs = require('fs');
const path = require('path');

const basePath = "D:/Do_An/vietstage_web_backend/Authoritative Input Files (Doc)/vietstage_curriculum_backend_handoff/scripts";

const dtBundle = require(path.join(basePath, 'DanTranhBundledLessonData.cjs'));
const dtCourse = require(path.join(basePath, 'DanTranhCourseData.cjs'));
const stBundle = require(path.join(basePath, 'SaoTrucBundledLessonData.cjs'));
const stCourse = require(path.join(basePath, 'SaoTrucCourseData.cjs'));

const output = {
  DanTranhBundledLessonData: dtBundle,
  DanTranhCourseData: dtCourse,
  SaoTrucBundledLessonData: stBundle,
  SaoTrucCourseData: stCourse
};

fs.writeFileSync('parsed_data.json', JSON.stringify(output, null, 2), 'utf-8');
console.log('Successfully wrote parsed_data.json');
