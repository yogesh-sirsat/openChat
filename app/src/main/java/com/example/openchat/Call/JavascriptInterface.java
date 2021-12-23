package com.example.openchat.Call;

public class JavascriptInterface {

    VideoCallActivity videoCallActivity;

    public JavascriptInterface(VideoCallActivity videoCallActivity) {
        this.videoCallActivity = videoCallActivity;
    }

    @android.webkit.JavascriptInterface
    public void onPeerConnected() {
        videoCallActivity.onPeerConnected();
    }

}
