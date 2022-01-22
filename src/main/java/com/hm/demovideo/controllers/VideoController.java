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
        encoderManager.encode(mediaResource);

        return ResponseEntity.ok(null);
    }

    @PostMapping(
            value = "/transmux",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public @ResponseBody ResponseEntity transmux(@RequestParam(value = "mediaResource") MultipartFile mediaResource) throws IOException, ExecutionException, InterruptedException {
        videoService.upload(mediaResource);
        encoderManager.transmux(mediaResource);

        return ResponseEntity.ok(null);
    }
}
