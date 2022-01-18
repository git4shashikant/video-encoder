package com.hm.demovideo.models;

import ws.schild.jave.encode.enums.X264_PROFILE;

public enum EncodingAttributeType {
    VID_360X240X160000("aac",
            64000,
            2,
            44100,
            "h264",
            400000,
            30,
            360,
            240,
            X264_PROFILE.BASELINE),
    VID_640X360X160000("aac",
            128000,
            2,
            44100,
            "h264",
            1100000,
            30,
            640,
            360,
            X264_PROFILE.HIGH),
    VID_960X640X160000("aac",
            160000,
            2,
            44100,
            "h264",
            3500000,
            30,
            960,
            640,
            X264_PROFILE.HIGH);


    private final String audioCodec;
    private final Integer audioBitRate, audioChannels, audioSamplingRate;
    private final String videoCodec;
    private final Integer videoBitRate, videoFrameRate, videoWidth, videoHeight;
    private final X264_PROFILE videoProfile;

    EncodingAttributeType(String audioCodec, Integer audioBitRate, Integer audioChannels, Integer audioSamplingRate,
                          String videoCodec, Integer videoBitRate, Integer videoFrameRate, Integer videoHeight, Integer videoWidth, X264_PROFILE videoProfile) {
        this.audioCodec = audioCodec;
        this.audioBitRate = audioBitRate;
        this.audioChannels = audioChannels;
        this.audioSamplingRate = audioSamplingRate;
        this.videoCodec = videoCodec;
        this.videoBitRate = videoBitRate;
        this.videoFrameRate = videoFrameRate;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.videoProfile = videoProfile;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public Integer getAudioBitRate() {
        return audioBitRate;
    }

    public Integer getAudioChannels() {
        return audioChannels;
    }

    public Integer getAudioSamplingRate() {
        return audioSamplingRate;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public Integer getVideoBitRate() {
        return videoBitRate;
    }

    public Integer getVideoFrameRate() {
        return videoFrameRate;
    }

    public Integer getVideoWidth() {
        return videoWidth;
    }

    public Integer getVideoHeight() {
        return videoHeight;
    }

    public X264_PROFILE getVideoProfile() {
        return videoProfile;
    }
}
