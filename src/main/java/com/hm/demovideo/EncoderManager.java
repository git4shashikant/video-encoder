package com.hm.demovideo;

import com.hm.demovideo.models.EncodingAttributeType;
import com.hm.demovideo.tasks.VideoEncoderTask;
import org.apache.tomcat.util.http.fileupload.FileUtils;
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

    private final VideoEncoderTask videoEncoderTask;

    public EncoderManager(VideoEncoderTask videoEncoderTask) {
        this.videoEncoderTask = videoEncoderTask;
    }

    @Async("asyncExecutor")
    public void encode(MultipartFile mediaResource) throws IOException, EncoderException {
        /* Step 1. Declaring source file and Target file */
        File source = new File(TEMP_DIR.concat("/").concat(mediaResource.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(source);
        byte[] bytes = mediaResource.getInputStream().readAllBytes();
        fileOutputStream.write(bytes);
        fileOutputStream.flush();
        fileOutputStream.close();

        List<Future> results = new ArrayList<>(EncodingAttributeType.values().length);
        for (EncodingAttributeType attributeType : EncodingAttributeType.values()) {
            results.add(videoEncoderTask.encode(source, attributeType));
        }

        results.forEach(result -> {
            try {
                result.get();
            } catch (InterruptedException | ExecutionException e) {
                //handle thread error
            }
        });

        cleanupFiles();
    }

    private void cleanupFiles() throws IOException {
        FileUtils.cleanDirectory(new File(TEMP_DIR));
    }
}
