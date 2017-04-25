# Lurker

Native android app for reddit. Focus on browsing or one could say "lurking". You can find the latest version in Play Store [here](https://play.google.com/store/apps/details?id=torille.fi.lurkforreddit)

## Motivation

I was never fully satisfied with the various reddit apps and I wanted to deep dive into android development, so I decided to try my hands at making an app myself. This app is also my thesis work in Haaga-Helia.

## Installation

To get up and running you need to make a new file to `app/src/main/res/values/values.xml` with 
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="client_id">your_id</string>
</resources>
```
## Planned 
* search posts
* sub/unsub from subreddits
* maybe up/down voting
* sort by hot/new etc

## Working
* [OAuth2 sign in](https://github.com/reddit/reddit/wiki/OAuth2)
* auto refreshing expired OAuth2 token
* tabbed subreddit browsing
* syncing and browsing subreddits (multireddits WIP)
* native gfycat/gifv support
* videos dont interrupt background sounds thanks to surfaceview implementation
* zoomable images
* comments
* links clickable in comments
* search for subreddits

## Build with
* [Android Studio](https://developer.android.com/studio/index.html)
* [Support Library](https://developer.android.com/topic/libraries/support-library/index.html) - For compatibility
* [Fresco](http://frescolib.org/) - Image Management Library
* [PhotoDraweeView](https://github.com/ongakuer/PhotoDraweeView) - Zoomable photoDraweeView
* [Retrofit 2](https://square.github.io/retrofit/) - For network management
* [Exoplayer](https://github.com/google/ExoPlayer) - Used for gifv and mp4 playback
* [Timber](https://github.com/JakeWharton/timber) - Supreme logging
* [AutoValue: Gson Extension](https://github.com/rharter/auto-value-gson) - Great extension for auto value
* [AutoValue: Parcel Extension](https://github.com/rharter/auto-value-parcel) - Parcel boilerplate handling
* [LeakCanary](https://github.com/square/leakcanary) - For finding memory leaks
## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
