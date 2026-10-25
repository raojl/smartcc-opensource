package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.entity.KeywordInfo;
import com.smt.refactor.entity.KeywordTopic;
import com.smt.refactor.entity.KnowlageReply;
import com.smt.refactor.entity.InterfaceInfo;
import com.smt.refactor.mapper.KeywordInfoMapper;
import com.smt.refactor.mapper.KeywordTopicMapper;
import com.smt.refactor.mapper.KnowlageReplyMapper;
import com.smt.refactor.mapper.InterfaceInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能消息服务（老系统 topicinfo/knowinfo/interf 模块）
 * 精准回复(im_keyword_topic) / 自动回复(im_knowlage_reply + im_keyword_info) / 消息记录(im_msg_received)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("biz")
public class ImService {

    private final KeywordTopicMapper keywordTopicMapper;
    private final KnowlageReplyMapper knowlageReplyMapper;
    private final KeywordInfoMapper keywordInfoMapper;
    private final InterfaceInfoMapper interfaceInfoMapper;
    private final JdbcTemplate jdbcTemplate;

    // ============ 精准回复（主题） ============

    public List<KeywordTopic> topicList(String keyword) {
        QueryWrapper<KeywordTopic> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like("title", keyword.trim());
        }
        qw.orderByDesc("id").last("limit 200");
        return keywordTopicMapper.selectList(qw);
    }

    public void topicSave(KeywordTopic topic) {
        if (topic.getId() == null) {
            keywordTopicMapper.insert(topic);
        } else {
            keywordTopicMapper.updateById(topic);
        }
    }

    public void topicDelete(Long id) {
        keywordTopicMapper.deleteById(id);
    }

    // ============ 自动回复（知识） ============

    public List<Map<String, Object>> replyList(String keyword) {
        QueryWrapper<KnowlageReply> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like("title", keyword.trim()).or().like("description", keyword.trim());
        }
        qw.orderByDesc("id").last("limit 200");
        List<KnowlageReply> list = knowlageReplyMapper.selectList(qw);
        List<Map<String, Object>> result = new ArrayList<>();
        for (KnowlageReply r : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("title", r.getTitle());
            m.put("description", r.getDescription());
            m.put("url", r.getUrl());
            m.put("picurl", r.getPicurl());
            m.put("knowType", r.getKnowType());
            m.put("verifyStatus", r.getVerifyStatus());
            m.put("customType", r.getCustomType());
            // 关联关键词
            QueryWrapper<KeywordInfo> kqw = new QueryWrapper<KeywordInfo>().eq("reply_id", r.getId());
            List<KeywordInfo> kws = keywordInfoMapper.selectList(kqw);
            StringBuilder kw = new StringBuilder();
            for (KeywordInfo k : kws) {
                if (kw.length() > 0) {
                    kw.append(", ");
                }
                kw.append(k.getContent());
            }
            m.put("keywords", kw.toString());
            result.add(m);
        }
        return result;
    }

    public void replySave(KnowlageReply reply) {
        if (reply.getId() == null) {
            knowlageReplyMapper.insert(reply);
        } else {
            knowlageReplyMapper.updateById(reply);
        }
    }

    public void replyDelete(Long id) {
        knowlageReplyMapper.deleteById(id);
    }

    // ============ 接入信息（微信/支付宝/IM 公众号接入） ============

    public List<InterfaceInfo> interfaceList() {
        return interfaceInfoMapper.selectList(new QueryWrapper<InterfaceInfo>().orderByDesc("id"));
    }

    public void interfaceSave(InterfaceInfo info) {
        if (info.getId() == null) {
            interfaceInfoMapper.insert(info);
        } else {
            interfaceInfoMapper.updateById(info);
        }
    }

    public void interfaceDelete(Long id) {
        interfaceInfoMapper.deleteById(id);
    }

    // ============ 消息记录（微信/IM 接入消息） ============

    /**
     * 消息记录分页（im_msg_received，本地库无数据时容错返回空）
     */
    public List<Map<String, Object>> msgRecords(String keyword, int limit) {
        String sql = "SELECT id, fromusername, tousername, msgtype, content, handled, "
                + "FROM_UNIXTIME(createtime/1000, '%Y-%m-%d %H:%i:%s') create_time, "
                + "user_name, agent_id FROM im_msg_received";
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " WHERE fromusername LIKE ? OR content LIKE ?";
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        sql += " ORDER BY id DESC LIMIT " + Math.min(Math.max(limit, 1), 500);
        try {
            return jdbcTemplate.queryForList(sql, args.toArray());
        } catch (Exception e) {
            log.warn("im_msg_received 查询失败(可能无表): {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
