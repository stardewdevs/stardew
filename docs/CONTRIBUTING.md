# Contributing to Stardew

Thank you for considering contributing to Stardew! We welcome contributions of all kinds, including bug reports, feature suggestions, documentation improvements, and code contributions.

## How to Contribute

### Reporting Bugs

If you find a bug, please open an issue with the following information:

- A clear and descriptive title
- Steps to reproduce the bug
- Expected behavior
- Actual behavior
- Screenshots or screen recordings (if applicable)
- Device information (Android version, device model)

### Suggesting Features

Feature suggestions are welcome! Please open an issue with:

- A clear and descriptive title
- A detailed description of the feature
- Why this feature would be useful
- Any potential implementation ideas (optional)

### Code Contributions

We welcome code contributions. Please follow these steps:

1. **Fork the repository** on GitHub
2. **Clone your fork** locally
3. **Create a feature branch** from `main`
4. **Make your changes** following our coding standards
5. **Test your changes** to ensure they work
6. **Commit your changes** with a clear commit message
7. **Push to your fork** and open a pull request

## Development Setup

### Prerequisites

- Android Studio Ladybug or newer
- Android SDK 34+
- Android NDK 26+
- Rust toolchain with cargo-ndk

### Building the Project

```bash
# Clone the repository
git clone https://github.com/stardewdevs/stardew.git
cd stardew

# Build the APK
./gradlew assembleDebug
