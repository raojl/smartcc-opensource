package com.smt.refactor;

import com.smt.refactor.entity.Custominfo;
import com.smt.refactor.entity.SysUser;
import com.smt.refactor.entity.Trunk;
import com.smt.refactor.service.BizService;
import com.smt.refactor.service.ConfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 阶段 0 验证：双数据源连通性 + 表映射正确性
 */
@SpringBootTest
class DualDataSourceTest {

    @Autowired
    private ConfService confService;

    @Autowired
    private BizService bizService;

    @Test
    void confDataSource_shouldQuerySysUser() {
        long count = confService.countUser();
        assertTrue(count > 0, "smartconf.cc_core_sys_user 应有数据");
        List<SysUser> users = confService.listUsers();
        assertFalse(users.isEmpty(), "用户列表不应为空");
        System.out.println("[conf] cc_core_sys_user count=" + count + ", first=" + users.get(0).getLoginName());
    }

    @Test
    void confDataSource_shouldQueryMenu() {
        long count = confService.countMenu();
        assertTrue(count >= 60, "菜单应 >= 60 条，实际 " + count);
        System.out.println("[conf] cc_core_sys_menu count=" + count);
    }

    @Test
    void confDataSource_shouldQueryTrunk() {
        List<Trunk> trunks = confService.listTrunks();
        System.out.println("[conf] cc_core_trunk count=" + trunks.size());
        assertNotNull(trunks, "trunk 查询不应为 null");
    }

    @Test
    void bizDataSource_shouldQueryCustominfo() {
        long count = bizService.countCustom();
        assertTrue(count > 0, "smartcc.crm_custominfo 应有数据（420 条）");
        List<Custominfo> list = bizService.listCustom();
        assertFalse(list.isEmpty(), "客户列表不应为空");
        System.out.println("[biz] crm_custominfo count=" + count + ", first custName=" + list.get(0).getCustomName());
    }

    @Test
    void bizDataSource_shouldQueryTalkinfo() {
        long count = bizService.countTalk();
        System.out.println("[biz] base_talkinfo count=" + count);
        assertNotNull(count, "talkinfo 查询不应为 null");
    }
}
