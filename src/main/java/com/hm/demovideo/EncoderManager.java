package com.hm.demovideo;

import com.hm.demovideo.models.EncodingAttributeType;
import com.hm.demovideo.tasks.DASHTransmuxingTask;
import com.hm.demovideo.tasks.HLSTransmuxingTask;
import com.hm.demovideo.tasks.SegmentationTask;
import com.hm.demovideo.tasks.ThumbnailExtractorTask;
import com.hm.demovideo.tasks.VideoEncoderTask;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class EncoderManager {

    private static final String TEMP_DIR = "temp";

    private final HLSTransmuxingTask hlsTransmuxingTask;
    private final DASHTransmuxingTask dashTransmuxingTask;
    private final VideoEncoderTask videoEncoderTask;
    private final ThumbnailExtractorTask thumbnailExtractorTask;
    private final SegmentationTask segmentationTask;

    @Autowired
    public EncoderManager(HLSTransmuxingTask hlsTransmuxingTask,
                          DASHTransmuxingTask dashTransmuxingTask,
                          VideoEncoderTask videoEncoderTask,
                          ThumbnailExtractorTask thumbnailExtractorTask, SegmentationTask segmentationTask) {
        this.hlsTransmuxingTask = hlsTransmuxingTask;
        this.dashTransmuxingTask = dashTransmuxingTask;
        this.videoEncoderTask = videoEncoderTask;
        this.thumbnailExtractorTask = thumbnailExtractorTask;
        this.segmentationTask = segmentationTask;
    }

    @Async("asyncExecutor")
    public void encode(MultipartFile mediaResource, String imageTargetFormat, String videoTargetFormat) throws IOException, EncoderException {
        final String fileName = mediaResource.getOriginalFilename().substring(0, mediaResource.getOriginalFilename().lastIndexOf("."));
        final File source = storeTempFile(mediaResource);

        final List<Future<String>> imageResults = new ArrayList<>(EncodingAttributeType.values().length);
        final List<Future<String>> videoResults = new ArrayList<>(EncodingAttributeType.values().length);
        for (EncodingAttributeType attributeType : EncodingAttributeType.values()) {
            File targetImage = new File(String.format("%s//%s_%d_%d_%d.%s", TEMP_DIR,
                    fileName,
                    attributeType.getVideoWidth(),
                    attributeType.getVideoHeight(),
                    attributeType.getVideoBitRate(),
                    imageTargetFormat));
            imageResults.add(thumbnailExtractorTask.render(source, attributeType, 10, targetImage, 5));

            File targetVideo = new File(String.format("%s//%s_%d_%d_%d.%s", TEMP_DIR,
                    fileName,
                    attributeType.getVideoWidth(),
                    attributeType.getVideoHeight(),
                    attributeType.getVideoBitRate(),
                    videoTargetFormat));
            videoResults.add(videoEncoderTask.encode(source, attributeType, targetVideo, videoTargetFormat));
        }

        imageResults.forEach(result -> {
            try {
                result.get();
            } catch (InterruptedException | ExecutionException e) {
                //handle thread error
            }
        });

        videoResults.forEach(result -> {
            try {
                result.get();
            } catch (InterruptedException | ExecutionException e) {
                //handle thread error
            }
        });

        FileUtils.forceDelete(source);
    }

    @Async("asyncExecutor")
    public void segment(MultipartFile mediaResource, String targetFormat) throws IOException, ExecutionException, InterruptedException {
        final String fileName = mediaResource.getOriginalFilename().substring(0, mediaResource.getOriginalFilename().lastIndexOf("."));
        final File source = storeTempFile(mediaResource);
        Future<String> future = segmentationTask.segmentVideo(source, fileName, targetFormat);
        future.get();
    }

    @Async("asyncExecutor")
    public void transmux(MultipartFile mediaResource, String targetFormat) throws IOException, ExecutionException, InterruptedException {
        final String fileName = mediaResource.getOriginalFilename().substring(0, mediaResource.getOriginalFilename().lastIndexOf("."));
        final File source = storeTempFile(mediaResource);
        final File target = new File(String.format("%s//%s.%s", TEMP_DIR, fileName, targetFormat));
        Future<String> future;
        switch (targetFormat) {
            case "m3u8":
                future = hlsTransmuxingTask.transmuxHls(source, target, fileName);
                break;
            case "mpd":
                future = dashTransmuxingTask.transmuxDash(source, target, fileName);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + targetFormat);
        }

        future.get();
        FileUtils.forceDelete(source);
    }

    private File storeTempFile(MultipartFile mediaResource) throws IOException {
        final File source = new File(TEMP_DIR.concat("/").concat(mediaResource.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(source);
        byte[] bytes = mediaResource.getInputStream().readAllBytes();
        fileOutputStream.write(bytes);
        fileOutputStream.flush();
        fileOutputStream.close();

        return source;
    }
}
