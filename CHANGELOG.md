<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# dotenvx-jetbrains-plugin Changelog

## [Unreleased]

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

