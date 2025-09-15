<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# dotenvx-jetbrains-plugin Changelog

## [Unreleased]

## [0.1.15] - 2025-09-15

### Added

- Add key pairs cache: avoid the plugin to read key store frequently

## [0.1.14] - 2025-09-13

### Added

- Add inspection for key name with `password`, `private`, `key`, `token`, `secret`, `credential` with plain value

## [0.1.13] - 2025-09-12

### Added

- Add gutter icon for encrypted variable
- Add high light for encrypted value
- Add gutter icon for a public key

## [0.1.12] - 2025-09-12

### Added

- Add gutter icon for encrypted variable

## [0.1.11] - 2025-09-10

### Added

- Add icon for Dotenvx intention actions

## [0.1.10] - 2025-09-07

### Added

- Paste and encrypt support: encrypt pasted data after a key: `API_KEY=<caret>` if key name contains `password`,
  `secret`, `key`, `private`, `token`, `credential`.

## [0.1.9] - 2025-09-05

### Fixed

- Wrong dotenv header for `.env` file
- Trim input value

## [0.1.8] - 2025-09-04

### Added

- Add YAML support: `application.yaml` is ready for Spring Boot

### Fixed

- Fixed: wrong target directory to create dotenvx file

## [0.1.7] - 2025-09-02

### Added

- Edit encrypted value by intention action

## [0.1.6] - 2025-08-19

### Added

- Add replacement support when adding new Dotenvx variable
- Change value to TextArea for new Dotenvx variable

## [0.1.5] - 2025-08-19

### Added

- Support new `.env.keys.json` file format

## [0.1.4] - 2025-08-16

### Fixed

- Fixed: working directory with the "file://" prefix
- Fixed: private key length problem

## [0.1.3] - 2025-08-15

### Added

- Create new `Dotenvx file` from `New` menu
- Inlay hints for public key in `.env` and `.properties` files

## [0.1.2] - 2025-08-12

### Added

- Inlay hints for encrypted variables in `.env` and `.properties` files
- Create an encrypted variable from the editor's popup menu
- Run configuration with Dotenvx support: Java, Node.js, Python, PHP, Ruby, Go.
- Terminal environment customizer to load variables from .env by Dotenvx
- Generate and update public key for normal `.env` and `.properties` files from `Generat` popup menu
- Intention action to encrypt a variable in the editor

