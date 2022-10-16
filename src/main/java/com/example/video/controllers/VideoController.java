package com.example.video.controllers;

import com.example.video.interfaces.IVideoEncoderService;
import com.example.video.interfaces.IVideoUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.example.video.Constants.DASH_TARGET_FORMAT;
import static com.example.video.Constants.HLS_TARGET_FORMAT;
import static com.example.video.Constants.IMAGE_TARGET_FORMAT;
import static com.example.video.Constants.Mp4_TARGET_FORMAT;

@RestController
public class VideoController {

    private final IVideoUploadService videoService;
    private final IVideoEncoderService videoEncoderService;

    @Autowired
    public VideoController(IVideoUploadService videoService, IVideoEncoderService videoEncoderService) {
        this.videoService = videoService;
        this.videoEncoderService = videoEncoderService;
    }

    @PostMapping(
            value = "/video-encoder/encode",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public @ResponseBody ResponseEntity<HttpStatus> encode(@RequestParam(value = "mediaResource") MultipartFile mediaResource) throws IOException, EncoderException {
        videoService.upload(mediaResource);
        videoEncoderService.encode(mediaResource, IMAGE_TARGET_FORMAT, Mp4_TARGET_FORMAT);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping(
            value = "/segment",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public @ResponseBody ResponseEntity<HttpStatus> segment(@RequestParam(value = "mediaResource") MultipartFile mediaResource) throws IOException, ExecutionException, InterruptedException {
        videoService.upload(mediaResource);
        videoEncoderService.segment(mediaResource, Mp4_TARGET_FORMAT);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping(
            value = "/transmuxHls",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public @ResponseBody ResponseEntity<HttpStatus> transmuxHls(@RequestParam(value = "mediaResource") MultipartFile mediaResource) throws IOException, ExecutionException, InterruptedException {
        videoService.upload(mediaResource);
        videoEncoderService.transmux(mediaResource, HLS_TARGET_FORMAT);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping(
            value = "/transmuxDash",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public @ResponseBody ResponseEntity<HttpStatus> transmuxDash(@RequestParam(value = "mediaResource") MultipartFile mediaResource) throws IOException, ExecutionException, InterruptedException {
        videoService.upload(mediaResource);
        videoEncoderService.transmux(mediaResource, DASH_TARGET_FORMAT);

        return ResponseEntity.ok(HttpStatus.OK);
    }
}
