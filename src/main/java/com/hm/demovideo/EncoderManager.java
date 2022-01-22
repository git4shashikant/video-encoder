package com.hm.demovideo;

import com.hm.demovideo.models.EncodingAttributeType;
import com.hm.demovideo.tasks.HLSTransmuxingTask;
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
    private static final String IMAGE_TARGET_FORMAT = "png";
    private static final String VIDEO_TARGET_FORMAT = "mp4";
    private static final String HLS_TARGET_FORMAT = "m3u8";

    private final HLSTransmuxingTask hlsTransmuxingTask;
    private final VideoEncoderTask videoEncoderTask;
    private final ThumbnailExtractorTask thumbnailExtractorTask;

    @Autowired
    public EncoderManager(HLSTransmuxingTask hlsTransmuxingTask,
                          VideoEncoderTask videoEncoderTask,
                          ThumbnailExtractorTask thumbnailExtractorTask) {
        this.hlsTransmuxingTask = hlsTransmuxingTask;
        this.videoEncoderTask = videoEncoderTask;
        this.thumbnailExtractorTask = thumbnailExtractorTask;
    }

    @Async("asyncExecutor")
    public void transmux(MultipartFile mediaResource) throws IOException, ExecutionException, InterruptedException {
        final String fileName = mediaResource.getOriginalFilename().substring(0, mediaResource.getOriginalFilename().lastIndexOf("."));
        final File source = storeTempFile(mediaResource);
        final File target = new File(String.format("%s//%s.%s", TEMP_DIR, fileName, HLS_TARGET_FORMAT));
        Future<String> future = hlsTransmuxingTask.transmux(source, target, fileName);
        future.get();

        FileUtils.forceDelete(source);
    }

    @Async("asyncExecutor")
    public void encode(MultipartFile mediaResource) throws IOException, EncoderException {
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
                    IMAGE_TARGET_FORMAT));
            imageResults.add(thumbnailExtractorTask.render(source, attributeType, 10, targetImage, 5));

            File targetVideo = new File(String.format("%s//%s_%d_%d_%d.%s", TEMP_DIR,
                    fileName,
                    attributeType.getVideoWidth(),
                    attributeType.getVideoHeight(),
                    attributeType.getVideoBitRate(),
                    VIDEO_TARGET_FORMAT));
            videoResults.add(videoEncoderTask.encode(source, attributeType, targetVideo, VIDEO_TARGET_FORMAT));
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
