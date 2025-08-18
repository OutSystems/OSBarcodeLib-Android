# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The changes documented here do not include those from the original repository.

## [1.2.1]

### 20205-08-18

- Make MLKit scanner initialization lazy to prevent memory consumption overload (https://outsystemsrd.atlassian.net/browse/RMET-3481)

### 2025-06-24

- Migrate publishing from OSSRH to Central Portal.

### 28-01-2025
- Add `publish-android` workflow to publish library under `io.ionic.libs` in Maven (https://outsystemsrd.atlassian.net/browse/RMET-3983)

## [1.2.0]

### 02-12-2024
- Chore: Bump Kotlin and Gradle versions (https://outsystemsrd.atlassian.net/browse/RMET-3887).

### 13-11-2024
- Feature: Support Edge-to-Edge on all Android versions.

## [1.1.5]

### 08-11-2024
- Fix: Update libraries for supporting 16KB page size (https://outsystemsrd.atlassian.net/browse/RMET-3602)

### 05-11-2024
- Fix: Edge-to-edge support on Android 15 (https://outsystemsrd.atlassian.net/browse/RMET-3597)

## [1.1.4]

### 08-10-2024
- Fix: Make Scanner view wider on landscape and on tablets (https://outsystemsrd.atlassian.net/browse/RMET-3682).

## [1.1.3]

### 22-08-2024
- Fix: Avoid UI bug on background when layout is portrait (https://outsystemsrd.atlassian.net/browse/RMET-3379).

## [1.1.2]

### 21-05-2024
- Fix: Adds serializable annotation to avoid problems with code obfuscation (https://outsystemsrd.atlassian.net/browse/RMET-3394).

## [1.1.1]

### 30-04-2024
- Fix: Improve scanning by using higher resolution frames (https://outsystemsrd.atlassian.net/browse/RMET-3399).

## [1.1.0]

### 26-03-2024
- Add zoom options (https://outsystemsrd.atlassian.net/browse/RMET-2987).

### 22-02-2024
- Update `github_actions.yml` file steps versions (https://outsystemsrd.atlassian.net/browse/RMET-2568).

## [1.0.0]

### 09-01-2024
Android - Udpate error codes and messages (https://outsystemsrd.atlassian.net/browse/RMET-3037)

### 19-12-2023
Android - Run image analysis outside of the main/UI thread (https://outsystemsrd.atlassian.net/browse/RMET-2912)

### 06-12-2023
Android - Implement scanning only the frame area (Portrait, Landscape, Adaptive) (https://outsystemsrd.atlassian.net/browse/RMET-2912)

### 06-12-2023
Android - Implement Scanner screen for Tablets (Portrait, Landscape, Adaptive) (https://outsystemsrd.atlassian.net/browse/RMET-2912)

### 30-11-2023
Android - Implement Scanner screen for Phones (Portrait, Landscape, Adaptive) (https://outsystemsrd.atlassian.net/browse/RMET-2770)

### 17-11-2023
Android - Implement Scan Orientation (Portrait, Landscape, Adaptive) (https://outsystemsrd.atlassian.net/browse/RMET-2763)

### 16-11-2023
Android - Implement Scan Button (https://outsystemsrd.atlassian.net/browse/RMET-2762)

### 15-11-2023
Android - Implement Scan Instructions (https://outsystemsrd.atlassian.net/browse/RMET-2761)

### 14-11-2023
Android - Implement Torch Button to settings (https://outsystemsrd.atlassian.net/browse/RMET-2759)

### 13-11-2023
Android - Implement AlertDialog to settings (https://outsystemsrd.atlassian.net/browse/RMET-2764)

### 13-11-2023
Android - Select Camera (Back or Front) (https://outsystemsrd.atlassian.net/browse/RMET-2764)

### 10-11-2023
Android - Use both libraries dynamically (https://outsystemsrd.atlassian.net/browse/RMET-2895)

### 09-11-2023
Android - Scan barcode feature using ML Kit (https://outsystemsrd.atlassian.net/browse/RMET-2894)

### 06-11-2023
Android - First implementation of the scan barcode feature using zxing (https://outsystemsrd.atlassian.net/browse/RMET-2758)