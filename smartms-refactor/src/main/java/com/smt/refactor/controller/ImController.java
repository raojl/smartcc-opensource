package com.smt.refactor.controller;

import com.smt.refactor.entity.InterfaceInfo;
import com.smt.refactor.entity.KeywordTopic;
import com.smt.refactor.entity.KnowlageReply;
import com.smt.refactor.service.ImService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能消息 API（老系统 topicinfo/knowinfo 模块）
 */
@RestController
@RequestMapping("/api/im")
@RequiredArgsConstructor
public class ImController {

    private final ImService imService;

    /** 精准回复主题列表（老系统 topicinfo index） */
    @GetMapping("/topics")
    public List<KeywordTopic> topics(@RequestParam(required = false) String keyword) {
        return imService.topicList(keyword);
    }

    /** 保存精准回复主题 */
    @PostMapping("/topics")
    public void topicSave(@RequestBody KeywordTopic topic) {
        imService.topicSave(topic);
    }

    /** 删除精准回复主题 */
    @DeleteMapping("/topics/{id}")
    public void topicDelete(@PathVariable Long id) {
        imService.topicDelete(id);
    }

    /** 自动回复知识列表（老系统 knowinfo index，含关键词聚合） */
    @GetMapping("/replies")
    public List<Map<String, Object>> replies(@RequestParam(required = false) String keyword) {
        return imService.replyList(keyword);
    }

    /** 保存自动回复知识 */
    @PostMapping("/replies")
    public void replySave(@RequestBody KnowlageReply reply) {
        imService.replySave(reply);
    }

    /** 删除自动回复知识 */
    @DeleteMapping("/replies/{id}")
    public void replyDelete(@PathVariable Long id) {
        imService.replyDelete(id);
    }

    /** 消息记录（老系统 msgRecordNew） */
    @GetMapping("/records")
    public List<Map<String, Object>> msgRecords(@RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "50") int limit) {
        return imService.msgRecords(keyword, limit);
    }

    /** 接入信息列表（老系统 interfController） */
    @GetMapping("/interfaces")
    public List<InterfaceInfo> interfaces() {
        return imService.interfaceList();
    }

    /** 保存接入信息 */
    @PostMapping("/interfaces")
    public void interfaceSave(@RequestBody InterfaceInfo info) {
        imService.interfaceSave(info);
    }

    /** 删除接入信息 */
    @DeleteMapping("/interfaces/{id}")
    public void interfaceDelete(@PathVariable Long id) {
        imService.interfaceDelete(id);
    }
}
