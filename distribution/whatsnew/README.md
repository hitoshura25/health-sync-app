# Play Store Release Notes

This directory contains release notes for Google Play Store deployments.

## Structure

Each locale has its own directory with a `whatsnew` file (no extension):

```
whatsnew/
├── en-US/
│   └── whatsnew
├── de-DE/
│   └── whatsnew
├── es-ES/
│   └── whatsnew
├── fr-FR/
│   └── whatsnew
├── ja-JP/
│   └── whatsnew
├── pt-BR/
│   └── whatsnew
└── zh-CN/
    └── whatsnew
```

## Format Guidelines

**File Requirements:**
- File name: `whatsnew` (no extension)
- Encoding: UTF-8
- Maximum: 500 characters
- Plain text only (no markdown/HTML)

**Content Guidelines:**
- Focus on user-visible changes
- List most important first
- Use bullet points (-, •, or *)
- Be concise and clear
- Avoid technical jargon

## Example Release Notes

**Good:**
```
- New dark mode for easier nighttime reading
- Improved app startup speed by 50%
- Fixed crash when uploading large photos
- Updated design for better accessibility
```

**Avoid:**
```
- Refactored codebase architecture
- Updated dependencies to latest versions
- Various bug fixes and improvements
- Performance optimizations
```

## Supported Locales

Current locales: en-US, de-DE, es-ES, fr-FR, ja-JP, pt-BR, zh-CN

To add a locale:
1. Create directory: `mkdir -p whatsnew/LOCALE-CODE`
2. Create file: `touch whatsnew/LOCALE-CODE/whatsnew`
3. Add translated release notes

## Common Locales

- en-US: English (United States)
- de-DE: German (Germany)
- es-ES: Spanish (Spain)
- fr-FR: French (France)
- it-IT: Italian (Italy)
- ja-JP: Japanese (Japan)
- ko-KR: Korean (Korea)
- pt-BR: Portuguese (Brazil)
- zh-CN: Chinese (Simplified)
- zh-TW: Chinese (Traditional)

## Updating Release Notes

Before each release:
1. Update `whatsnew` files for all locales
2. Keep under 500 characters
3. Verify UTF-8 encoding
4. Test locally: `wc -m whatsnew/en-US/whatsnew`
