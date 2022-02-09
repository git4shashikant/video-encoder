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

/**********************************************************************************************************
* ffmpeg -re -y -i <input> -c copy -f dash -window_size 10 -use_template 1 -use_timeline 1 <ClearLive>.mpd
************************************************************************************************************/
@Component
public class DASHTransmuxingTask {

    private static final Log LOG = LogFactory.getLog(DASHTransmuxingTask.class);
    public static final String MPD_CONTENT_TYPE = "application/dash+xml";

    private final ProcessLocator locator;
    private final IVideoService videoService;

    @Autowired
    public DASHTransmuxingTask(IVideoService videoService) {
        this.locator = new DefaultFFMPEGLocator();
        this.videoService = videoService;
    }

    @Async("asyncExecutor")
    public Future<String> transmuxDash(File source, File target, String fileName) throws IOException {
        var ffmpeg = this.locator.createExecutor();

        ffmpeg.addArgument("-re");
        ffmpeg.addArgument("-y");
        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(source.getAbsolutePath());
        ffmpeg.addArgument("-codec:");
        ffmpeg.addArgument("copy");
        ffmpeg.addArgument("-f");
        ffmpeg.addArgument("dash");
        ffmpeg.addArgument("-seg_duration");
        ffmpeg.addArgument("10");
        ffmpeg.addArgument("-use_template");
        ffmpeg.addArgument("1");
        ffmpeg.addArgument("-use_timeline");
        ffmpeg.addArgument("1");
        ffmpeg.addArgument("-init_seg_name");
        ffmpeg.addArgument(fileName + "$RepresentationID$.$ext$");
        ffmpeg.addArgument("-media_seg_name");
        ffmpeg.addArgument(fileName + "$RepresentationID$-$Number%05d$.$ext$");
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
        headers.setContentType(MPD_CONTENT_TYPE);
        Arrays.stream(target.getParentFile().listFiles())
                .filter(file -> file.getName().contains(fileName) && file.getName().endsWith(".m4s"))
                .forEach(tsFile -> videoService.uploadFile(tsFile, headers));
        String uploadUrl = videoService.uploadFile(target, headers);

        Arrays.stream(target.getParentFile().listFiles())
                .filter(file -> file.getName().contains(fileName) && file.getName().endsWith(".m4s"))
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
