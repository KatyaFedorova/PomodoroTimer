# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Kotlin Multiplatform + Compose Multiplatform Pomodoro timer app ("TomatoPaws" / "Kittidoro") targeting **Android, iOS, and Web (Kotlin/Wasm)**. Almost all logic and UI is shared in `composeApp/src/commonMain`; `iosApp/` is a thin SwiftUI host plus native-only features (Live Activities, Firebase Auth bridge).

## Build & Run

Gradle wrapper (`./gradlew`) drives Android and Web. iOS is built through Xcode (or XcodeBuildMCP).

```bash
# Web (Kotlin/Wasm) — dev server with hot reload
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web production bundle (output: composeApp/build/dist/wasmJs/productionExecutable)
./gradlew wasmJsBrowserDistribution

# Android debug APK (installs app id org.timer.dev, name "TomatoPawsDev")
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# All checks
./gradlew check
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode. The shared Kotlin code compiles into a static framework named `ComposeApp` (see `composeApp/build.gradle.kts`); Xcode builds it via a run-script phase. Schemes/targets are split **Dev** vs **Prod** (`TimerWidgetDev`/`TimerWidgetProd`, `Configuration/Dev.xcconfig` vs `Prod.xcconfig`).

### Tests

Tests live in `composeApp/src/commonTest` and run on all targets.

```bash
./gradlew :composeApp:allTests                      # every target
./gradlew :composeApp:testDebugUnitTest             # Android (JVM) — fastest for TDD
./gradlew :composeApp:wasmJsTest                    # Wasm

# Single test class / method (Android target)
./gradlew :composeApp:testDebugUnitTest --tests "org.timer.main.auth.auth.LoginUseCaseTest"
./gradlew :composeApp:testDebugUnitTest --tests "*.LoginUseCaseTest.*specific method*"
```

Tests use `kotlin.test`, `kotlinx-coroutines-test`, and the **truthish** assertion library. The established pattern is use-case / view-model tests backed by hand-written `Fake*` gateways (e.g. `FakeAuthGateway`, `FakeGameGateway`, `FakeSystemClock`) — prefer fakes over mocking frameworks to match existing tests.

## Architecture

### Layering (Clean Architecture)
Per feature, code is split between a **UI/presentation** package and a **domain** package:
- `org.timer.main.<feature>` — Compose screens + `ViewModel`s (e.g. `org.timer.main.timer`, `.projects`, `.auth`).
- `org.timer.main.domain.<feature>` — `UseCase`s and `Gateway` interfaces (e.g. `org.timer.main.domain.timer`, `.project`, `.auth`).

ViewModels depend on UseCases; UseCases depend on Gateway **interfaces**. Concrete gateways are Firebase-backed (`Firebase*Gateway`) or settings-backed (`PersistentSettingsGateway`) and are bound in DI.

Feature domains (each with matching UI + `domain` packages): `timer`, `projects`, `auth`, `team`, `statistic`, `feedback`, `progress`, `settings`, `breakactivity` (+ `domain.exercise`), plus cross-cutting `domain.game` (gamification), `domain.video`, and `domain.SystemClock` / `domain.IDGenerator`.

### Dependency Injection (Koin)
`composeApp/src/commonMain/kotlin/org/timer/DI.kt` is the single source of truth:
- `appModule()` registers all ViewModels, UseCases, and Gateways. **Register new ViewModels/UseCases/Gateways here.** Many use-case interfaces follow the `XUseCase` interface ↔ `DefaultXUseCase` implementation convention.
- `expect fun platformSpecificModule(): Module` has `actual`s in `Koin.android.kt`, `Koin.ios.kt`, `Koin.wasmJs.kt` for platform bindings.
- `initializeKoin()` / `afterKoinStarted()` wire startup. On iOS, `IOSViewModelProvider` exposes the `AuthViewModel` to Swift.
- Note: `AuthViewModel` is a singleton (`singleOf`); other ViewModels are `viewModelOf`.

### Entry points
- Common: `App.kt` → `CommonMainScreen.kt`, which branches to `MobileMainScreen` vs `WebMainScreen` (responsive via `WindowInfo`).
- Android: `MainActivity.kt`; iOS: `MainViewController.kt`; Web: `main.kt`.

### expect/actual
Platform-specific behavior (alarms, video player, settings storage, clock) uses `expect`/`actual` — search for `expect` declarations in `commonMain` and find matching `actual`s in `androidMain` / `iosMain` / `wasmJsMain` (files typically suffixed `.ios.kt`, `.android.kt`, `.wasmJs.kt`). Note: `composeApp/src/main`, `debug`, and `release` are the **Android** host source sets (manifest, res, signing), separate from the shared `androidMain`.

### iOS Live Activities
iOS-only timer Live Activity / Dynamic Island lives entirely in Swift under `iosApp/iosApp/` (`LiveActivityScheduler.swift`, `GeneratedLiveActivityBridge.swift`, `MinimalLiveActivityView.swift`) and the widget extensions (`TimerWidgetDev/`, `TimerWidgetProd/`). Keep `TimerWidgetsAttributes.swift` in sync across Dev and Prod widget targets.

## Web / Firebase config

The Wasm app reads `composeApp/src/wasmJsMain/resources/firebaseConfig.js`. This file is **environment-swapped at build time** — CI copies `firebaseConfig.prod.js` (or `.dev.js`) over it. Don't hardcode prod values into `firebaseConfig.js`. Firebase project: `kittidoro-timer`.

CI (`.github/workflows/`) deploys the Wasm bundle to Firebase Hosting: `firebase-hosting-merge.yml` (prod, on push to `main`), `-development.yml`, and `-pull-request.yml`. The prod job also publishes `.well-known/assetlinks.json` (Android App Links) and `apple-app-site-association` (iOS Universal Links) from the `*.prod` resource files.
