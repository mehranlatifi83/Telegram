# Telegram for Android — Accessibility

This repository tracks **accessibility work on [Telegram for Android](https://github.com/DrKLO/Telegram)** for people who use a screen reader. Every change listed below is a focused pull request proposed to the official repository.

> The branches here exist so that each change stays small enough to review on its own, and so that all of them can be tested together before being offered upstream. A build of the merged work is [published here](#builds) as well, for people who would rather have the changes now than wait for them to land upstream.

Currently based on Telegram for Android **12.10.1**.

## Branches

| Branch | What it holds |
|---|---|
| `master` | Mirror of the official [DrKLO/Telegram](https://github.com/DrKLO/Telegram) source, plus this README |
| `accessibility` | Every change below, merged together and tested as a whole, and what the published build needs |
| `feature/*`, `fix/*` | One change each — a single commit, and a single pull request |

## Pull requests

All of the following are open against [DrKLO/Telegram](https://github.com/DrKLO/Telegram). Each one carries the steps for reproducing what it fixes with a screen reader.

### Reading messages

| PR | Title | Status |
|---|---|---|
| [#1989](https://github.com/DrKLO/Telegram/pull/1989) | Support TalkBack granular text navigation in chat message cells | Open |
| [#2006](https://github.com/DrKLO/Telegram/pull/2006) | Give the buttons of a message a place and an order | Open |
| [#1998](https://github.com/DrKLO/Telegram/pull/1998) | Stop reading a poll of a previous message on other messages | Open |
| [#1999](https://github.com/DrKLO/Telegram/pull/1999) | Stop reading a translated caption twice on media messages | Open |
| [#2037](https://github.com/DrKLO/Telegram/pull/2037) | Say who a message is from when it is not a person | Open |
| [#2027](https://github.com/DrKLO/Telegram/pull/2027) | Say what the card under a link holds | Open |

### Reactions and emoji

| PR | Title | Status |
|---|---|---|
| [#2043](https://github.com/DrKLO/Telegram/pull/2043) | Let a reaction already on a message be given back | Open |
| [#2038](https://github.com/DrKLO/Telegram/pull/2038) | Say which emoji a custom one stands for | Open |

### Voice, media and files

| PR | Title | Status |
|---|---|---|
| [#1993](https://github.com/DrKLO/Telegram/pull/1993) | Announce playback position of voice messages and music | Open |
| [#1992](https://github.com/DrKLO/Telegram/pull/1992) | Announce download and upload percentage to screen readers | Open |
| [#2005](https://github.com/DrKLO/Telegram/pull/2005) | Make the voice recording controls work with a screen reader | Open |
| [#2001](https://github.com/DrKLO/Telegram/pull/2001) | Fix the record button being reported twice and losing its place | Open |
| [#2000](https://github.com/DrKLO/Telegram/pull/2000) | Free the bottom of a chat after a locked voice recording | Open |
| [#2002](https://github.com/DrKLO/Telegram/pull/2002) | Keep the attach button reachable by touch exploration | Open |
| [#2041](https://github.com/DrKLO/Telegram/pull/2041) | Give a video and a piece of music a download of their own | Open |
| [#2040](https://github.com/DrKLO/Telegram/pull/2040) | Keep what a message offers to do up to date | Open |
| [#2046](https://github.com/DrKLO/Telegram/pull/2046) | Open the viewer of a video that is still downloading | Open |
| [#2020](https://github.com/DrKLO/Telegram/pull/2020) | Stop the pause button of a video from starting it over | Open |

### Moving around

| PR | Title | Status |
|---|---|---|
| [#2007](https://github.com/DrKLO/Telegram/pull/2007) | Keep a screen reader from skipping over rows of a list | Open |
| [#2003](https://github.com/DrKLO/Telegram/pull/2003) | Take screen readers to the message that was jumped to | Open |
| [#2004](https://github.com/DrKLO/Telegram/pull/2004) | Report which search result is shown in a chat | Open |
| [#2044](https://github.com/DrKLO/Telegram/pull/2044) | Open the message a reply was made to | Open |
| [#1995](https://github.com/DrKLO/Telegram/pull/1995) | Fix crash when a list updates while a screen reader reads an item | Open |

### What is drawn rather than laid out

These are places where a control is painted onto a canvas, so there is no view for a screen reader to land on.

| PR | Title | Status |
|---|---|---|
| [#2042](https://github.com/DrKLO/Telegram/pull/2042) | Let a finger find the buttons on a profile | Open |
| [#2039](https://github.com/DrKLO/Telegram/pull/2039) | Reach what the avatar of a chat leads to | Open |
| [#2045](https://github.com/DrKLO/Telegram/pull/2045) | Reach the menu behind the avatar of a sender | Open |
| [#2021](https://github.com/DrKLO/Telegram/pull/2021) | Say what the update bar is and how far its download has got | Open |

### Stories

| PR | Title | Status |
|---|---|---|
| [#1986](https://github.com/DrKLO/Telegram/pull/1986) | Improve TalkBack support in the story viewer | Open |
| [#2025](https://github.com/DrKLO/Telegram/pull/2025) | Give the sticker sheet of a story what it draws | Open |
| [#2026](https://github.com/DrKLO/Telegram/pull/2026) | Name the buttons of the steps that put up a story | Open |

An earlier attempt at labelling the profile music player, [#1895](https://github.com/DrKLO/Telegram/pull/1895), was closed and its subject is now covered by [#1993](https://github.com/DrKLO/Telegram/pull/1993).

## Builds

Everything above is offered upstream and is meant to end up in the official app. Until it does, the merged work is built here and published under [Releases](https://github.com/mehranlatifi83/Telegram/releases), so that the people it is written for do not have to wait.

| Release | What it is | Package |
|---|---|---|
| Marked as **pre-release** | The beta build, where changes arrive first | `ir.codelighthouse.telegram.beta` |
| Not marked | The standalone build | `ir.codelighthouse.telegram` |

Both carry their own name and their own package id, so either sits alongside the official app rather than replacing it. Each build checks this repository for a newer one of its own kind and offers it from inside the app, with the list of what changed.

The version name follows Telegram's own; the version code is Telegram's multiplied by a hundred, counting up in the last two digits for builds made on the same Telegram version.

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
