package lg.connected_platform.gst.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lg.connected_platform.gst.repository.GstRepository;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.freedesktop.gstreamer.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Tag(name = "스트리밍(Stream)")
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamController {
    private final VideoRepository videoRepository;
    private final GstRepository gstRepository;
    private final S3Client s3Client;
    private final Map<Long, Pipeline> pipelineMap = new ConcurrentHashMap<>();

    //s3로 세그먼트와 플레이리스트 파일 업로드
    private void uploadToS3(String bucketName, Path filePath, String s3Key){
        try{
            s3Client.putObject(
                    builder -> builder.bucket(bucketName).key(s3Key),
                    filePath
            );
            System.out.println("File uploaded to S3 : " + s3Key);
        } catch (Exception e){
            System.out.println("Failed to upload file to S3 : " + e.getMessage());
            throw new RuntimeException("S3 Upload Error", e);
        }
    }



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
                            //System.out.println("Detected resolution: " + resolution[0] + "x" + resolution[1]);
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
        //영상 원본 url 가져오기
        Video video = videoRepository.findById(videoId)
                        .orElseThrow(()-> new RuntimeException("Video not found"));
        String videoSourceUrl = video.getSourceUrl();

        //hls 파일 저장 디렉토리 경로 설정
        Path projectRoot = Paths.get(System.getProperty("user.dir")); // 현재 프로젝트 디렉토리
        Path hlsDir = projectRoot.resolve("hls"); // hls 폴더 설정
        Files.createDirectories(hlsDir); // hls 폴더 생성
        Path playlistRoot = hlsDir.resolve("hls_" + videoId); // videoId별 폴더 생성
        Files.createDirectories(playlistRoot); // videoId별 폴더 생성
        gstRepository.save(new lg.connected_platform.gst.entity.Gst(playlistRoot, videoId));

        //gstreamer 파이프라인 선언
        Pipeline pipeline;


        //영상의 해상도 감지
        int[] resolution = getVideoResolution(videoSourceUrl);


        //영상의 해상도를 기반으로 gstreamer 파이프라인 설정
        if(resolution[1] <= 480){
            //영상 자체의 해상도가 낮은 경우 : low, medium만 지원
            pipeline = (Pipeline) Gst.parseLaunch(
                    "uridecodebin uri=" + videoSourceUrl + " ! tee name=t "
                            + "t. ! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 ! videoscale ! video/x-raw,width=360,height=240 ! x264enc bitrate=800 ! hlssink2 max-files=5 target-duration=10 name=low_sink location=" + playlistRoot.resolve("low_%05d.ts") + " playlist-location=" + playlistRoot.resolve("low_playlist.m3u8") + " "
                            + "t. ! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 ! videoscale ! video/x-raw,width=480,height=360 ! x264enc bitrate=1500 ! hlssink2 max-files=5 target-duration=10 name=medium_sink location=" + playlistRoot.resolve("medium_%05d.ts") + " playlist-location=" + playlistRoot.resolve("medium_playlist.m3u8")
            );
        }
        else{
            //영상 자체의 해상도가 높은 경우 : low, medium, high 모두 지원
            pipeline = (Pipeline) Gst.parseLaunch(
                    "uridecodebin uri=" + videoSourceUrl + " ! tee name=t "
                            + "t. ! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 ! videoscale ! video/x-raw,width=640,height=360 ! x264enc bitrate=800 ! hlssink2 max-files=5 target-duration=10 name=low_sink location=" + playlistRoot.resolve("low_%05d.ts") + " playlist-location=" + playlistRoot.resolve("low_playlist.m3u8") + " "
                            + "t. ! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 ! videoscale ! video/x-raw,width=1280,height=720 ! x264enc bitrate=1500 ! hlssink2 max-files=5 target-duration=10 name=medium_sink location=" + playlistRoot.resolve("medium_%05d.ts") + " playlist-location=" + playlistRoot.resolve("medium_playlist.m3u8") + " "
                            + "t. ! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 ! videoscale ! video/x-raw,width=1920,height=1080 ! x264enc bitrate=8000 ! hlssink2 max-files=5 target-duration=10 name=high_sink location=" + playlistRoot.resolve("high_%05d.ts") + " playlist-location=" + playlistRoot.resolve("high_playlist.m3u8")
            );
        }

        // Configure sinks
        configureSink(pipeline.getElementByName("low_sink"), playlistRoot.resolve("low_playlist.m3u8"), playlistRoot.resolve("low_%05d.ts"));
        configureSink(pipeline.getElementByName("medium_sink"), playlistRoot.resolve("medium_playlist.m3u8"), playlistRoot.resolve("medium_%05d.ts"));
        configureSink(pipeline.getElementByName("high_sink"), playlistRoot.resolve("high_playlist.m3u8"), playlistRoot.resolve("high_%05d.ts"));
        pipelineMap.put(videoId, pipeline);

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

        String bucketName = "connectedplatform";

        //각 hls 파일 업로드
        pipeline.getBus().connect((Bus.EOS) (source) -> {
            System.out.println("End of Stream reached. Starting S3 upload...");

            // S3 업로드
            try {
                Files.walk(playlistRoot).filter(Files::isRegularFile).forEach(file -> {
                    String s3Key = "hls/" + playlistRoot.getFileName().toString() + "/" + playlistRoot.relativize(file).toString();
                    uploadToS3(bucketName, file, s3Key);

                    try {
                        Files.delete(file); // 파일 삭제
                    } catch (IOException e) {
                        System.err.println("Failed to delete local file: " + file);
                    }
                });

                // 디렉토리 삭제
                Files.deleteIfExists(playlistRoot);
            } catch (IOException e) {
                System.err.println("Failed to upload or clean up files: " + e.getMessage());
            }

            Gst.quit();
        });

        return "Streaming started successfully! Access at your S3 bucket.";
    }

    //스트리밍 중단
    @PostMapping("/stop")
    @Operation(summary = "스트리밍 중단", description = "현재 활성화된 스트리밍을 중단")
    public String stopStreaming(@RequestParam("videoId") Long videoId) throws InterruptedException {
        Pipeline pipeline = pipelineMap.remove(videoId);
        if (pipeline != null) {
            //파이프라인 중단
            pipeline.stop();
            Gst.quit();
            Thread.sleep(1000);  // 1초 정도 대기하여 파일이 완전히 닫히도록 함
        }

        gstRepository.delete(gstRepository.findByVideoId(videoId)
                .orElseThrow(()-> new RuntimeException()));

        // 임시 파일 정리
        //cleanUpTempFiles(playlistRoot);

        return "Streaming stopped successfully!";
    }


    //master_playlist.m3u8 파일 반환
    @GetMapping("/{videoId}/master_playlist.m3u8")
    @Operation(summary = "마스터 플레이리스트 조회", description = "비디오 ID에 해당하는 마스터 플레이리스트(.m3u8)를 반환")
    public ResponseEntity<String> getMasterPlaylist(@PathVariable("videoId") Long videoId) {
        Path playlistRoot = gstRepository.findByVideoId(videoId)
                .orElseThrow(()-> new RuntimeException())
                .getPlaylistRoot();

        if(playlistRoot == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Master playlist not found");
        }

        String masterPlaylistUrl = "https://connectedplatform.s3.ap-northeast-2.amazonaws.com/hls/"
                + playlistRoot.getFileName().toString()
                + "/master_playlist.m3u8";
        System.out.println(masterPlaylistUrl);

        return ResponseEntity.ok(masterPlaylistUrl);
    }


    // 세그먼트 파일 반환
    @GetMapping("/{videoId}/{segment}")
    @Operation(summary = "세그먼트 파일 조회", description = "세그먼트 이름에 해당하는 HLS 세그먼트(.ts)를 반환")
    public ResponseEntity<String> getSegment(@PathVariable("videoId") Long videoId, @PathVariable("segment") String segment) {
        Path playlistRoot = gstRepository.findByVideoId(videoId)
                .orElseThrow(()-> new RuntimeException())
                .getPlaylistRoot();
        if(playlistRoot == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Segment not found");
        }

        String segmentUrl = "https://connectedplatform.s3.ap-northeast-2.amazonaws.com/hls/"
                + playlistRoot.getFileName().toString()
                +"/"
                + segment;

        return ResponseEntity.ok(segmentUrl);
    }



}