<div id="logo" align="center">
  <a href="https://github.com/emilkrebs/WatchLock" target="_blank" rel="noopener noreferrer">
   <img width="256" alt="WatchLock Logo" src="./images/WatchLock.png">
 </a>
  <h3>
    Don't let people abuse your phone
  </h3>
</div>

<div id="badges" align="center">

[![Build](https://github.com/emilkrebs/WatchLock/actions/workflows/android.yml/badge.svg)](https://github.com/emilkrebs/WatchLock/actions/workflows/android.yml)
![GitHub Latest Release)](https://img.shields.io/github/v/release/emilkrebs/WatchLock?logo=github)

</div>

<hr>

## Features

- Lock your phone from your Watch
- View the if the phone is locked from the watch
- Dashboard in the mobile application to check if everything is working

Languages: English, German

# How to Install WatchLock on mobile
1. Download the latest version of WatchLock on your phone from the [releases page](https://github.com/emilkrebs/WatchLock/releases/latest).
3. Click the apk-file and tap install.
   
You may get a security warning when installing the app. This is because the app is not signed by Google. You can safely ignore this warning.

 <img width="256" alt="Security Warning" src="./images/security_warning.jpg">

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

1. Download the latest version of WatchLock from the [releases page](https://github.com/emilkrebs/WatchLock/releases/latest).
2. Run `adb install <path to apk>` in a terminal.

![Install Terminal](./images/installing_terminal.png)

# Screenshots

Mobile

<img width="512" alt="WearOS" src="./images/watchlock_mobile.jpg">

WearOS

![Wear Screenshot](./images/watchlock_wear.png)

