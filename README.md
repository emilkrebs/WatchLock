<div id="logo" align="center">
  <a href="https://github.com/emilkrebs/WatchLock" target="_blank" rel="noopener noreferrer">
   <img width="512" alt="WatchLock Logo" src="./images/WatchLock_Logo.svg">
 </a>
  <h3>
    Don't let people abuse your phone
  </h3>
</div>

<div id="badges" align="center">

[![Build](https://github.com/emilkrebs/generator-discord/actions/workflows/build.yml/badge.svg)](https://github.com/emilkrebs/generator-discord/actions/workflows/build.yml)
[![Tests](https://github.com/emilkrebs/generator-discord/actions/workflows/test.yml/badge.svg)](https://github.com/emilkrebs/generator-discord/actions/workflows/test.yml)
[![downloads](https://img.shields.io/npm/dw/generator-discord?color=orange)](https://www.npmjs.com/package/generator-discord)
[![version](https://img.shields.io/npm/v/generator-discord)](https://www.npmjs.com/package/generator-discord)

</div>

<hr>

# How to Install WatchLock on WearOS

## Watch Setup
1. Enable developer mode on your watch by tapping the software version in the settings app 5 times.

![Enable Developer Options](./images/enable_developer_options.png)

2. Enable ADB debugging in the developer settings.

![Enable ADB Debugging](./images/enable_adb_debugging.png)

3. Enable wireless debugging in the developer settings.

![Enable Wireless Debugging](./images/enable_wireless_debugging.png)

4. Pair a new device

![Pair a new device](./images/pair_new_device.png)


## Computer Setup

1. Install ADB on your computer. You can find instructions [here](https://developer.android.com/tools/adb).
2. Connect your computer to the same network as your watch.
3. Run `adb pair <pairng address>` in a terminal.

![Pairing Terminal](./images/pairing_terminal.png)

## Install WatchLock 

1. Download the latest version of WatchLock from the [releases page](link).
2. Run `adb install <path to apk>` in a terminal.

![Install Terminal](./images/installing_terminal.png)
