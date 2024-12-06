package lg.connected_platform.gst.controller;

import org.freedesktop.gstreamer.*;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/stream")
public class StreamController {

    private static Pipeline pipeline;
    private static Path playlistRoot;

    @PostMapping("/start")
    public String startStreaming() throws IOException {
        if (pipeline != null) {
            return "Streaming is already running.";
        }
        System.setProperty("GST_DEBUG", "3");

        Utils.configurePaths();
        Gst.init(Version.of(1, 16), "HLS");

        String caps = "video/x-raw, width=640, height=360, pixel-aspect-ratio=1/1, framerate=30/1, "
                + (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "format=BGRx" : "format=xRGB");

        pipeline = (Pipeline) Gst.parseLaunch(
                "uridecodebin uri=http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4 ! "
                        + "identity name=identity ! videoconvert ! videoscale ! x264enc ! hlssink2 name=sink");


        Element identity = pipeline.getElementByName("identity");
        identity.getStaticPad("sink")
                .addProbe(PadProbeType.BUFFER, new Renderer(640, 360));

        String videoUri = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4";
        String videoName = Paths.get(URI.create(videoUri).getPath()).getFileName().toString().replace(".mp4", "");


        //playlistRoot = Files.createTempDirectory("hls");
        playlistRoot = Files.createTempDirectory(videoName);
        Element sink = pipeline.getElementByName("sink");
        sink.set("playlist-location", playlistRoot.resolve("playlist.m3u8").toString());
        sink.set("location", playlistRoot.resolve("segment%05d.ts").toString());

        pipeline.getBus().connect((Bus.ERROR) ((source, code, message) -> {
            System.err.println("GStreamer Error (" + code + "): " + message);
            Gst.quit();
        }));
        pipeline.getBus().connect((Bus.EOS) (source) -> Gst.quit());
        pipeline.play();

        return "Streaming started successfully! Access at /hls/playlist.m3u8";
    }

    @PostMapping("/stop")
    public String stopStreaming() {
        if (pipeline == null) {
            return "No streaming session to stop.";
        }

        pipeline.stop();
        Gst.quit();
        pipeline = null;

        return "Streaming stopped successfully!";
    }
    // playlist.m3u8 파일 반환
    @GetMapping("/{videoName}/playlist.m3u8")
    public ResponseEntity<Resource> getPlaylist(@PathVariable("videoName") String videoName) {
        Path playlistPath = Paths.get(System.getProperty("java.io.tmpdir"), videoName, "playlist.m3u8");
        if (Files.exists(playlistPath)) {
            Resource playlistResource = new PathResource(playlistPath);
            return ResponseEntity.ok().body(playlistResource);
        } else {
            // 파일이 아직 생성되지 않은 경우, VLC에 503 응답을 반환해 재시도하게 합니다.
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
        }
    }

    // 세그먼트 파일 반환
    @GetMapping("/{videoName}/{segment}")
    public ResponseEntity<Resource> getSegment(@PathVariable("videoName") String videoName, @PathVariable("segment") String segment) {
        Path segmentPath = Paths.get(System.getProperty("java.io.tmpdir"), videoName, segment);
        if (Files.exists(segmentPath)) {
            Resource segmentResource = new PathResource(segmentPath);
            return ResponseEntity.ok().body(segmentResource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    static class Renderer implements Pad.PROBE {
        private final BufferedImage image;
        private final int[] data;
        private final Point[] points;
        private final Paint fill;

        private Renderer(int width, int height) {
            image = new BufferedImage
                    (width, height, BufferedImage.TYPE_INT_RGB);
            data = ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();
            points = new Point[18];
            for (int i = 0; i < points.length; i++) {
                points[i] = new Point();
            }
            fill = new GradientPaint(0, 0, new Color(1.0f, 0.3f, 0.5f, 0.9f),
                    60, 20, new Color(0.3f, 1.0f, 0.7f, 0.8f), true);
        }

        @Override
        public PadProbeReturn probeCallback(Pad pad, PadProbeInfo info) {
            Buffer buffer = info.getBuffer();

            if (buffer.isWritable()) {
                IntBuffer ib = buffer.map(true).asIntBuffer();
                ib.get(data);
                render();
                ib.rewind();
                ib.put(data);
                buffer.unmap();
            }
            return PadProbeReturn.OK;
        }

        private void render() {
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            for (Point point : points) {
                point.tick();
            }
            GeneralPath path = new GeneralPath();
            path.moveTo(points[0].x, points[0].y);
            for (int i = 2; i < points.length; i += 2) {
                path.quadTo(points[i - 1].x, points[i - 1].y,
                        points[i].x, points[i].y);
            }
            path.closePath();
            path.transform(AffineTransform.getScaleInstance(image.getWidth(), image.getHeight()));
            g2d.setPaint(fill);
            g2d.fill(path);
            g2d.setColor(Color.BLACK);
            g2d.draw(path);
        }
    }

    static class Point {
        private double x, y, dx, dy;

        private Point() {
            this.x = Math.random();
            this.y = Math.random();
            this.dx = 0.02 * Math.random();
            this.dy = 0.02 * Math.random();
        }

        private void tick() {
            x += dx;
            y += dy;
            if (x < 0 || x > 1) {
                dx = -dx;
            }
            if (y < 0 || y > 1) {
                dy = -dy;
            }
        }
    }

}