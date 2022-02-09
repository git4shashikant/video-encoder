package com.hm.demovideo.controllers;

import com.hm.demovideo.EncoderManager;
import com.hm.demovideo.interfaces.IVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class VideoController {

    private final IVideoService videoService;
    private final EncoderManager encoderManager;

    private static final String IMAGE_TARGET_FORMAT = "png";
    private static final String HLS_TARGET_FORMAT = "m3u8";
    private static final String DASH_TARGET_FORMAT = "mpd";
    private static final String Mp4_TARGET_FORMAT = "mp4";

    @Autowired
    public VideoController(IVideoService videoService, EncoderManager encoderManager) {
        this.videoService = videoService;
        this.encoderManager = encoderManager;
    }

    @PostMapping(
            value = "/encode",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public @ResponseBody ResponseEntity encode(@RequestParam(value = "mediaResource") MultipartFile mediaResource) throws IOException, EncoderException {
        videoService.upload(mediaResource);
        encoderManager.encode(mediaResource, IMAGE_TARGET_FORMAT, Mp4_TARGET_FORMAT);

        return ResponseEntity.ok(null);
    }

    @PostMapping(
            value = "/segment",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public @ResponseBody ResponseEntity segment(@RequestParam(value = "mediaResource") MultipartFile mediaResource) throws IOException, ExecutionException, InterruptedException {
        videoService.upload(mediaResource);
        encoderManager.segment(mediaResource, Mp4_TARGET_FORMAT);

        return ResponseEntity.ok(null);
    }

    @PostMapping(
            value = "/transmuxHls",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public @ResponseBody ResponseEntity transmuxHls(@RequestParam(value = "mediaResource") MultipartFile mediaResource) throws IOException, ExecutionException, InterruptedException {
        videoService.upload(mediaResource);
        encoderManager.transmux(mediaResource, HLS_TARGET_FORMAT);

        return ResponseEntity.ok(null);
    }

    @PostMapping(
            value = "/transmuxDash",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public @ResponseBody ResponseEntity transmuxDash(@RequestParam(value = "mediaResource") MultipartFile mediaResource) throws IOException, ExecutionException, InterruptedException {
        videoService.upload(mediaResource);
        encoderManager.transmux(mediaResource, DASH_TARGET_FORMAT);

        return ResponseEntity.ok(null);
    }
}
