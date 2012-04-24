PlayHaven Android SDK 1.10.1
====================
PlayHaven is a real-time mobile game marketing platform to help you take control of the business of your games.

Acquire, retain, re-engage, and monetize your players with the help of PlayHaven's powerful marketing platform. Integrate once and embrace the flexibility of the web as you build, schedule, deploy, and analyze your in-game promotions and monetization in real-time through PlayHaven's easy-to-use, web-based dashboard. 

An API token and secret is required to use this SDK. These tokens uniquely identify your app to PlayHaven and prevent others from making requests to the API on your behalf. To get a token and secret, please visit the PlayHaven developer dashboard at https://dashboard.playhaven.com

What's new in 1.10.1
===================
* Fixed Full screen content not showing up.
* Fixed "market://" exception causing content templates not to close on redirect.
* Crash on launch of Sample App.

1.10.0
===================
* Support for Rewards. See "Unlocking rewards with the SDK" in the API Reference section for information on how to integrate this into your app.
* Bug fixes to support new content types
* Prefetch and caching of content templates when an open request is made. Turned caching on for Android WebViews use local file defined in PHConstants.java. Caching and prefetching can also be turned off on PHConstants.java
* Better delegate handling code
* Device language reported every API request
* Fixed HTML SELECT crash for supporting data gathering content templates
* Updated sample application to allow publishers to test different dashboard accounts and URLs. Setting preferences
* Speed up of HTTP requests. Set protocol to HTTP 1.1 and call getHostName() on first API request for both api2.playhaven.com and media.playhaven.com to speed up DNS resolution.
* Various other bug fixes and code cleanup

1.8.0
===================
* Content templates now use Activities for there views

1.3.10
===========================
* Initial beta release of Play Haven Android SDK
* permissions and cleanup call

Usage
=====

There are three options for using the PlayHaven SDK in your Android project. The first is to include the "sdk-android.jar" binary provided in the GIT repository in the "release" folder. The second option is to build the JAR file yourself and include it in your project. The last option is to include the "sdk-android" project into yours. In this section we will talk about option 1 and 2:

#### Option 1:

1) Select *release/sdk-android.jar* and click __raw__ to download

<img src="http://i990.photobucket.com/albums/af25/flashpro/playhaven%20android%20sdk/bin.png" style="padding: 5px" />

<img src="http://i990.photobucket.com/albums/af25/flashpro/playhaven%20android%20sdk/jar_file.png" style="padding: 5px" />

<img src="http://i990.photobucket.com/albums/af25/flashpro/playhaven%20android%20sdk/raw.png" style="padding: 5px" />

2) Copy the .jar file to your project folder (creating a */libs* folder is advised)

<img src="http://i990.photobucket.com/albums/af25/flashpro/playhaven%20android%20sdk/lib_folder.png" style="padding: 5px" />

3) Right-click your Project in Eclipse and select __Properties__

<img src="http://i990.photobucket.com/albums/af25/flashpro/playhaven%20android%20sdk/properties.png" style="padding: 5px" />

4) Select _Java Build Path_ then the __Libraries__ tab

<img src="http://i990.photobucket.com/albums/af25/flashpro/playhaven%20android%20sdk/libraries.png" style="padding: 5px; width: 500px" />

5) Click __Add JARs__ and browse for your local copy of the PlayHaven .jar file

<img src="http://i990.photobucket.com/albums/af25/flashpro/playhaven%20android%20sdk/add_jar.png" style="padding: 5px" />

6) You're done!

#### Option 2:

1) Make a clone of the PlayHaven sdk-android repository (https://tomdiz@github.com/playhaven/sdk-android.git).

2) Create a new workspace in Eclipse or import the PlayHaven sdk-android project into a workspace you alreay have created.

3) The default property for the sdk-android project is to create a "sdk-android.jar" file in the bin folder. You can then add that path to your project JAR search directories or follow instructions in option 1 above to put into a "libs" folder.


Note: If you are using Unity for your game, please integrate the Android Unity SDK located here http://www.playhaven.com/sdk

Using the Android SDK
--------------------- 

Set API Keys: (perhaps in your *onCreate* method?):
	
	PHConstants.setKeys("token", "secret");

Provide SDK with device information:
	
	PHConstants.findDeviceInfo([activity]);

This method allows the SDK to safely gather device information such as orientation, screen size, etc.

## AndroidManifest.xml Changes
You need to add the following to your Android Manifest for Play Haven SDK to function properly

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET"/>
<activity android:name="com.playhaven.src.publishersdk.content.PHContentView" android:theme="@android:style/Theme.Dialog"></activity>
```


Example App
===========
Included with the SDK is an example implementation in the 'com.playhaven.sampleapp' package of the source root (it is not included in the .jar file). 

It features open and content request implementations including relevant delegate methods. You will need a PlayHaven API token and secret to make requests with the Example app.

### Running the Example App

Running the example app is straightforward; simply download the entire repository, then import into Eclipse. You can import the Playhaven project into Eclipse via __File > Import__ command.

Once the project is open, simply create a new Android run configuration via __Run > Run Configurations...__ to run the sample app.

*NOTE:* The default setting is for the project to create the "sdk-android.jar" file. To build and run the test application goto Project->Properties and select the "Android" option. Make sure the "Is Library" checkbox is unchecked.


API Reference
-------------
### Recording game opens
Asynchronously reports a game open to PlayHaven. A delegate is not needed for this request, but if you would like to receive a callback when this request succeeds or fails refer to the implementation found in *com.playhaven.sampleapp/PublisherOpenView.java*.

```java
PHConstants.findDeviceInfo(this);
PHConstants.setKeys(token, secret);
PHPublisherOpenRequest request = new PHPublisherOpenRequest(this);
request.send();
```
In this example code the PHPublisherOpenRequest takes a parameter of the following type PHAPIRequestDelegate. This is usually an Andoird Activity or some other type of class that will be recieving and processing the PlayHaven SDK callbacks using the PHAPIRequestDelegate interface. There are various constructors you can use when making an Open Request. Check the sdk-android PHPublisherOpenRequest.java for others.

#### Precaching content templates
PlayHaven will automatically download and store a number of content templates after a successful PHPublisherOpenRequest. This happens automatically in the background after each open request, so there's no integration required to take advantage of this feature.

### Requesting content for your placements
You may request content for your app using your API token, secret, as well as a placement tag to identify the placement you are requesting content for. Implement PHPublisherContentRequestDelegate methods to receive callbacks from this request. Refer to the section below as well as *com.playhaven.sampleapp/PublisherContentView.java* for a sample implementation.

```java
PHConstants.findDeviceInfo(this);
PHConstants.setKeys(token, secret);
PHPublisherContentRequest request = new PHPublisherContentRequest(this, this);	// The 'this' is an Activity that also supports the PHPublisherContentRequestDelegate interface
request.delegate = <your class>; // This is a class you may define to implement the PHPublisherContentRequestDelegate so that you are notified of the content state, see methods below
request.placement = "placement_ID";
request.setOverlayImmediately(true); //optional, see below.
request.send();
```

In this example code the PHPublisherContentRequest takes 2 parameters of the following type PHPublisherContentRequestDelegate and a Activity. The delegate is usually an Andoird Activity or some other type of class that will be recieving and processing the PlayHaven SDK callbacks. There are various constructors you can use when making an Content Request. Check the sdk-android PHPublisherContentRequest.java for others.


*NOTE:* You may set placement_ids through the PlayHaven Developer Dashboard.

Optionally, you may choose to show the loading overlay immediately by setting the request object's *showsOverlayImmediately* property to YES. This is useful if you would like keep users from interacting with your UI while the content is loading.

#### Preloading requests (optional)
To make content requests more responsive, you may choose to preload a content unit for a given placement. This will start a request for a content unit without displaying it, preserving the content unit until you call -(void)send on a  content request for the same placement in your app.

```java
PHConstants.findDeviceInfo(this);
PHConstants.setKeys(token, secret);
PHPublisherContentRequest  request = new PHPublisherContentRequest(Activity, Activity);
request.placement = "placement_ID";
request.delegate = Activity; // This is a class you may define to implement the PHPublisherContentRequestDelegate so that you are notified of the content state, see methods below
request.preload();
```

You may implement a delegate for your preload if you would like to be informed when a content request is ready to display. See the sections below for more details.

To show the preloaded request just call the send() method.

```java
request.send();
```

*NOTE:* Preloading only affects the next content request for a given placement. If you are showing the same placement multiple times in your app, you will need to make additional preload requests after displaying that placement's content unit for the first time.

#### Starting a content request
The request is about to attempt to get content from the PlayHaven API. 

```java
public void willGetContent(PHPublisherContentRequest request);
```

#### Preparing to show a content view
If there is content for this placement, it will be loaded at this point. An overlay view will appear over your app and a spinner will indicate that the content is loading. Depending on the transition type for your content your view may or may not be visible at this time. If you haven't before, you should mute any sounds and pause any animations in your app. 

```java
public void willDisplayContent(PHPublisherContentRequest request, PHContent content);
```

#### Content view finished loading
The content has been successfully loaded and the user is now interacting with the downloaded content view. 

```java
public void didDisplayContent(PHPublisherContentRequest request, PHContent content);
```

#### Content view dismissing
The content has successfully dismissed and control is being returned to your app. This can happen as a result of the user clicking on the close button or clicking on a link that will open outside of the app. You may restore sounds and animations at this point.

```java
public void didDismissContent(PHPublisherContentRequest request, PHDismissType type);
```

Type may be one of the following constants:

1. ContentUnitTriggered: a user or a content unit dismissed the content request
2. CloseButtonTriggered: the user used the native close button to dismiss the view
3. ApplicationTriggered: iOS 4.0+ only, the content unit was dismissed because the app was sent to the background
4. NoContentTriggered: the content unit was dismissed because there was no content assigned to this placement id

#### Content request failing
If for any reason the content request does not successfully return some content to display or fails to load after the overlay view has appears, the request will stop any any visible overlays will be removed. You receive these calls when your class implements the PHFailureDelegate.

```java
public void didFail(PHPublisherContentRequest request, String error);
public void contentDidFail(PHPublisherContentRequest request, Exception e);
```

### Canceling requests
You may now cancel any API request at any time using the cancel() method. This will also cancel any open network connections and clean up any views in the case of content requests. Canceled requests will not send any more messages to their delegates.

Additionally you may cancel all open API requests for a given delegate. This can be useful if you are not keeping references to API request instances you may have created. As with the cancel() method, canceled requests will not send any more messages to delegates. To cancel all requests:

```java
public static void cancelRequests(PHAPIRequestDelegate delegate);
```

### Customizing content display
#### Replace close button graphics
Implement the PHCustomizeDelegate methods to replace the close button image with something that more closely matches your app. Images will be scaled.

```java
public Bitmap closeButton(PHPublisherContentRequest request, PHButtonState state);
public int borderColor(PHPublisherContentRequest request, PHContent content);
```

### Unlocking rewards with the SDK
PlayHaven allows you to reward users with virtual currency, in-game items, or any other content within your game. If you have configured unlock-able rewards for your content units, you will receive unlock events through implementing a PHRewardDelegate method. It is important to handle these unlock events in every placement that has rewards configured.

```java
public void unlockedReward(PHPublisherContentRequest request, PHReward reward);
```

The PHReward object passed through this method has the following helpful properties:

  * __name__: the name of your reward as configured on the dashboard
  * __quantity__: if there is a quantity associated with the reward, it will be an integer value here
  * __receipt__: a unique identifier that is used to detect duplicate reward unlocks, your app should ensure that each receipt is only unlocked once

### Notifications with PHNotificationView
PHNotificationView provides a fully encapsulated notification view that automatically fetches an appropriate notification from the API and renders it into your view hierarchy. It extends View and you may place in your UI where it should appear and supply it with your app token, secret, and a placement id.

```java
PHConstants.findDeviceInfo(this);
PHConstants.setKeys(token, secret);
PHNotificationView notifyView = new PHNotificationView(Activity, "placement_ID");
notifyView.setBackgroundColor(0xFF020AFF);
notifyView.refresh();
```

*NOTE:* You may set up placement_ids through the PlayHaven Developer Dashboard.

Notification view will remain anchored to the center of the position they are placed in the view, even as the size of the badge changes. You may refresh your notification view from the network using the -(void)refresh method on an instance. We recommend refreshing the notification view each time it will appear in your UI. See *com.playhaven.sampleapp/PublisherContentView.java* for an example.

You will also need to clear any notification view instances when you successfully launch a content unit. You may do this using the clear() method on any notification views you wish to clear.

#### Testing PHNotificationView
Most of the time the API will return an empty response, which means a notification view will not be shown. You can see a sample notification by using test(); wherever you would use refresh(). It has been marked as deprecated to remind you to switch all instances of test() in your code to refresh();

#### Customizing notification rendering with PHNotificationRenderer
PHNotificationRenderer is a base class that draws a notification view for a given notification data. The base class implements a blank notification view used for unknown notification types. PHNotificationBadgeRenderer renders a Play Haven default-style notification badge with a given "value" string. You may customize existing notification renderers and register new ones at runtime using the following method on PHNotificationView

```java
public static void setRenderer(Class renderer, String type);
```

Your PHNotificationRenderer subclass needs to implement the following methods to draw and size your notification view appropriately:

```java
public abstract void draw(Canvas canvas, JSONObject notificationData);
```

This method will be called inside of the PHNotificationView instance onMeasure() method whenever the view needs to be drawn. You will use specific keys inside of notificationData to draw your badge in the view.

```java
public abstract Rect size(JSONObject data);
```

This method will be called to calculate an appropriate frame for the notification badge each time the notification data changes. Using specific keys inside of notificationData, you will need to calculate an appropriate size.
