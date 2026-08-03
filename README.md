# Telegram for Android — Accessibility

This repository tracks **accessibility work on [Telegram for Android](https://github.com/DrKLO/Telegram)** for people who use a screen reader. Every change listed below is a focused pull request proposed to the official repository.

> This is **not** a separate app and not a separate build. The branches here exist so that each change stays small enough to review on its own, and so that all of them can be tested together before being offered upstream.

## Branches

| Branch | What it holds |
|---|---|
| `master` | Mirror of the official [DrKLO/Telegram](https://github.com/DrKLO/Telegram) source, plus this README |
| `accessibility` | Every change below, merged together and tested as a whole |
| `feature/*`, `fix/*` | One change each — a single commit, and a single pull request |

## Pull requests

All of the following are open against [DrKLO/Telegram](https://github.com/DrKLO/Telegram).

### Reading messages

| PR | Title | Status |
|---|---|---|
| [#1989](https://github.com/DrKLO/Telegram/pull/1989) | Support TalkBack granular text navigation in chat message cells | Open |
| [#2006](https://github.com/DrKLO/Telegram/pull/2006) | Give the buttons of a message a place and an order | Open |
| [#1998](https://github.com/DrKLO/Telegram/pull/1998) | Stop reading a poll of a previous message on other messages | Open |
| [#1999](https://github.com/DrKLO/Telegram/pull/1999) | Stop reading a translated caption twice on media messages | Open |
| [#1986](https://github.com/DrKLO/Telegram/pull/1986) | Improve TalkBack support in the story viewer | Open |

### Voice, media and files

| PR | Title | Status |
|---|---|---|
| [#1993](https://github.com/DrKLO/Telegram/pull/1993) | Announce playback position of voice messages and music | Open |
| [#1992](https://github.com/DrKLO/Telegram/pull/1992) | Announce download and upload percentage to screen readers | Open |
| [#2005](https://github.com/DrKLO/Telegram/pull/2005) | Make the voice recording controls work with a screen reader | Open |
| [#2001](https://github.com/DrKLO/Telegram/pull/2001) | Fix the record button being reported twice and losing its place | Open |
| [#2000](https://github.com/DrKLO/Telegram/pull/2000) | Free the bottom of a chat after a locked voice recording | Open |
| [#2002](https://github.com/DrKLO/Telegram/pull/2002) | Keep the attach button reachable by touch exploration | Open |

### Moving around

| PR | Title | Status |
|---|---|---|
| [#2007](https://github.com/DrKLO/Telegram/pull/2007) | Keep a screen reader from skipping over rows of a list | Open |
| [#2003](https://github.com/DrKLO/Telegram/pull/2003) | Take screen readers to the message that was jumped to | Open |
| [#2004](https://github.com/DrKLO/Telegram/pull/2004) | Report which search result is shown in a chat | Open |
| [#1995](https://github.com/DrKLO/Telegram/pull/1995) | Fix crash when a list updates while a screen reader reads an item | Open |

An earlier attempt at labelling the profile music player, [#1895](https://github.com/DrKLO/Telegram/pull/1895), was closed and its subject is now covered by [#1993](https://github.com/DrKLO/Telegram/pull/1993).

## Tested with

- [TalkBack](https://support.google.com/accessibility/android/answer/6283677) on Android
- [Commentary Screen Reader (Jieshuo)](https://play.google.com/store/apps/details?id=com.hciwm.commentary)

## Contributors

- **[@mehranlatifi83](https://github.com/mehranlatifi83)** (Mehran Latifi) — a blind developer; author of the pull requests listed above, testing every change with the screen readers it is written for.
- **[@amirmahdifard](https://github.com/amirmahdifard)** (Amir Mahdi Fard) — a blind developer, contributing to the accessibility work in this repository.

Contributions are welcome. Open a pull request against the `accessibility` branch.

## Building

Build instructions, the API manuals and the requirements for creating your own Telegram application are in the [official repository](https://github.com/DrKLO/Telegram#creating-your-telegram-application). You will need your own `api_id` and `api_hash`.

The native libraries are git submodules, so clone with them:

```bash
git clone --recursive --shallow-submodules https://github.com/mehranlatifi83/Telegram.git
```

For a clone that is already there:

```bash
git submodule update --init --recursive --depth=1
```

## License

The source code is published under GPLv2 with OpenSSL exception, the same as the official [Telegram for Android](https://github.com/DrKLO/Telegram). See [LICENSE](LICENSE) for details.
