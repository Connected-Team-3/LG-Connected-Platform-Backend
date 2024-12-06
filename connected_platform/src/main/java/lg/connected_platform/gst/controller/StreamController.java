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
        System.setProperty("GST_DEBUG", "3");
        Gst.init(Version.of(1, 16), "HLS");
        Utils.configurePaths();
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

        //hls 파일 저장 디렉토리 경로 설정
        Path playlistRoot = Files.createTempDirectory("hls_" + videoId);
        playlistRoots.put(videoId, playlistRoot);

        //gstreamer 파이프라인 선언
        Pipeline pipeline;


        //영상의 해상도 감지
        Element probeElement = Gst.parseLaunch("uridecodebin uri=" + videoSourceUrl);
        int width, height;
        try {
            Pad pad = probeElement.getStaticPad("src");
            Caps caps = pad.getCurrentCaps();
            Structure structure = caps.getStructure(0);
            width = structure.getInteger("width");
            height = structure.getInteger("height");
            System.out.println("Input video resolution: " + width + "x" + height);
        } finally {
            probeElement.dispose();
        }


        //영상의 해상도를 기반으로 gstreamer 파이프라인 설정
        if(height <= 480){
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
        if(height <= 480){
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
    @GetMapping("/{segment}")
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

}