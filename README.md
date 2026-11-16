# WoW Forever for Android: Mali and hardware-keyboard fork

A fork of [jaredgei/wow-forever-android](https://github.com/jaredgei/wow-forever-android) that adds experimental
support for non-Adreno (Mali) GPUs and for phones with a built-in keyboard.

This page only covers what the fork changes. For everything else (how the launcher works, installing the game,
updating, add-ons, troubleshooting, building from source and credits) see the
[upstream README](https://github.com/jaredgei/wow-forever-android#readme).

> Unofficial community project. Not affiliated with or endorsed by Blizzard Entertainment. You need your own
> Battle.net account with WoW Forever access. This repository contains no game files.

## Status

Developed and tested on one device: a **Unihertz Titan 2** (Dimensity 7300, Mali-G615 MC2). There the game reaches
the world at about 35 fps on the login screen and about 12 fps in the world on minimum settings. Other Mali devices
are untested. Snapdragon devices behave as upstream.

## Download

Get `WoW-Forever-Mali.apk` from the [releases page](https://github.com/andrehafner/Wow/releases).

Releases are signed with a private key. Only install APKs from this repository's releases page. An APK from this
fork can't update an upstream install or be updated by one.

## Non-Adreno GPUs

Upstream renders through Turnip, a Vulkan driver that only exists for Adreno GPUs. On any other GPU this fork:

- **Uses the phone's own Vulkan driver** through GameNative's Vulkan wrapper, with Vulkan 1.3 exposed for DXVK 2.4.1
  and BCn (DXT) textures transcoded on the CPU, since Mali can't sample them.
- **Turns off the wrapper's OpConstantComposite shader pass**, which crashed the game's first shader on a Mali-G615.
- **Adds a 540p resolution** and makes it the default there.
- **Adds setup-screen options** for the texture transcoder, wrapper build, shader pass and diagnostics, so a new
  device can be tried without rebuilding.

Settings known to work on the Titan 2:

| Option | Value |
|---|---|
| Textures | CPU, all formats |
| Driver layer | GameNative |
| Shader patching | Off |
| Diagnostics | Off |

The GPU texture modes corrupt textures on that device. Diagnostics, when on, writes DXVK and Wine logs to a
`DxvkLogs` folder in the game folder and shows DXVK's overlay; both cost frame rate.

## Hardware keyboards

- **Alt-layer keys work in game.** A keyboard with no number row types digits and symbols as Alt+letter; those reach
  the game as the real number or symbol key.
- **`keymap.txt`** in the game folder remaps any hardware key, including `ALT+` and `SHIFT+` layers. Sym is Tab and
  Fn is Esc by default. The file is created on first launch and lists the key names it accepts. Keys nothing is
  mapped to are named in `keymap-unmapped.txt`.
- **Interface option:** WoW's keyboard-and-mouse interface instead of the gamepad one. It is the default on a phone
  with a QWERTY keyboard.
- **On-screen Touch and Menu buttons:** Touch switches between the gamepad overlay and direct touch (tap to left
  click, two-finger tap to right click); Menu presses Esc.

## Builds

- **Build test APK** runs on every push to a branch other than `main` and attaches the APK to the run. Test builds
  are signed with the key in `tools/test-signing`, which is public on purpose so they install over each other. They
  can't install over a release or the other way round, and shouldn't be installed from anywhere but this repository.
- **Publish Mali release** publishes a release when `.github/RELEASE_TAG` changes on `main`; that file holds the tag
  to create. It signs with the release key held in the repository's Actions secrets.

## License

GPL-3.0, the same as upstream and GameNative. See [`LICENSE`](LICENSE). The bundled runtime components carry their
own licenses; see `THIRD_PARTY_NOTICES`.
