# Pattern

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-orange)](https://kotlinlang.org/)  
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.9.0-blueviolet)](https://developer.android.com/jetpack/compose)

**Pattern** is a modern Kotlin based Android app designed to help you manage habits and recurring tasks with a flexible and extensible pattern system.  
It demonstrates clean architecture, reactive programming, and best practices in Android development.

## Features
- ✅ Create, track, and manage habits with different types: **Grow**, **Drop**, and **Task**.  
- 📅 Supports daily, weekly, and custom recurring schedules.  
- 🎨 Clean, modern UI built with **Jetpack Compose**.  
- 🗂️ Local persistence using **Room Database**.  
- ⚡ Dependency injection with **Dagger/Hilt** for scalable architecture.  
- 🔄 Asynchronous data handling using **Coroutines** and **Flows**.  
- 📊 Provides a clear overview of habit streaks and progress.  
- 🚀 Easily extendable for new habit types or features.  

## Tech Stack
- **Language:** Kotlin  
- **UI:** Jetpack Compose  
- **Dependency Injection:** Dagger / Hilt  
- **Local Database:** Room  
- **Concurrency:** Kotlin Coroutines & Flows  
- **Architecture:** MVVM / Clean Architecture  

## Installation
1. Clone the repository:
    ```bash
    git clone https://github.com/yourusername/Pattern.git
    ```
2. Open the project in **Android Studio**.  
3. Sync Gradle and build the project.  

## Usage
- **App:** Launch the app and add a new habit via the HabitCards screen.  
- **Library:** Example usage in code:
    ```kotlin
    val habit = HabitType.BUILD
    habitManager.addHabit(habit)
    ```

## License
This project is licensed under the [MIT License](LICENSE).

## Contact
- **Developer:** Emre Uyar  
- **GitHub:** [https://github.com/EmreRuy](https://github.com/EmreRuy)  
- **Email:** uyar.em.eu@gmail.com  
