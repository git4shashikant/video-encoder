package com.hm.demovideo.models;

import ws.schild.jave.encode.enums.X264_PROFILE;

public enum EncodingAttributeType {
    VID_640X480X160000("aac",
            64000,
            2,
            44100,
            "h264",
            160000,
            15,
            640,
            480,
            X264_PROFILE.BASELINE),
    VID_1280X720X160000("aac",
            64000,
            2,
            44100,
            "h264",
            160000,
            25,
            1280,
            720,
            X264_PROFILE.BASELINE),
    VID_1920X160000("aac",
            64000,
            2,
            44100,
            "h264",
            160000,
            30,
            1920,
            1080,
            X264_PROFILE.BASELINE);

    private final String audioCodec;
    private final Integer audioBitRate, audioChannels, audioSamplingRate;
    private final String videoCodec;
    private final Integer videoBitRate, videoFrameRate, videoWidth, videoHeight;
    private final X264_PROFILE videoProfile;

    EncodingAttributeType(String audioCodec, Integer audioBitRate, Integer audioChannels, Integer audioSamplingRate,
                          String videoCodec, Integer videoBitRate, Integer videoFrameRate, Integer videoWidth, Integer videoHeight, X264_PROFILE videoProfile) {
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
