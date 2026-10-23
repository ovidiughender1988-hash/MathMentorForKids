# MathMentorForKids

A smart Android math tutor for parents and kids.

Features
- Take a photo of a math problem
- Read the equation using ML Kit OCR
- Solve arithmetic expressions
- Solve simple linear equations like 3x + 7 = 19
- Solve quadratic equations like x^2 - 5x + 6 = 0
- Show clear, step-by-step guidance for learning

Project summary
This app is designed to help parents teach children math in a visual and friendly way. A photo of a problem can be captured and processed, then the app explains the solution in simple, educational steps.

Build instructions
1. Open the folder in Android Studio.
2. Let Gradle sync.
3. Run the app on an emulator or physical Android device.
4. Allow camera permission when prompted.

Example inputs
- 12 / 3 + 5
- 3x + 7 = 19
- x^2 - 5x + 6 = 0

Main files
- app/src/main/java/com/example/mathmentor/MainActivity.kt
- app/src/main/java/com/example/mathmentor/EquationSolver.kt
- app/src/main/res/layout/activity_main.xml

Small note
The binary Gradle wrapper JAR is normally created by Android Studio when the project is opened. It is not shown as text because it is a binary file.
