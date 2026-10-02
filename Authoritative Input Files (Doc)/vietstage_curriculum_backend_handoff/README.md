# VietStage curriculum handoff — exact bundled source

This package is the source of truth for the curriculum currently rendered by
VietStageApp. Import the content from these files without simplifying or
renumbering any stable lesson ID.

## Source files

- `scripts/DanTranhBundledLessonData.gd`: Dan Tranh lesson catalog, every Mai
  dialogue, note/duration/fingering sequences, and special Á/Nhấn/Vê/Song
  thanh/triad configurations.
- `scripts/SaoTrucBundledLessonData.gd`: Sáo Trúc catalog, notes, finger holes,
  teacher intro/mid dialogue and practice data.
- `scripts/DanTranhCourseData.gd`, `scripts/SaoTrucCourseData.gd`: course and
  level ordering plus stable card IDs.

## Required backend representation

For each stable lesson code, create activity records in this order:

1. `VIDEO_CUE` only if a `video_path`/video sequence exists.
2. one `TEACHER_SPEECH` record per dialogue line, retaining the source order.
3. one `PRACTICE` exercise containing the unchanged source configuration:
   `sheet`/`noteSequence`, `durations`, `fingerings`, `cues`, `practiceMode`,
   and technique rounds where present.

Do not create a blank exercise for a video-only lesson. All seeded lessons must
remain `APPROVED` and visible. The media files in `media/` are copied byte for
byte from the app paths referenced by the source.
