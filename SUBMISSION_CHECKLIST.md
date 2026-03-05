# Submission Checklist

Use this checklist before final submission.

## 1) Build and test

- [ ] `.\gradlew.bat test` passes with no failing tests.
- [ ] `.\gradlew.bat run` starts the game successfully.

## 2) Code readability

- [ ] Class and method names are clear and meaningful.
- [ ] Magic numbers are moved to constants (prefer `GameSettings`).
- [ ] Large methods are split when a method handles multiple responsibilities.
- [ ] Complex logic has short comments explaining intent.

## 3) Formatting and consistency

- [ ] Code follows `.editorconfig` (4 spaces, no trailing whitespace).
- [ ] No unused imports or dead code.
- [ ] Files use consistent line endings and final newline.

## 4) Testing quality

- [ ] New/changed logic has unit tests.
- [ ] Edge cases are covered (cooldowns, collisions, respawn, power-up timing).

## 5) Submission clarity

- [ ] Project structure and how to run are documented (already in `README` if maintained).
- [ ] Commit history/messages are understandable.
