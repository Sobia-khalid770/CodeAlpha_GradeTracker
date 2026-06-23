# GradeTracker — Student Grade Management System

A polished, console-based Java application for tracking student grades.

![GradeTracker logo](logo.png)

## ✨ Features
- Add, view, search, and remove student records
- Automatic **average / highest / lowest** score calculation
- Letter grades (A–F) auto-assigned per student
- Color-coded, professional terminal UI with a "GradeTracker" logo banner
- Data stored in an `ArrayList<Student>` (easy to extend to file/DB storage)
- Input validation (no crashes on bad input)

## 📁 Files
- `GradeTracker.java` — the complete program (main class + `Student` class)

## ▶️ How to Run

1. Make sure you have a JDK installed (Java 17+ recommended; uses modern `switch ->` syntax).
2. Compile:
   ```
   javac GradeTracker.java
   ```
3. Run:
   ```
   java GradeTracker
   ```

> 💡 The interface uses ANSI color codes. It looks best in a real terminal
> (Windows Terminal, macOS Terminal, Linux terminal, or VS Code's integrated
> terminal). Older Windows `cmd.exe` may not render colors — use Windows
> Terminal or PowerShell instead, or run via VS Code.

## 🧭 Menu Options
```
1. Add a Student        — enter name + score (0–100)
2. View All Students     — table of all records with grade
3. Show Summary Report   — average / highest / lowest + full table
4. Remove a Student      — delete by list number
5. Search Student        — find by (partial) name
0. Exit
```

## 🛠 Possible Extensions (great next steps to learn more)
- Save/load data to a `.csv` or `.txt` file so records persist between runs
- Convert to a Swing/JavaFX GUI version
- Add subjects/multiple scores per student with weighted averages
- Sort students by score or name
- Export the summary report to a file
