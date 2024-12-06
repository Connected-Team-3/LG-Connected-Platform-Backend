package lg.connected_platform.gst.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "스트리밍(Stream)")
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamController {
    private final Map<Long, Pipeline> pipelines = new ConcurrentHashMap<>(); //videoId별로 파이프라인 관리
    private final Map<Long, Path> playlistRoots = new ConcurrentHashMap<>(); //videoId별로 hls 파일 경로 관리
    private final VideoRepository videoRepository;

    @PostConstruct
    public void initGStreamer() {
        System.setProperty("GST_DEBUG", "4");
        //System.setProperty("java.library.path", "C:\\gstreamer\\1.0\\msvc_x86_64\\bin");
        Gst.init(Version.of(1, 16), "HLS");
        Utils.configurePaths();
    }

    private void configureSink(Element sink, Path playlistLocation, Path segmentLocation) {
        sink.set("playlist-location", playlistLocation.toString());
        sink.set("location", segmentLocation.toString());
    }

    public int[] getVideoResolution(String videoSourceUrl) {
        int[] resolution = {0, 0}; // Default resolution in case of failure

        try {
            // GStreamer 파이프라인 생성
            Element source = ElementFactory.make("uridecodebin", "source");
            Element sink = ElementFactory.make("fakesink", "sink");

            // 소스 URL 설정
            source.set("uri", videoSourceUrl);

            // 파이프라인 생성 및 요소 추가
            Pipeline pipeline = new Pipeline();
            pipeline.addMany(source, sink);
            Element.linkMany(source, sink);

            // Pad가 추가되었을 때 처리
            source.connect((Element.PAD_ADDED) (element, pad) -> {
                try {
                    // 캡스를 통해 해상도 추출
                    Caps caps = pad.getCurrentCaps();
                    if (caps != null) {
                        Structure struct = caps.getStructure(0);
                        if (struct.hasField("width") && struct.hasField("height")) {
                            resolution[0] = struct.getInteger("width");
                            resolution[1] = struct.getInteger("height");
                            System.out.println("Detected resolution: " + resolution[0] + "x" + resolution[1]);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error extracting resolution: " + e.getMessage());
                }
            });

            // 파이프라인 실행
            pipeline.play();

            // 파이프라인 실행 후 잠시 대기
            Thread.sleep(3000); // 3초 대기
            pipeline.stop();
        } catch (Exception e) {
            System.err.println("Error initializing pipeline: " + e.getMessage());
        }

        return resolution;
    }

    @PostMapping("/start")
    @Operation(summary = "스트리밍 시작", description = "지정된 비디오 ID로 스트리밍을 시작")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스트리밍이 성공적으로 시작되었습니다."),
            @ApiResponse(responseCode = "404", description = "지정된 비디오를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "스트리밍 시작 중 오류가 발생했습니다.")
    })
    public String startStreaming(@RequestParam("videoId") Long videoId) throws IOException {
        //이미 스트리밍이 진행 중인 경우
        if (pipelines.containsKey(videoId)) {
            return "Streaming is already running for this video.";
        }
        
        //영상 원본 url 가져오기
        Video video = videoRepository.findById(videoId)
                        .orElseThrow(()-> new RuntimeException("Video not found"));
        String videoSourceUrl = video.getSourceUrl();
        System.out.println(videoSourceUrl);

        //hls 파일 저장 디렉토리 경로 설정
        //Path playlistRoot = Files.createTempDirectory("hls_" + videoId);
        Path projectRoot = Paths.get(System.getProperty("user.dir")); // 현재 프로젝트 디렉토리
        Path hlsDir = projectRoot.resolve("hls"); // hls 폴더 설정
        Files.createDirectories(hlsDir); // hls 폴더 생성
        Path playlistRoot = hlsDir.resolve("hls_" + videoId); // videoId별 폴더 생성
        Files.createDirectories(playlistRoot); // videoId별 폴더 생성
        System.out.println(playlistRoot);
        playlistRoots.put(videoId, playlistRoot);

        //gstreamer 파이프라인 선언
        Pipeline pipeline;


        //영상의 해상도 감지
        /*Element probeElement = Gst.parseLaunch("uridecodebin uri=" + videoSourceUrl);
        int[] resolution = new int[2]; // Array to store width and height
        probeElement.connect((Element.PAD_ADDED) (element, pad) -> {
            pad.addProbe(PadProbeType.BLOCK_DOWNSTREAM, (Pad.PROBE) (pad1, info) -> {
                try {
                    Caps caps = pad.getCurrentCaps();
                    if (caps != null && caps.size() > 0) {
                        Structure structure = caps.getStructure(0);
                        resolution[0] = structure.getInteger("width");
                        resolution[1] = structure.getInteger("height");
                        System.out.println("Input video resolution: " + resolution[0] + "x" + resolution[1]);
                    } else {
                        System.err.println("Failed to detect video resolution: Caps are null or empty.");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to detect video resolution: " + e.getMessage());
                } finally {
                    probeElement.dispose();
                }
                return PadProbeReturn.REMOVE;
            });
        });*/

        int[] resolution = getVideoResolution(videoSourceUrl);
        System.out.println("Video resolution: " + resolution[0] + "x" + resolution[1]);



        //영상의 해상도를 기반으로 gstreamer 파이프라인 설정
        if(resolution[1] <= 480){
            //영상 자체의 해상도가 낮은 경우 : low, medium만 지원
            pipeline = (Pipeline) Gst.parseLaunch(
                    "uridecodebin uri=" + videoSourceUrl + " ! tee name=t "
                            + "t. ! queue ! videoscale ! video/x-raw,width=360,height=240 ! x264enc bitrate=500 ! hlssink2 name=low_sink location=" + playlistRoot.resolve("low_%05d.ts") + " playlist-location=" + playlistRoot.resolve("low_playlist.m3u8") + " "
                            + "t. ! queue ! videoscale ! video/x-raw,width=480,height=360 ! x264enc bitrate=800 ! hlssink2 name=medium_sink location=" + playlistRoot.resolve("medium_%05d.ts") + " playlist-location=" + playlistRoot.resolve("medium_playlist.m3u8")
            );
        }
        else{
            //영상 자체의 해상도가 높은 경우 : low, medium, high 모두 지원
            pipeline = (Pipeline) Gst.parseLaunch(
                    "uridecodebin uri=" + videoSourceUrl + " ! tee name=t "
                            + "t. ! queue ! videoscale ! video/x-raw,width=640,height=360 ! x264enc bitrate=800 ! hlssink2 name=low_sink location=" + playlistRoot.resolve("low_%05d.ts") + " playlist-location=" + playlistRoot.resolve("low_playlist.m3u8") + " "
                            + "t. ! queue ! videoscale ! video/x-raw,width=1280,height=720 ! x264enc bitrate=1500 ! hlssink2 name=medium_sink location=" + playlistRoot.resolve("medium_%05d.ts") + " playlist-location=" + playlistRoot.resolve("medium_playlist.m3u8") + " "
                            + "t. ! queue ! videoscale ! video/x-raw,width=1920,height=1080 ! x264enc bitrate=4000 ! hlssink2 name=high_sink location=" + playlistRoot.resolve("high_%05d.ts") + " playlist-location=" + playlistRoot.resolve("high_playlist.m3u8")
            );
        }

        /*Element lowSink = pipeline.getElementByName("low_sink");
        Element mediumSink = pipeline.getElementByName("medium_sink");
        Element highSink = pipeline.getElementByName("high_sink");

        if(resolution[1] <= 480){
            lowSink.getStaticPad("sink").addProbe(PadProbeType.BUFFER, new Renderer(640, 360));
            mediumSink.getStaticPad("sink").addProbe(PadProbeType.BUFFER, new Renderer(1280, 720));
        }
        else{
            lowSink.getStaticPad("sink").addProbe(PadProbeType.BUFFER, new Renderer(640, 360));
            mediumSink.getStaticPad("sink").addProbe(PadProbeType.BUFFER, new Renderer(1280, 720));
            highSink.getStaticPad("sink").addProbe(PadProbeType.BUFFER, new Renderer(1920, 1080));
        }*/

        // Configure sinks
        configureSink(pipeline.getElementByName("low_sink"), playlistRoot.resolve("low_playlist.m3u8"), playlistRoot.resolve("low_%05d.ts"));
        configureSink(pipeline.getElementByName("medium_sink"), playlistRoot.resolve("medium_playlist.m3u8"), playlistRoot.resolve("medium_%05d.ts"));
        configureSink(pipeline.getElementByName("high_sink"), playlistRoot.resolve("high_playlist.m3u8"), playlistRoot.resolve("high_%05d.ts"));
        pipelines.put(videoId, pipeline);


        //gstreamer 버스 이벤트 연결
        pipeline.getBus().connect((Bus.ERROR) ((source, code, message)->{
            System.err.println("GStreamer Error (" + code + "): " + message);
            Gst.quit();
        }));
        pipeline.getBus().connect((Bus.EOS) (source)->{
            System.out.println("End of Stream reached");
            Gst.quit();
        });


        //마스터 플레이리스트 생성
        Path masterPlaylistPath = playlistRoot.resolve("master_playlist.m3u8");
        if(resolution[1] <= 480){
            Files.write(masterPlaylistPath, Arrays.asList(
                    "#EXTM3U",
                    "#EXT-X-STREAM-INF:BANDWIDTH=500000,RESOLUTION=360x240",
                    "low_playlist.m3u8",
                    "#EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=480x360",
                    "medium_playlist.m3u8"
            ));
        }
        else{
            Files.write(masterPlaylistPath, Arrays.asList(
                    "#EXTM3U",
                    "#EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x360",
                    "low_playlist.m3u8",
                    "#EXT-X-STREAM-INF:BANDWIDTH=1500000,RESOLUTION=1280x720",
                    "medium_playlist.m3u8",
                    "#EXT-X-STREAM-INF:BANDWIDTH=4000000,RESOLUTION=1920x1080",
                    "high_playlist.m3u8"
            ));
        }

        //파이프라인 실행
        pipeline.play();


        return "Streaming started successfully! Access at /hls_" + videoId + "/master_playlist.m3u8";
    }


    //스트리밍 중단
    @PostMapping("/stop")
    @Operation(summary = "스트리밍 중단", description = "현재 활성화된 스트리밍을 중단")
    public String stopStreaming(@RequestParam("videoId") Long videoId) {
        Pipeline pipeline = pipelines.remove(videoId);
        if (pipeline != null) {
            //파이프라인 중단
            pipeline.stop();
            Gst.quit();
        }

        //hls 파일 정리
        Path playlistRoot = playlistRoots.remove(videoId);
        if(playlistRoot != null) {
            try {
                Files.walk(playlistRoot)
                        .map(Path::toFile)
                        .forEach(File::delete);
                Files.delete(playlistRoot);
            } catch (IOException e) {
                System.err.println("Streaming stopped, but failed to delete HLS files : " + e.getMessage());
            }
        }

        return "Streaming stopped successfully!";
    }


    //master_playlist.m3u8 파일 반환
    @GetMapping("/{videoId}/master_playlist.m3u8")
    @Operation(summary = "마스터 플레이리스트 조회", description = "비디오 ID에 해당하는 마스터 플레이리스트(.m3u8)를 반환")
    public ResponseEntity<Resource> getMasterPlaylist(@PathVariable("videoId") Long videoId) {
        Path playlistRoot = playlistRoots.get(videoId);
        if(playlistRoot == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Path playlistPath = playlistRoot.resolve("master_playlist.m3u8");

        if(Files.exists(playlistPath)){
            return ResponseEntity.ok().body(new PathResource(playlistPath));
        }
        else{
            //파일이 없으면 404 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    // 세그먼트 파일 반환
    @GetMapping("/{videoId}/{segment}")
    @Operation(summary = "세그먼트 파일 조회", description = "세그먼트 이름에 해당하는 HLS 세그먼트(.ts)를 반환")
    public ResponseEntity<Resource> getSegment(@PathVariable("videoId") Long videoId, @PathVariable("segment") String segment) {
        Path playlistRoot = playlistRoots.get(videoId);
        if(playlistRoot == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Path segmentPath = playlistRoot.resolve(segment);

        if(Files.exists(segmentPath)){
            return ResponseEntity.ok().body(new PathResource(segmentPath));
        }
        else{
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