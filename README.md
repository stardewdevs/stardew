# Stardew

[Stardew](https://github.com/stardewdevs/stardew) is a modern Android terminal application and Linux environment built with Kotlin and Rust.

Note that this repository is for the app itself (the user interface and the terminal emulation). For the packages installable inside the app, see [stardewdevs/stardew-packages](https://github.com/stardewdevs/packages).

Quick how-to about Stardew package management is available at [Package Management](https://github.com/stardewdevs/stardew-packages/wiki/Package-Management). It also has info on how to fix repository is under maintenance or down errors when running apt or pkg commands.

We are looking for Stardew Android application maintainers.

---

## Contents

- [Fixed Issues](#fixed-issues)
- [Features](#features)
- [Technical Architecture](#technical-architecture)
- [Why Stardew?](#why-stardew)
- [Installation](#installation)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgements](#acknowledgements)

---

## Fixed Issues

Stardew addresses several limitations of traditional terminal emulators on Android that have been reported and documented by the community over many years. These issues range from fundamental architectural decisions to compatibility problems with modern Android versions.

### Android 12+ Phantom Process Killer

Android 12 introduced aggressive background process management that limits the number of processes that can run in the background. This limit applies to all applications combined, not just individual apps. When this limit is exceeded, the system begins terminating processes to free up resources. Termux users frequently encounter this as the shell process being killed unexpectedly, often accompanied by the message `[Process completed (signal 9)]`. This makes it impossible to run long-running tasks such as servers, downloads, or compilations in the background. Stardew resolves this by using a permanent foreground service with proper WorkManager integration, ensuring terminal sessions remain active even when the app is in the background. The foreground service displays a persistent notification that informs the user that Stardew is running, and the WorkManager handles periodic tasks efficiently without triggering the phantom process killer.

### dpkg Failures and Package Management Issues

The combination of apt and dpkg used in Termux is prone to a wide variety of failures. These include the infamous dpkg trigger errors, configuration failures, and post-installation script errors. Many of these failures occur because Termux is missing tools and paths that Debian packages expect, such as ldconfig, standard FHS paths, and full POSIX shell environments. The maintainer scripts that run during package installation frequently fail, leaving the system in an inconsistent state. Stardew replaces this fragile system with mpkg, a custom package manager written in Rust. Mpkg uses declarative JSON manifests instead of shell scripts for package metadata and installation instructions. This eliminates the failure-prone maintainer scripts entirely. The installation process is atomic, meaning that if any step fails, the entire transaction is rolled back to a consistent state, leaving the system untouched. This approach eliminates the common scenario where a failed package installation leaves the package manager in a broken state requiring manual intervention.

### Bionic Libc Incompatibility

Android's Bionic libc is a custom C library designed for embedded systems. It is smaller and faster than glibc but lacks many functions and features that standard Linux programs expect. This causes a wide range of compatibility issues. Statically linked programs compiled for glibc often fail to run due to missing symbols or incompatible system call implementations. Dynamically linked programs require recompilation against Bionic, which is not always possible. Tools like gettext and iconv are either missing or incomplete. The absence of full locale support breaks many internationalization features. Stardew bundles glibc alongside the application and uses an LD_PRELOAD-based mechanism to redirect standard library calls, allowing standard Linux binaries to run without modification. This approach has been validated by projects like glibc-packages and various Linux-on-Android solutions.

### Non-FHS Filesystem Layout

Termux uses a non-standard filesystem layout with a custom prefix path, typically `/data/data/com.termux/files/usr`. This deviates from the Filesystem Hierarchy Standard, which specifies standard locations like `/bin`, `/etc`, `/usr`, and `/lib`. This non-standard layout causes numerous problems. Build scripts and configure scripts often hardcode paths like `/usr/bin` or `/etc`, which do not exist in Termux. Package maintainers must patch software to use the correct paths. Many tools fail to find configuration files or libraries because they are not in the expected locations. Environment variables like PATH must be manually set to include the prefix. Stardew implements a fully FHS-compliant layout with standard paths such as `/bin`, `/etc`, `/usr`, and `/lib`, all mapped to appropriate locations within the application's private storage. This ensures compatibility with the vast majority of Linux software without requiring patches or modifications.

### Janky Scrolling and Poor Rendering Performance

Termux uses the Android Canvas API for terminal rendering, which is CPU-bound and not optimized for the high update rates required by terminal applications. This results in choppy scrolling, especially when displaying large amounts of text or running applications with complex terminal output such as ncurses-based tools, text editors with syntax highlighting, or applications with fast-scrolling log output. The performance limitations become particularly noticeable on larger screens or when using split-pane views. Stardew leverages Vulkan GPU acceleration for terminal rendering, providing smooth 120Hz scrolling and fluid animations. The renderer uses a glyph cache to minimize GPU work and supports efficient incremental updates to the terminal grid. This approach provides a responsive and pleasant user experience even during intensive terminal activity.

### Android 15 and 16 Compatibility

Android 15 (SDK 35) and Android 16 (SDK 36) have introduced significant changes that affect terminal emulators. Network access restrictions prevent many networking tools from functioning correctly. The apt and pkg commands fail due to network access being blocked. Security hardening measures prevent the execution of downloaded code through mechanisms such as stricter seccomp policies and improved SELinux enforcement. The phantom process killer limits are further tightened. Termux has been reported broken on these newer versions. Stardew is actively maintained to support the latest Android versions, with continuous updates ensuring compatibility with new Android releases. The development cycle includes regular testing on the latest Android versions and immediate fixes for any compatibility issues that arise.

### Broken Hardware API Access

Termux:API, which provides access to Android hardware features like camera, microphone, and fingerprint sensor, is a separate add-on application that must be installed independently. This design creates several problems. The Termux:API application frequently breaks on newer Android versions due to API changes. Users must install multiple applications to get full functionality. The API communication uses intents, which are slow and unreliable. Features like fingerprint access are broken on Android 14 and later. The camera capture takes 5-10 seconds to complete. Microphone recording has high latency. Stardew integrates hardware access directly into the main application using modern APIs. Camera access uses CameraX for fast and reliable capture. Microphone access uses AudioRecord for low-latency recording. Fingerprint access uses BiometricPrompt for compatibility with the latest Android versions. The integration provides a unified and reliable API that is always up to date.

### No Native Plugin System

Termux offers no native plugin system, limiting extensibility to shell scripts and manual cloning of repositories. This makes it difficult for users to share and install extensions. Themes require manual editing of configuration files. Keybindings are hardcoded. There is no central repository for community contributions. Stardew includes a native plugin system supporting TypeScript, JavaScript, and JSON. The plugin manager provides one-click installation from a community repository. Plugins can extend the terminal with custom commands, key bindings, themes, color schemes, and additional functionality. The plugin system supports hot reloading, allowing plugins to be loaded and unloaded without restarting the application.

---

## Features

Stardew is built from the ground up with a modern architecture that prioritizes performance, reliability, and extensibility.

### Terminal Engine

The terminal engine is implemented in Rust using the battle-tested alacritty_terminal crate. This crate provides robust escape sequence parsing, grid management, and terminal state handling. It supports the full VT100 and xterm escape sequence sets, including advanced features like true color, bracketed paste, and terminal queries. The Rust implementation provides memory safety and performance benefits over the Java-based implementation used in Termux. The engine handles terminal resize operations efficiently and supports large terminal buffers for scrolling back through output history.

### GPU-Accelerated Rendering

The renderer uses Vulkan to provide GPU-accelerated rendering, delivering smooth 120Hz scrolling and fluid animations. The renderer caches individual glyphs as Vulkan textures, minimizing GPU work for repeated characters. TrueColor support enables 24-bit color output for vibrant terminal graphics. P3 wide gamut support provides more accurate colors on supported displays. Font ligatures are supported for programming fonts like Fira Code and JetBrains Mono. The renderer handles terminal resize and font size changes efficiently without noticeable lag.

### Package Management

Package management is handled by mpkg, a custom package manager written in Rust. mpkg uses declarative JSON manifests instead of maintainer scripts, eliminating the failure-prone dpkg system. The manifests specify package metadata, dependencies, conflicts, and installed files. The installation process uses atomic transactions with instant rollback. If any step of an installation fails, the entire transaction is rolled back, leaving the system in a consistent state. Parallel downloads support up to eight simultaneous downloads, significantly reducing installation time. Multiple repository mirrors with automatic failover provide reliability. The SQLite database provides fast and reliable package tracking.

### Filesystem and libc

The filesystem follows FHS standards with standard paths including `/bin`, `/etc`, `/usr`, `/lib`, and `/tmp`. The filesystem layout provides compatibility with standard Linux software. sglibc is bundled alongside the application, allowing standard Linux binaries to run without patching. PRoot is included for running full Linux distributions without root access. The filesystem supports standard POSIX permissions and ownership, ensuring compatibility with tools that rely on these features.

### User Interface

The user interface is built with Jetpack Compose, providing a modern and responsive UI. Material You dynamic theming adapts to the user's wallpaper and system settings. The interface supports both light and dark themes with seamless transitions. Tabbed sessions with split-pane layout provide efficient multi-session management. Users can open multiple terminal sessions in separate tabs and split the screen horizontally or vertically to view multiple sessions simultaneously. The extra keys bar is fully customizable with drag-to-rearrange support. Keys can be added, removed, and reordered to match the user's preferences. Context-aware key display shows additional keys when editing in specific applications like vim or emacs. Pinch-to-zoom provides smooth font size adjustment. Touch gestures include swipe to switch tabs and three-finger tap to paste.

### Plugin System

The plugin system supports TypeScript, JavaScript, and JSON plugins. Plugins can extend the terminal with custom commands, key bindings, themes, and additional functionality. The plugin manager provides one-click installation from a community repository. Plugins support hot reloading, allowing them to be loaded and unloaded without restarting the application. The plugin API provides access to terminal state, input handling, and output processing. Themes can customize colors, fonts, and other visual aspects. Custom commands can extend the command palette with user-defined actions.

### Hardware Access

Hardware access is integrated directly into the application, eliminating the need for a separate Termux:API add-on. Camera access uses CameraX for fast and reliable image capture. Microphone access uses AudioRecord for low-latency audio recording. Fingerprint access uses BiometricPrompt for compatibility with the latest Android versions. Real-time sensor data is available from accelerometer, gyroscope, and compass. Bluetooth Low Energy support enables communication with BLE devices. NFC read and write support allows interaction with NFC tags. USB host support provides access to connected peripherals. WakeLock support keeps the device awake during long-running tasks.

### Localhost Emulator

The localhost emulator is included for running full operating systems locally. It supports Android, Windows, macOS, and Linux distributions. Access is provided via ADB, SSH, VNC, and RDP. The emulator uses QEMU with TCG software emulation and hardware acceleration when available. VM templates provide one-click creation for common operating systems. The emulator supports ARM64, x86, and x86_64 architectures. Networking is provided via user-mode networking with port forwarding. VM snapshots allow saving and restoring VM states.

---

## Technical Architecture

The application is organized into four main modules, each with a specific responsibility and clear interfaces between them.

### stardew-app

The main Android application module written in Kotlin. It contains the main activity, Compose UI, service management, and application lifecycle handling. This module is responsible for user interaction and orchestrating the other modules. It manages the foreground service that maintains terminal sessions, handles Android permissions, and coordinates the launch and termination of terminal sessions. The module uses modern Android architecture components including ViewModel for state management and LiveData or Flow for reactive updates.

### stardew-emulator

The terminal emulation module written in Rust. It uses the alacritty_terminal crate for escape sequence parsing and grid management, with JNI bindings exposed via the jni-rs crate. This module handles all terminal I/O and session state management. It processes input from the keyboard and other input devices, sends it to the PTY, receives output from the PTY, parses escape sequences, and maintains the terminal grid with character attributes, colors, and cursor position. The module also handles terminal resizing, selection, and clipboard operations.

### stardew-view

The rendering module written in Rust with Vulkan support. It handles GPU-accelerated terminal rendering, glyph caching, and display management. This module interfaces with the Android surface via native window bindings. The renderer receives the terminal grid from the emulator module, computes the visual representation of each cell, caches glyphs as Vulkan textures, and renders the complete grid to the screen. The module supports smooth scrolling by efficiently updating only changed cells.

### stardew-shared

A shared library module written in Kotlin. It provides constants, utilities, and helpers used across the other modules, including permission handling, file utilities, and application constants. This module contains no Android-specific code beyond standard library dependencies, making it reusable across different Android components.

### Native PTY Layer

The PTY layer uses the portable-pty crate for PTY creation and management and the rustix-openpty crate for low-level PTY operations. The JNI bridge is implemented using the jni-rs crate, providing safe and efficient communication between Kotlin and Rust. The PTY layer handles the creation of pseudoterminals, spawning of shell processes, input and output handling, and process monitoring.

---

## Why Stardew?

Termux pioneered the concept of running a Linux environment on Android and remains a widely used and respected application. However, it was built on technologies and assumptions that have not aged well in the rapidly evolving Android ecosystem.

The Android platform has undergone significant changes since Termux was first released in 2015. Android 12 introduced aggressive background process killing that renders Termux unreliable for long-running tasks. Android 15 and 16 have introduced further restrictions that break core functionality. Security hardening measures have made it increasingly difficult to run command-line tools on Android.

The underlying technologies used by Termux have also aged. The Java-based terminal emulator is slower and less memory-efficient than modern alternatives. The use of dpkg and apt for package management, inherited from Debian, introduces complexity and failure modes that are difficult to avoid on Android. The reliance on Bionic libc creates compatibility issues that require extensive patching of standard Linux software.

Stardew addresses these challenges by building on modern foundations. The use of Rust provides memory safety and performance that is difficult to achieve with C and C++. The adoption of Kotlin and Jetpack Compose brings the user interface into the modern era of Android development. The switch from Bionic to glibc resolves a decade of compatibility issues with standard Linux binaries. The replacement of dpkg with mpkg eliminates the most common source of package management failures. The integration of hardware access into the main application provides a unified and reliable experience.

The result is a terminal emulator that is not just a successor to Termux, but a complete reimagining of what a mobile terminal can be. It combines the power of a full Linux environment with modern Android development practices, providing a reliable, performant, and extensible platform for mobile development.

---

## Installation

The latest version is v0.1 (beta). The app is currently in active development. If you encounter any issues, please open an issue and we will respond.

APKs are available for download from the Releases section on GitHub. Installation requires Android 8.0 (API 26) or newer. The application is signed with a release key that is consistent across all builds from the same source. For development builds, the debug key is used.

### System Requirements

Stardew requires Android 8.0 (API 26) or newer. The application has been tested on Android 8.0 through Android 15 and is actively maintained for Android 15 and later. The application supports ARM64 (aarch64), ARMv7 (armeabi-v7a), x86 and x86_64 architectures.

### Recommended Setup

For the best experience with Stardew, we recommend Android 10 or newer with at least 2GB of RAM. A device with Vulkan support provides the best graphical performance, but the application will fall back to software rendering if Vulkan is not available. For package management, a stable internet connection is recommended for accessing package repositories.

---

## Building from Source

### Prerequisites

Building Stardew from source requires the following tools.

Android Studio Ladybug or newer is required for building the Android application. The Android SDK 34 or newer must be installed and configured. The Android NDK 26 or newer is required for building the native components. The Rust toolchain with cargo-ndk installed is required for building the Rust components. Git is required for cloning the repository.

### Build Steps

Clone the repository and navigate to the project directory. Build the APK using the Gradle wrapper. The resulting APK will be placed in the build output directory.

```bash
git clone https://github.com/stardewdevs/stardew.git
cd stardew
./gradlew assembleDebug
./gradlew installDebug
```

### Building Rust Components

The Rust components must be built separately using the cargo-ndk tool. Install cargo-ndk and build for the target architecture. The built library will be placed in the appropriate jniLibs directory.

```bash
cargo install cargo-ndk
cd stardew-emulator
cargo ndk -t universal -o ../stardew-app/src/main/jniLibs build --release
```

### Building the Full Package

For a complete build, the application and all plugins must be built from source. The main application includes references to plugin modules that must be built separately. The recommended approach is to build all components using the Gradle wrapper, which coordinates the build of all modules.

---

## Contributing

We welcome contributions of all kinds, including bug reports, feature suggestions, documentation improvements, and code contributions.

### Code of Conduct

Stardew is committed to fostering an open and welcoming community. All contributors are expected to adhere to the Code of Conduct. Please report any unacceptable behavior to the project maintainers.

### How to Contribute

Fork the repository on GitHub and create a feature branch from the main branch. Make your changes and ensure that all tests pass. Submit a pull request with a clear description of the changes. The pull request will be reviewed by maintainers and merged after approval.

### Areas Needing Help

UI and UX design is an ongoing need for the project. Rust development, particularly in the terminal engine and rendering modules, is another area where contributions are welcome. Package packaging for the stardew-packages repository is an ongoing effort. Documentation improvements and testing across different devices and Android versions are always needed.

---

## License

Stardew is licensed under the Apache 2.0q License. This license applies to all source code in this repository. By using, modifying, or distributing this software, you agree to the terms of the Apache 2.0 License.

### Commercial Licensing

For commercial licensing options, please contact the project maintainers. The Apache 2.0 license does not permit proprietary use of the software unless the source code is made available under the same license.

---

## Acknowledgements

Stardew is inspired by the pioneering work of the Termux project and its contributors. We thank the Termux team for their contributions to the Android open-source ecosystem and for demonstrating what is possible on the Android platform.

### Third-Party Libraries

Stardew uses several open-source libraries and components. Alacritty Terminal provides the terminal engine. Portable Pty provides PTY creation and management. JNI RS provides Rust JNI bindings. Vulkan and Vulkano provide GPU-accelerated rendering. Jetpack Compose provides the user interface. MPKG is developed as part of the Stardew project.

### Contributors

The Stardew project is grateful to all contributors who have helped with development, testing, documentation, and support. A complete list of contributors is available in the CONTRIBUTORS.md file.

---

### Thanks you for reading