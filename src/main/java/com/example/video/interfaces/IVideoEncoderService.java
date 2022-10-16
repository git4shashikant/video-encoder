package com.example.video.interfaces;

import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface IVideoEncoderService {
    void encode(MultipartFile mediaResource, String imageTargetFormat, String videoTargetFormat) throws IOException, EncoderException;
    void segment(MultipartFile mediaResource, String targetFormat) throws IOException, ExecutionException, InterruptedException;
    void transmux(MultipartFile mediaResource, String targetFormat) throws IOException, ExecutionException, InterruptedException;
}
