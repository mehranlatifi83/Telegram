# Telegram for Android — Accessibility

This repository tracks **accessibility work on [Telegram for Android](https://github.com/DrKLO/Telegram)** for people who use a screen reader. Every change listed below is a focused pull request proposed to the official repository.

> The branches here exist so that each change stays small enough to review on its own, and so that all of them can be tested together before being offered upstream. A build of the merged work is [published here](#builds) as well, for people who would rather have the changes now than wait for them to land upstream.

Currently based on Telegram for Android **13.0.2**.

## Branches

| Branch | What it holds |
|---|---|
| `master` | Mirror of the official [DrKLO/Telegram](https://github.com/DrKLO/Telegram) source, plus this README |
| `accessibility` | Every change below, merged together and tested as a whole, and what the published build needs |
| `feature/*`, `fix/*` | One change each — a single commit, and a single pull request |

## Pull requests

All 127 of the following are open against [DrKLO/Telegram](https://github.com/DrKLO/Telegram). Each one says what was wrong, what changed, and how to check it with a screen reader.

### Reading messages

| PR | Title |
|---|---|
| [#1989](https://github.com/DrKLO/Telegram/pull/1989) | Support TalkBack granular text navigation in chat message cells |
| [#2006](https://github.com/DrKLO/Telegram/pull/2006) | Fix the order of message buttons for screen readers |
| [#2127](https://github.com/DrKLO/Telegram/pull/2127) | Fix screen readers getting stuck on long messages |
| [#1998](https://github.com/DrKLO/Telegram/pull/1998) | Fix screen readers reading a stale poll on other messages |
| [#1999](https://github.com/DrKLO/Telegram/pull/1999) | Fix translated captions being read twice on media messages |
| [#2037](https://github.com/DrKLO/Telegram/pull/2037) | Announce channel and group senders to screen readers |
| [#2072](https://github.com/DrKLO/Telegram/pull/2072) | Announce position within an album to screen readers |
| [#2027](https://github.com/DrKLO/Telegram/pull/2027) | Read link previews with screen readers |
| [#2083](https://github.com/DrKLO/Telegram/pull/2083) | Make the Instant View button reachable by screen readers |
| [#2084](https://github.com/DrKLO/Telegram/pull/2084) | Make "Show more" on long messages accessible to screen readers |
| [#2082](https://github.com/DrKLO/Telegram/pull/2082) | Announce message effects to screen readers |
| [#2157](https://github.com/DrKLO/Telegram/pull/2157) | Announce pinned messages to screen readers |
| [#2170](https://github.com/DrKLO/Telegram/pull/2170) | Announce timers on live location and timed media messages |
| [#2080](https://github.com/DrKLO/Telegram/pull/2080) | Announce dice and game emoji results to screen readers |
| [#2081](https://github.com/DrKLO/Telegram/pull/2081) | Make giveaway messages accessible to screen readers |
| [#2105](https://github.com/DrKLO/Telegram/pull/2105) | Make signed post authors accessible to screen readers |
| [#2108](https://github.com/DrKLO/Telegram/pull/2108) | Announce message state changes to screen readers |
| [#2107](https://github.com/DrKLO/Telegram/pull/2107) | Fix pressing polls, quizzes and checklists with screen readers |
| [#2062](https://github.com/DrKLO/Telegram/pull/2062) | Allow copying every monospace text in a message with screen readers |
| [#2132](https://github.com/DrKLO/Telegram/pull/2132) | Add link menu actions for screen readers |
| [#2063](https://github.com/DrKLO/Telegram/pull/2063) | Announce bot callback answers to screen readers |

### Polls and checklists

| PR | Title |
|---|---|
| [#2068](https://github.com/DrKLO/Telegram/pull/2068) | Announce poll type and state before its question |
| [#2050](https://github.com/DrKLO/Telegram/pull/2050) | Announce poll option state to screen readers |
| [#2051](https://github.com/DrKLO/Telegram/pull/2051) | Make poll media accessible to screen readers |
| [#2061](https://github.com/DrKLO/Telegram/pull/2061) | Read recent poll voters with screen readers |
| [#2069](https://github.com/DrKLO/Telegram/pull/2069) | Read quiz explanations with screen readers |
| [#2070](https://github.com/DrKLO/Telegram/pull/2070) | Make the add-answer button of polls reachable by screen readers |
| [#2079](https://github.com/DrKLO/Telegram/pull/2079) | Fix the poll vote button doing nothing with screen readers |
| [#2059](https://github.com/DrKLO/Telegram/pull/2059) | Announce checklist item state to screen readers |
| [#2060](https://github.com/DrKLO/Telegram/pull/2060) | Announce checklist type and progress to screen readers |
| [#2052](https://github.com/DrKLO/Telegram/pull/2052) | Allow reordering poll options with screen readers |
| [#2053](https://github.com/DrKLO/Telegram/pull/2053) | Fix a hidden correct-answer checkbox read by screen readers |
| [#2054](https://github.com/DrKLO/Telegram/pull/2054) | Add position labels to poll options in the poll editor |
| [#2055](https://github.com/DrKLO/Telegram/pull/2055) | Announce poll validation errors to screen readers |
| [#2056](https://github.com/DrKLO/Telegram/pull/2056) | Announce locked poll settings to screen readers |
| [#2057](https://github.com/DrKLO/Telegram/pull/2057) | Announce new rows when poll settings are turned on |
| [#2058](https://github.com/DrKLO/Telegram/pull/2058) | Allow removing poll options with screen readers |
| [#2071](https://github.com/DrKLO/Telegram/pull/2071) | Add a screen reader label to the poll attach button |
| [#2102](https://github.com/DrKLO/Telegram/pull/2102) | Announce country selection state in poll and giveaway country lists |

### Reactions and emoji

| PR | Title |
|---|---|
| [#2043](https://github.com/DrKLO/Telegram/pull/2043) | Allow toggling existing reactions with screen readers |
| [#2038](https://github.com/DrKLO/Telegram/pull/2038) | Read custom emoji as their emoji with screen readers |
| [#2104](https://github.com/DrKLO/Telegram/pull/2104) | Announce the selected message effect |
| [#2106](https://github.com/DrKLO/Telegram/pull/2106) | Make the star reaction sheet accessible to screen readers |
| [#2085](https://github.com/DrKLO/Telegram/pull/2085) | Add screen reader labels to the emoji, sticker and GIF panel |
| [#2151](https://github.com/DrKLO/Telegram/pull/2151) | Add screen reader labels to emoji suggestions |
| [#2159](https://github.com/DrKLO/Telegram/pull/2159) | Add screen reader labels to emoji in the emoji pack preview |

### Voice, media and files

| PR | Title |
|---|---|
| [#1993](https://github.com/DrKLO/Telegram/pull/1993) | Announce playback position of voice messages and music |
| [#1992](https://github.com/DrKLO/Telegram/pull/1992) | Announce download and upload percentage to screen readers |
| [#2005](https://github.com/DrKLO/Telegram/pull/2005) | Make the voice recording controls work with a screen reader |
| [#2001](https://github.com/DrKLO/Telegram/pull/2001) | Fix the record button being reported twice and losing its place |
| [#2000](https://github.com/DrKLO/Telegram/pull/2000) | Fix chat input freezing after a locked voice recording with screen readers |
| [#2002](https://github.com/DrKLO/Telegram/pull/2002) | Fix the attach button being unreachable by touch exploration |
| [#2041](https://github.com/DrKLO/Telegram/pull/2041) | Add a download action for videos and music |
| [#2040](https://github.com/DrKLO/Telegram/pull/2040) | Fix stale media action labels on messages |
| [#2046](https://github.com/DrKLO/Telegram/pull/2046) | Fix opening streaming videos with screen readers |
| [#2020](https://github.com/DrKLO/Telegram/pull/2020) | Fix the video pause button restarting playback with screen readers |
| [#2167](https://github.com/DrKLO/Telegram/pull/2167) | Allow switching photos in the media viewer with screen readers |
| [#2169](https://github.com/DrKLO/Telegram/pull/2169) | Fix opening and playing view-once media with screen readers |
| [#2145](https://github.com/DrKLO/Telegram/pull/2145) | Announce the view-once timer in the media viewer |
| [#2164](https://github.com/DrKLO/Telegram/pull/2164) | Allow trimming videos before sending with screen readers |
| [#2165](https://github.com/DrKLO/Telegram/pull/2165) | Allow trimming round videos before sending with screen readers |
| [#2124](https://github.com/DrKLO/Telegram/pull/2124) | Make the downloads list accessible to screen readers |
| [#2133](https://github.com/DrKLO/Telegram/pull/2133) | Announce music download state in all lists |

### Rich messages

| PR | Title |
|---|---|
| [#2144](https://github.com/DrKLO/Telegram/pull/2144) | Make buttons, music, files and maps in rich messages accessible to screen readers |
| [#2148](https://github.com/DrKLO/Telegram/pull/2148) | Read photo captions and turn slideshows in rich messages with screen readers |
| [#2168](https://github.com/DrKLO/Telegram/pull/2168) | Make the rich message editor accessible to screen readers |

### Chat list

| PR | Title |
|---|---|
| [#2074](https://github.com/DrKLO/Telegram/pull/2074) | Fix how screen readers read chat list previews |
| [#2075](https://github.com/DrKLO/Telegram/pull/2075) | Announce sent and read state in the chat list |
| [#2076](https://github.com/DrKLO/Telegram/pull/2076) | Announce chat list badges to screen readers |
| [#2077](https://github.com/DrKLO/Telegram/pull/2077) | Fix stale chat list row descriptions |
| [#2110](https://github.com/DrKLO/Telegram/pull/2110) | Announce chat list changes while a row is focused |
| [#2128](https://github.com/DrKLO/Telegram/pull/2128) | Fix screen reader taps opening the wrong item in the chat list |
| [#2156](https://github.com/DrKLO/Telegram/pull/2156) | Add the chat swipe action as a screen reader action |
| [#2158](https://github.com/DrKLO/Telegram/pull/2158) | Allow reordering folders, sticker sets and pinned chats without dragging |
| [#2039](https://github.com/DrKLO/Telegram/pull/2039) | Make chat avatar actions accessible to screen readers |

### Moving around

| PR | Title |
|---|---|
| [#2093](https://github.com/DrKLO/Telegram/pull/2093) | Move screen reader focus to where a chat opens |
| [#2003](https://github.com/DrKLO/Telegram/pull/2003) | Move screen reader focus to the message jumped to |
| [#2004](https://github.com/DrKLO/Telegram/pull/2004) | Announce the current search result in chats |
| [#2044](https://github.com/DrKLO/Telegram/pull/2044) | Fix opening the replied message in topics with screen readers |
| [#2007](https://github.com/DrKLO/Telegram/pull/2007) | Fix screen readers skipping list rows |
| [#2125](https://github.com/DrKLO/Telegram/pull/2125) | Keep screen reader focus on a contact when the list re-sorts |
| [#2126](https://github.com/DrKLO/Telegram/pull/2126) | Fix screen readers leaving the mentions panel |
| [#2086](https://github.com/DrKLO/Telegram/pull/2086) | Allow selecting messages with screen readers |
| [#2131](https://github.com/DrKLO/Telegram/pull/2131) | Make the chat calendar accessible to screen readers |
| [#2109](https://github.com/DrKLO/Telegram/pull/2109) | Announce chat header status changes |
| [#2139](https://github.com/DrKLO/Telegram/pull/2139) | Announce the selected tab and unread count in the bottom bar |
| [#2140](https://github.com/DrKLO/Telegram/pull/2140) | Fix screen reader labels and state of topic tabs |
| [#2150](https://github.com/DrKLO/Telegram/pull/2150) | Announce counts on the scroll-down buttons in chats |
| [#1995](https://github.com/DrKLO/Telegram/pull/1995) | Fix crash when a list updates while a screen reader reads an item |

### What is drawn rather than laid out

These are places where a control is painted onto a canvas, or has no words of its own, so a screen reader has nothing to land on or nothing to say.

| PR | Title |
|---|---|
| [#2042](https://github.com/DrKLO/Telegram/pull/2042) | Make profile buttons reachable by touch exploration |
| [#2045](https://github.com/DrKLO/Telegram/pull/2045) | Make the sender avatar menu accessible to screen readers |
| [#2090](https://github.com/DrKLO/Telegram/pull/2090) | Fix sender options not opening with screen readers |
| [#2021](https://github.com/DrKLO/Telegram/pull/2021) | Add screen reader label and progress to the update bar |
| [#2088](https://github.com/DrKLO/Telegram/pull/2088) | Add screen reader labels to unlabeled buttons |
| [#2089](https://github.com/DrKLO/Telegram/pull/2089) | Add screen reader labels to unlabeled clickable views |
| [#2091](https://github.com/DrKLO/Telegram/pull/2091) | Add a screen reader label to the search button |
| [#2092](https://github.com/DrKLO/Telegram/pull/2092) | Hide the decorative header animation from screen readers |
| [#2064](https://github.com/DrKLO/Telegram/pull/2064) | Fix bulletins without text being silent for screen readers |
| [#2065](https://github.com/DrKLO/Telegram/pull/2065) | Announce tooltips to screen readers |
| [#2066](https://github.com/DrKLO/Telegram/pull/2066) | Remove the screen-reader-only profile entry from the message menu |
| [#2103](https://github.com/DrKLO/Telegram/pull/2103) | Fix the screen reader label of the suggest post button |
| [#2147](https://github.com/DrKLO/Telegram/pull/2147) | Fix screen reader labels of the in-app browser bar |
| [#2152](https://github.com/DrKLO/Telegram/pull/2152) | Read the bot menu button's text with screen readers |
| [#2154](https://github.com/DrKLO/Telegram/pull/2154) | Add screen reader labels to login code digits |
| [#2162](https://github.com/DrKLO/Telegram/pull/2162) | Announce checkbox state and label outlined text fields for screen readers |
| [#2163](https://github.com/DrKLO/Telegram/pull/2163) | Announce the emoji button and contacts sort state to screen readers |
| [#2146](https://github.com/DrKLO/Telegram/pull/2146) | Read the login email in settings with screen readers |
| [#2073](https://github.com/DrKLO/Telegram/pull/2073) | Add screen reader labels to saved messages search tags |
| [#2138](https://github.com/DrKLO/Telegram/pull/2138) | Keep the Undo bar up long enough for screen readers |

### Stories

| PR | Title |
|---|---|
| [#1986](https://github.com/DrKLO/Telegram/pull/1986) | Improve TalkBack support in the story viewer |
| [#2025](https://github.com/DrKLO/Telegram/pull/2025) | Make the story sticker sheet accessible to screen readers |
| [#2026](https://github.com/DrKLO/Telegram/pull/2026) | Add screen reader labels to story editor buttons |
| [#2078](https://github.com/DrKLO/Telegram/pull/2078) | Announce the selected camera in the story camera |
| [#2087](https://github.com/DrKLO/Telegram/pull/2087) | Announce dates in the story gallery |
| [#2155](https://github.com/DrKLO/Telegram/pull/2155) | Announce new comments on live stories to screen readers |
| [#2160](https://github.com/DrKLO/Telegram/pull/2160) | Allow trimming story video and music with screen readers |
| [#2161](https://github.com/DrKLO/Telegram/pull/2161) | Make story stickers and texts accessible to screen readers |
| [#2166](https://github.com/DrKLO/Telegram/pull/2166) | Make the multiple stories selector accessible to screen readers |

### Calls, profiles and other screens

| PR | Title |
|---|---|
| [#2149](https://github.com/DrKLO/Telegram/pull/2149) | Announce new comments in video chats to screen readers |
| [#2141](https://github.com/DrKLO/Telegram/pull/2141) | Announce removed, added and changed words in the AI editor |
| [#2142](https://github.com/DrKLO/Telegram/pull/2142) | Fix business hours being read twice by screen readers |
| [#2143](https://github.com/DrKLO/Telegram/pull/2143) | Make similar channel suggestions accessible to screen readers |
| [#2153](https://github.com/DrKLO/Telegram/pull/2153) | Announce the time left on live locations in the location screen |
| [#2129](https://github.com/DrKLO/Telegram/pull/2129) | Allow screen readers in secret chats with the other side's notice |
| [#2130](https://github.com/DrKLO/Telegram/pull/2130) | Make channel and post statistics accessible to screen readers |

### Beyond accessibility

| PR | Title |
|---|---|
| [#2135](https://github.com/DrKLO/Telegram/pull/2135) | Fix connection stuck on spare addresses after the temp key is reset |
| [#2134](https://github.com/DrKLO/Telegram/pull/2134) | Share device audio during screen sharing |

An earlier attempt at labelling the profile music player, [#1895](https://github.com/DrKLO/Telegram/pull/1895), was closed and its subject is now covered by [#1993](https://github.com/DrKLO/Telegram/pull/1993).

## Builds

Everything above is offered upstream and is meant to end up in the official app. Until it does, the merged work is built here and published under [Releases](https://github.com/mehranlatifi83/Telegram/releases), so that the people it is written for do not have to wait.

| Release | What it is | Package |
|---|---|---|
| Marked as **pre-release** | The beta build, where changes arrive first | `ir.codelighthouse.telegram.beta` |
| Not marked | The standalone build | `ir.codelighthouse.telegram` |

Both carry their own name and their own package id, so either sits alongside the official app rather than replacing it. Each build checks this repository for a newer one of its own kind and offers it from inside the app, with the list of what changed. The strings these changes add are also translated into Persian in the builds, since Telegram's own translations do not have them yet.

The version name follows Telegram's own; the version code is Telegram's multiplied by a hundred, counting up in the last two digits for builds made on the same Telegram version.

## Tested with

- [TalkBack](https://support.google.com/accessibility/android/answer/6283677) on Android
- [Commentary Screen Reader (Jieshuo)](https://play.google.com/store/apps/details?id=com.hciwm.commentary)

## Contributors

- **[@mehranlatifi83](https://github.com/mehranlatifi83)** (Mehran Latifi) — a blind developer; author of the pull requests listed above, testing every change with the screen readers it is written for.

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
