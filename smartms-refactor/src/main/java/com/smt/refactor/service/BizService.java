package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.smt.refactor.entity.Custominfo;
import com.smt.refactor.entity.Talkinfo;
import com.smt.refactor.mapper.CustominfoMapper;
import com.smt.refactor.mapper.TalkinfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务库（smartcc）数据访问服务 —— 阶段 0 验证用
 */
@Service
@RequiredArgsConstructor
@DS("biz")
public class BizService {

    private final CustominfoMapper custominfoMapper;
    private final TalkinfoMapper talkinfoMapper;

    public long countCustom() {
        return custominfoMapper.selectCount(null);
    }

    public List<Custominfo> listCustom() {
        return custominfoMapper.selectList(null);
    }

    public long countTalk() {
        return talkinfoMapper.selectCount(null);
    }
}
