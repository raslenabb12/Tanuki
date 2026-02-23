# 🦝 Tanuki — Manga Tracker & Explorer

**Tanuki** is a modern Android application designed for manga enthusiasts to track their collections, explore new titles, and sync seamlessly with **AniList** and **MyAnimeList**. Built with Kotlin and following modern Android development practices, Tanuki provides a rich, responsive interface for managing your reading journey.
<div align="center">
  <a href="https://github.com/raslenabb12/Tanuki">
    <img src="[./images/app_icon.jpg](https://github.com/raslenabb12/Tanuki/blob/main/app/src/main/res/mipmap-hdpi/ic_launcher_foreground.webp)" alt="AnimeDrop" width="200">
  </a>

## ✨ Key Features

- **Sync with AniList & MAL**: Authenticate with your favorite tracking services to sync your library and status.
- **Advanced Manga Search**: Find manga by title, genre, or popularity using powerful search filters.
- **Character Discovery**: Explore detailed information about your favorite characters across various media.
- **Personalized Recommendations**: Get manga suggestions based on your reading history and preferences.
- **Stats and Insights**: Track your reading habits with visual charts and detailed profile statistics.
- **Modern UI/UX**: Enjoy a smooth, dynamic interface with features like bottom sheets, drawer navigation, and shimmer loading effects.

## 🛠 Tech Stack

Tanuki leverages a variety of modern Android libraries and tools:

- **Language**: [Kotlin](https://kotlinlang.org/)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **JSON Parsing**: [Moshi](https://github.com/square/moshi) & [Gson](https://github.com/google/gson)
- **Image Loading**: [Glide](https://github.com/bumptech/glide)
- **Architecture**: MVVM with [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) & [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
- **Concurrency**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Pagination**: [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data)
- **Charts**: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **HTML Parsing**: [Jsoup](https://jsoup.org/)
- **UI Components**: Material Components, Lottie, and custom animations.

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala or newer.
- JDK 17 or higher.
- A physical device or emulator running Android 8.0 (API 28) or higher.

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/raslenabb12/Tanuki.git
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync and download dependencies.
4. Run the app on your device/emulator.

## 📦 Building

To generate a debug APK, run:
```bash
./gradlew assembleDebug
```

For a release build (requires signing configuration):
```bash
./gradlew assembleRelease
```

## 🤝 Contributing

Contributions are welcome! If you have suggestions or find bugs, please feel free to open an issue or submit a pull request.

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Developed with ❤️ by [raslenabb12](https://github.com/raslenabb12)*
