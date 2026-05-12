# PersonalLibraryAndroidApp

An Android app that lets you catalog your personal book collection across multiple libraries and shelves. Add a book by scanning its ISBN barcode, by searching online, or by hand — metadata is fetched automatically from Google Books and Open Library.

> **Built in 2018, archived as-is.** A Kotlin rewrite is on the roadmap (see [*Planned modernization*](#planned-modernization) below) — this Java snapshot is preserved because the integration patterns (REST clients, camera + barcode pipeline, multi-source data merging, fragment-based navigation) are still instructive, and because the contrast between the 2018 codebase and the planned rewrite is part of what this repo is meant to show.

---

## At a glance

| | |
|---|---|
| Language | Java |
| Platform | Android (Activity + Fragment, Navigation Drawer) |
| Book metadata | Google Books API + Open Library API (dual source) |
| ISBN entry | Google Vision Barcode API (camera-based scanning) |
| HTTP | `AsyncHttpClient` (loopj) — see *What I'd change today* below |
| Localization | English + Italian (`values/`, `values-it/`) |
| Ads / SDKs integrated | AdMob, AppLovin, Facebook SDK |

---

## What the app does

- **Multi-library catalog** — define multiple libraries (e.g. *Home*, *Office*) with optional shelf numbering, then assign books to them. The data model separates `Book`, `Library`, and `BookOnLibrary` so a single book can live (as distinct copies) in multiple places.
- **Three ways to add a book**:
  1. **Scan the ISBN barcode** with the device camera. The detected ISBN is resolved against the online APIs and the metadata is filled in for you.
  2. **Search online** by title or author against Google Books or Open Library.
  3. **Manual entry** when the book isn't on either service.
- **Book status** — track each copy as *on shelf* or *in use*.
- **Local persistence** for the user's collection.
- **Localized** — strings shipped in English and Italian.

---

## Why this project is interesting

Beyond "another CRUD list app", three things in here are non-trivial:

### 1. A real-time camera pipeline that produces structured data

`BarcodeCaptureActivity` wires the device camera into a Google Vision detector, which emits ISBN strings as the user points the phone at a book. The detected ISBN then triggers an asynchronous metadata lookup. The pipeline is:

```
Camera frames → Vision Barcode Detector → ISBN string
                                            │
                                            ▼
                                    BookClient.getBooksOnGoogleBooks
                                            │
                                            ▼
                                    Book(JSONObject volumeInfo)
                                            │
                                            ▼
                                    Pre-filled "Add book" form
```

The user sees the book details appear seconds after pointing the camera — no typing required.

### 2. A two-source metadata strategy

`BookClient` exposes both Google Books *and* Open Library — same async interface, two backends. The Google Books response is parsed in the `Book(JSONObject)` constructor, which knows how to unwrap nested `volumeInfo`, walk `industryIdentifiers` for ISBN-10 vs ISBN-13, and join `authors` / `categories` arrays into display strings. Adding a third provider would mean a new method on `BookClient` and a parallel constructor.

### 3. Data model that takes "library" seriously

Most book-tracker apps treat *library* as a single bucket. Here the model recognizes that one physical book can be:
- in one library (e.g. *Home*) on a specific shelf,
- duplicated in another library (e.g. *Office*) as a separate copy,
- moved between shelves over time.

The `Book ↔ Library` relationship is materialized through `BookOnLibrary`, with its own status field. It's a small piece of design that prevents the data model from collapsing as soon as the user has more than one bookshelf.

---

## Project structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/taiuti/personallibrary/
│   ├── SplashActivity, MainActivity, BookActivity, CameraActivity, BarcodeCaptureActivity
│   ├── BooksFragment, LibrariesFragment, SearchFragment, NavigationDrawerFragment
│   ├── BookAdapter                       # RecyclerView adapter for the book list
│   ├── model/
│   │   ├── Book.java                     # parses Google Books JSON
│   │   ├── Library.java, Libraries.java
│   │   ├── BookOnLibrary.java            # Book ↔ Library relationship + status
│   │   └── Status.java
│   └── core/
│       ├── PersonalLibrary.java          # Application class
│       ├── BookClient.java               # Google Books + Open Library
│       ├── Books.java, Libraries.java    # in-memory collections
│       └── ResourceHelper.java
├── java/com/google/android/gms/samples/vision/barcodereader/
│   └── (Google Vision sample code, adapted for ISBN scanning)
└── res/
    ├── layout/        # activity & fragment layouts
    ├── menu/          # drawer + action bar menus
    ├── values/        # English strings, styles, colors
    └── values-it/     # Italian translation
```

---

## Planned modernization

The roadmap for this repo isn't "polish the 2018 code" — it's a **Kotlin rewrite** that keeps the user-facing app the same and rebuilds the internals against the current Android platform. The 2018 Java snapshot is preserved so the before/after is visible.

Target stack for the rewrite:

| Concern | 2018 (Java) | Target (Kotlin) |
|---|---|---|
| Language | Java 7-style | **Kotlin** with coroutines and `Flow` |
| HTTP | `AsyncHttpClient` (loopj, deprecated) | **Retrofit + OkHttp**, type-safe API definitions |
| Concurrency | callbacks + `AsyncTask`-style | **structured concurrency** via `suspend` / `Flow` |
| JSON parsing | hand-rolled in `Book(JSONObject)` | **Moshi** or `kotlinx.serialization`, parser decoupled from the model |
| Dependency injection | none | **Hilt** |
| Persistence | in-memory collections | **Room** + repository layer |
| Navigation | Activity + Fragment + custom drawer | **Jetpack Navigation** + single-Activity pattern |
| Support libs | Android Support Library | **AndroidX** |
| UI (stretch) | XML layouts | **Jetpack Compose** |
| Camera / barcode | Google Vision (legacy) | **CameraX** + **ML Kit Barcode Scanning** |
| Build | (Gradle files missing in this snapshot) | Kotlin DSL `build.gradle.kts`, version catalogs |

What I'm explicitly **removing** in the rewrite:

- The AdMob / AppLovin / Facebook SDK integrations — they came from a monetization experiment that never paid for its own complexity.
- The cleartext `http://openlibrary.org/` endpoint — HTTPS only.

The goal isn't a green-field rewrite that abandons the original; it's to show I can take an aging codebase, identify what was good (the *data model*, the *two-source metadata strategy*, the *camera→ISBN→form* pipeline) and what's load-bearing legacy, and migrate it deliberately. That's a more honest portfolio piece than yet another tutorial-fresh app.

---

## Build & run

This snapshot ships the `app/` source module but **does not include the Gradle build files** (`build.gradle`, `settings.gradle`, `gradle/`). To run it today you'd need to:

1. Create an Android Studio project around the `app/` module.
2. Add a top-level `build.gradle` and `settings.gradle` targeting a recent Android Gradle Plugin.
3. Migrate the support-library imports to AndroidX (Android Studio's *Refactor → Migrate to AndroidX…* handles most of it).
4. Replace the deprecated `loopj` `AsyncHttpClient` calls — or add a transitive dependency stub if you want to keep the original code intact.
5. Provide your own AdMob / AppLovin / Facebook SDK keys (the ones in `AndroidManifest.xml` are 2018 placeholders).

The code is here for reading more than running.

---

## Credits

- **Filippo Taiuti** — design and implementation.
- **Google Vision Barcode Sample** — adapted under Apache 2.0 (see `com/google/android/gms/samples/vision/barcodereader/`).
- Book metadata from [Google Books API](https://developers.google.com/books) and [Open Library](https://openlibrary.org/developers/api).
