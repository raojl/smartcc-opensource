package com.smt.refactor.controller;

import com.smt.refactor.dto.WebRtcExtensionRequest;
import com.smt.refactor.service.WebRtcService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * WebRTC 分机管理 API（混合方案语音通道）
 *
 * 语音直连：浏览器 sip.js → wss://<webrtc.asterisk.host>:8089/ws（DTLS-SRTP/ICE）
 * 控制并行：坐席状态/事件仍走 /api/agent-phone（CTI 8082）
 *
 * 端点：
 *  - POST   /api/webrtc/extensions            创建坐席 WebRTC 分机（写 pjsip.conf + reload）
 *  - GET    /api/webrtc/extensions            分机列表
 *  - DELETE /api/webrtc/extensions/{exten}    删除分机
 *  - GET    /api/webrtc/config?exten=xxx      坐席接入配置（前端 PhoneBar 拉取）
 */
@RestController
@RequestMapping("/api/webrtc")
@RequiredArgsConstructor
public class WebRtcController {

    private final WebRtcService webRtcService;

    /** 创建坐席 WebRTC 分机 */
    @PostMapping("/extensions")
    public Map<String, Object> createExtension(@Validated @RequestBody WebRtcExtensionRequest req) {
        return webRtcService.createExtension(req.getExten(), req.getPassword(), req.getDisplayName());
    }

    /** WebRTC 分机列表 */
    @GetMapping("/extensions")
    public List<String> listExtensions() {
        return webRtcService.listExtensions();
    }

    /** 删除 WebRTC 分机 */
    @DeleteMapping("/extensions/{exten}")
    public Map<String, Object> deleteExtension(@PathVariable String exten) {
        return webRtcService.deleteExtension(exten);
    }

    /** 坐席 WebRTC 接入配置（前端 PhoneBar 初始化用） */
    @GetMapping("/config")
    public Map<String, Object> config(@RequestParam String exten) {
        return webRtcService.getAccessConfig(exten);
    }
}
