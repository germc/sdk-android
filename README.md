PlayHaven Android SDK 1.10.3
====================
PlayHaven is a real-time mobile game marketing platform which helps you take control of the business of your games.

An API token and secret is required to use this SDK. Visit the PlayHaven developer dashboard at https://dashboard.playhaven.com.

Table of Contents
=================

* [Installation](#installation)
    * [JAR Integration](#jar-integration)
* [Usage](#usage)
    * [Recording Game Opens](#recording-game-opens)
    * [Showing Ads](#displaying-full-screen-ads)
    * [Notification Badges](#displaying-a-notification-badge)
    * [Unlocking Rewards](#unlocking-rewards)
* [Callbacks](#callbacks)
    * [PHPublisherOpenRequest delegate](#phpublisheropenrequest-delegates)
    * [PHPublisherContentRequest delegates](#phpublishercontentrequest-delegates)
* [Tips n' Tricks](#tips-and-tricks)
    * [didDismissContentWithin](https://github.com/playhaven/sdk-android-internal/tree/1.10.3-release#diddismisscontentwithintimerange)

Installation
============

Integrating the Playhaven Android SDK is dead simple and should take no more than a minute. 

**Note:** If you are developing your game using Unity, this instructions are irrelevant and you should use the Playhaven Unity SDK located [here](https://github.com/playhaven/sdk-unity).

### JAR Integration

1. Download the Playhaven SDK [here](http://playhaven-sdk-builds.s3.amazonaws.com/android/jars/playhaven-1.10.3.jar) and ensure you have the latest version of the [Android Developer Tools installed](http://developer.android.com/sdk/eclipse-adt.html#updating).

2. Install the SDK into your project.
    1. If a __libs__ folder doesn't already exist, create one in your project root. Android will automatically recognize it.
        
        <img src="http://i1249.photobucket.com/albums/hh509/samatplayhaven/ScreenShot2012-06-13at111136AM.png"  style="padding: 5px; margin-bottom: 20px;" />
        
        <img src="http://i1249.photobucket.com/albums/hh509/samatplayhaven/ScreenShot2012-06-13at105708AM.png"  style="padding: 5px" />
        
    2. Drag the Playhaven SDK JAR file you downloaded into the __libs__ folder.
        
        <img src="http://i1249.photobucket.com/albums/hh509/samatplayhaven/ScreenShot2012-06-13at105747AM3.png" style="padding: 5px" />
        
    4. Add the appropriate import statement to your source files:
        ```java
        import com.playhaven.*;
        ```

3. Set the API keys you received from the [dashboard](http://www.dashboard.playhaven.com). Although you can set these wherever you wish, we advise the root `Activity`.

 ```java
 PHConfig.token = "your token"
 PHConfig.secret = "your secret"
 ```

4. Add the main ad display to your `AndroidManifest.xml` file.

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET"/>

<activity android:name="com.playhaven.src.publishersdk.content.PHContentView" android:theme="@android:style/Theme.Dialog"></activity>
```

----------

Usage
=====

**This guide assumes you have followed the installation instructions above.**

## Recording game opens

**Purpose:** helps the server track game usage.

**Notes:** Make sure to pass in a `Context` (usually an `Activity`) to the `PHPublisherOpenRequest` constructor.

```java
PHPublisherOpenRequest request = new PHPublisherOpenRequest([your context]);

request.send();
```

## Displaying full-screen ads

**Purpose:** Displays a fullscreen ad unit with the placement specified. 

**Notes:** Make sure to provide the `PHPublisherContentRequest` constructor with a valid `Context` (usually an `Activity`). You can specify placements in the Playhaven [dashboard](http://www.dashboard.playhaven.com]).

```java
PHPublisherContentRequest request = new PHPublisherContentRequest([your context], "your placement");

request.send();
```

## Displaying a notification badge

**Purpose:** Displays a small badge with a number indicating the number of new games a user can view.

**Notes:** You may place this anywhere in your app but we've found the best solution is to add it to an button which then launches a `PHPublisherContentRequest` with a "more_games" placement. Once a user clicks on the button you should call `clear()` on the notification view to reset the badge number.

```java
PHNotificationView notifyView = new PHNotificationView([your context], "your placement");

[your button/view/layout].addView(notifyView);

notifyView.refresh();
```

## Unlocking Rewards

**Purpose:** Allows your game to respond when the users unlocks rewards you have configured in the [dashboard](http://www.dashboard.playhaven.com]).

**Notes:** You must set the `RewardDelegate` on a `PHPublisherContentRequest` object to receive this callback. See the [Callbacks](#callbacks) section below for more information.

```java
public void unlockedReward(PHPublisherContentRequest request, PHReward reward) {
    ... your handling code here...
}
```

The PHReward object has the following useful properties:

* __name:__     The reward's name (specified in the dashboard)
* __quantity:__ The reward's amount (specified in the dashboard) 

-------------
## Callbacks


Every type of request in the Playhaven Android SDK has a special "delegate" you can set to receive "callbacks" as the request progresses. You can find more information regarding the delegate pattern [here](http://en.wikipedia.org/wiki/Delegation_pattern).

You must implement the appropriate delegate *interface* from the list below and then add your object as a delegate. Most often the root `Activity` should handle the callbacks.

Once you've implemented the appropriate callbacks you must set the delegate on the individual request before sending:

```java
PHPublisherOpenRequest request = new PHPublisherOpenRequest([your context]);
request.delegate = [your delegate (usually an Activity)]
request.send();
```

### PHPublisherContentRequest delegates

**Note:** There are several delegate *interfaces* for a `PHPublisherContentRequest`. You should implement the ones which provide relevant callbacks.

1. FailureDelegate
2. CustomizeDelegate
3. RewardDelegate
4. ContentDelegate

When working with the multiple delegates in `PHPublisherContentRequest`:

```java
PHPublisherContentRequest request = new PHPublisherContentRequest([your context (usually Activity)], "your placement");
request.content_delegate = [your customize delegate];
request.customize_delegate = [your customize delegate];
request.failure_delegate = [your failure delegate];
request.reward_delegate = [your reward delegate];

request.send();
```

* __failure of request:__ 

```java
public void didFail(PHPublisherContentRequest request, String error) {
    ... your handling code here ...
}
```

* __failure of actual ad:__

```java
public void contentDidFail(PHPublisherContentRequest request, Exception e) {
    ... your handling code here ...
}
```

* __customize the close button:__
```java
public Bitmap closeButton(PHPublisherContentRequest request, PHButtonState state) {
    ... return a custom bitmap for the given state ...
}
```

* __customize the border color:__
```java
public int borderColor(PHPublisherContentRequest request, PHContent content) {}
    ... constant from the Color class ...
}
```

* __unlocked a reward:__
```java
public void unlockedReward(PHPublisherContentRequest request, PHReward reward) {
    ... handle the reward in-game ...
}
```

* __ad content is downloading:__
```java
public void willGetContent(PHPublisherContentRequest request) {
    ... your handling code here ...
}
```

* __ad content is going to display:__
```java
public void willDisplayContent(PHPublisherContentRequest request, PHContent content) {
    ... return a custom bitmap for the given state ...
}
```

* __ad content has been shown:__
```java
public void didDisplayContent(PHPublisherContentRequest request, PHContent content) {
    ... your handling code here ...
}
```

* __ad was dismissed:__
```java
public void didDismissContent(PHPublisherContentRequest request, PHDismissType type) {
    ... your handling code here ...
}
```

### PHPublisherOpenRequest delegates


* __successful request callback:__ 

```java
public void requestSucceeded(PHAPIRequest request, JSONObject responseData) {
    ... your handling code here ...
}
```
* __unsuccessful request callback:__

```java
public void requestFailed(PHAPIRequest request, Exception e) {
    ...your handling code here...
}
```

-------------
## Tips and Tricks


A few helpful tips on using the Playhaven Android SDK.

### didDismissContentWithin(timerange)

This special method within `PHPublisherContentRequest` is helpful when you wish to determine if your game is resuming after showing an ad or from another app entirely. The *timerange* argument should be specified in milliseconds and we generally find that about 2 seconds (2000 milliseconds) works best. An example `onResume` handler using this feature:

```java
@Override
public void onResume() {
	super.onResume();
	if (PHPublisherContentRequest.didDismissContentWithin(2000)) { // can actually be less than 2 seconds, all we want is enough time for onResume to be called
		System.out.println("Resumed after displaying ad unit");
		return; 
	}
	
	System.out.println("Resumed after other app was shown");
}
```
