# Stardew

A modern terminal for Android.

[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE.md)
[![Android API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)

## Overview

Stardew is a complete reimagining of the terminal experience on Android. Built from the ground up with modern technologies, it combines the power of a full Linux environment with a fluid, GPU-accelerated interface.

## Features

- **GPU-Accelerated Rendering** — Vulkan-powered terminal rendering for smooth 120Hz scrolling and animations
- **Rust Terminal Engine** — Powered by the battle-tested alacritty_terminal crate
- **Custom Package Manager** — `mpkg` with atomic transactions and instant rollback
- **glibc Compatibility** — Run standard Linux binaries without patching
- **FHS-Compliant Filesystem** — Standard paths (/bin, /etc, /usr)
- **Plugin System** — Extend with TypeScript, JavaScript, or JSON
- **Localhost Emulator** — Run full operating systems locally
- **Jetpack Compose UI** — Material You dynamic theming
- **Tabbed Sessions** — Multi-session support with split-pane layout
- **Hardware Access** — Camera, microphone, sensors, BLE integration

## Building

See [Building from Source](docs/developer/building.md).

## License

GPLv3. See [LICENSE.md](LICENSE.md).
