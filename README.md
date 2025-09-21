dotenvx-jetbrains-plugin
========================

<!-- Plugin description -->
Dotenvx is a JetBrains plugin that provides inlay hints for encrypted variables in `.env` files
by [Dotenvx](https://dotenvx.com/).

With this plugin, and decrypted values will be displayed as inlay hints in the editor,
making it easier to work with sensitive data without exposing it directly in the code.

Features:

- Create new `Dotenvx .env/properties/yaml file/xml` from `New` menu
- Inlay hints for encrypted variables in `.env/properties/yaml/xml` files
- Create an encrypted variable from the editor's popup menu or `Generate` menu
- Paste and encrypt support: encrypt pasted data after a key `API_KEY=<caret>` if key name contains `password`,
  `secret`, `key`, `private`, `token`, `credential`
- Run configuration with Dotenvx support: Java, Node.js, Python, PHP, Ruby, Go.
- Terminal environment customizer to load variables from .env by Dotenvx
- Generate and update public key for normal `.env/.properties/xml` files from `Generate` popup menu
- Intention action to encrypt plain value in the editor
- Intention action to edit encrypted value in the editor
- Gutter icon for encrypted variables
- Native support for Spring Boot `application.properties` or `application.yaml`

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "dotenvx"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/28148-dotenvx) and install it by clicking
  the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/28148-dotenvx/versions) from
  JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/linux-china/dotenvx-jetbrains-plugin/releases/latest) and install it
  manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---

# Credits

- Better Direnv: https://plugins.jetbrains.com/plugin/19275-better-direnv
- EnvFile: https://plugins.jetbrains.com/plugin/7861-envfile

# References

* [Dotenvx](https://dotenvx.com/)
* [dotenvx-rs](https://github.com/linux-china/dotenvx-rs): Dotenvx CLI written in Rust
* [Dotenvx JetBrains Plugin](https://plugins.jetbrains.com/plugin/28148-dotenvx/): Dotenvx JetBrains IDE plugin
* Inlay Hints: https://plugins.jetbrains.com/docs/intellij/inlay-hints.html
* Plugin Compatibility with IntelliJ Platform
  Products: https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html