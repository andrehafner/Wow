World of Warcraft: Forever on non-Adreno GPUs and phones with a built-in keyboard.

**Tested on one device only:** Unihertz Titan 2 (Dimensity 7300, Mali-G615 MC2), about 35 fps at login and about 12 fps in the world on minimum settings. Other Mali devices are untested. Snapdragon devices behave as upstream.

**Install:** download `WoW-Forever-Mali.apk` below and open it. You need your own Battle.net account with WoW Forever access, about 60 GB free and Wi-Fi.

**Settings known to work on the Titan 2:** Textures "CPU, all formats", Driver layer "GameNative", Shader patching "Off". The GPU texture modes corrupt textures on that device.

**What's different from upstream**
- Renders through the phone's own Vulkan driver instead of Turnip
- 540p resolution option, reshaped to fit square screens
- Hardware keyboard support: Alt-layer numbers and symbols, and a `keymap.txt` for remapping keys
- Keyboard-and-mouse interface option
- On-screen Touch and Menu buttons

**Signing:** this APK is signed with the public test key in `tools/test-signing`, so it installs over the test builds. Because that key is public, only install this APK from this repository's releases page.

Fork of [jaredgei/wow-forever-android](https://github.com/jaredgei/wow-forever-android), built on GameNative. Unofficial; not affiliated with Blizzard.
