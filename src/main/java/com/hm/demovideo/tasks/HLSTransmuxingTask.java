package com.hm.demovideo.tasks;

import com.azure.storage.blob.models.BlobHttpHeaders;
import com.hm.demovideo.interfaces.IVideoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import ws.schild.jave.process.ProcessLocator;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Future;

@Component
public class HLSTransmuxingTask {

    private static final String MP4_CONTENT_TYPE = "video/mp4";
    private static final Log LOG = LogFactory.getLog(HLSTransmuxingTask.class);
    public static final String M3U8_CONTENT_TYPE = "application/vnd.apple.mpegurl";

    private final ProcessLocator locator;
    private final IVideoService videoService;

    @Autowired
    public HLSTransmuxingTask(IVideoService videoService) {
        this.locator = new DefaultFFMPEGLocator();
        this.videoService = videoService;
    }

    @Async("asyncExecutor")
    public Future<String> transmux(File source, File target, String fileName) throws IOException {
        var ffmpeg = this.locator.createExecutor();

        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(source.getAbsolutePath());
        ffmpeg.addArgument("-codec:");
        ffmpeg.addArgument("copy");
        ffmpeg.addArgument("-start_number");
        ffmpeg.addArgument("0");
        ffmpeg.addArgument("-hls_time");
        ffmpeg.addArgument("10");
        ffmpeg.addArgument("-hls_list_size");
        ffmpeg.addArgument("0");
        ffmpeg.addArgument("-f");
        ffmpeg.addArgument("hls");
        ffmpeg.addArgument(target.getAbsolutePath());

        ffmpeg.execute();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            int lineNR = 0;
            String line;
            while ((line = reader.readLine()) != null)
            {
                lineNR++;
                LOG.debug("Input Line (" + lineNR + "): " + line);
            }
        } finally {
            ffmpeg.destroy();
        }

        var headers = new BlobHttpHeaders();
        headers.setContentType(M3U8_CONTENT_TYPE);
        Arrays.stream(target.getParentFile().listFiles())
                .filter(file -> file.getName().contains(fileName) && file.getName().endsWith(".ts"))
                .forEach(tsFile -> videoService.uploadFile(tsFile, headers));
        String uploadUrl = videoService.uploadFile(target, headers);

        Arrays.stream(target.getParentFile().listFiles())
                .filter(file -> file.getName().contains(fileName) && file.getName().endsWith(".ts"))
                .forEach(tsFile -> {
                    try {
                        FileUtils.forceDelete(tsFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        FileUtils.forceDelete(target);
        return new AsyncResult<>(uploadUrl);
    }
}
