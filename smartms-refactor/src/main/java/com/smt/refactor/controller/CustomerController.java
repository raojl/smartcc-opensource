package com.smt.refactor.controller;

import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.CustomQuery;
import com.smt.refactor.dto.CustomSaveRequest;
import com.smt.refactor.entity.Custominfo;
import com.smt.refactor.service.CustomerService;
import com.smt.refactor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 客户管理接口（翻译自老系统 /custominfo）
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;

    private String myAgentCode(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Integer)) {
            throw new BusinessException("未登录");
        }
        return userService.agentCodeOf((Integer) auth.getPrincipal());
    }

    /** 客户分页（agentId="my"=我的客户；deleted=true=回收站） */
    @GetMapping
    public PageResult<Custominfo> page(CustomQuery query, Authentication auth) {
        return customerService.page(query, myAgentCode(auth));
    }

    /** 详情 */
    @GetMapping("/{customId}")
    public Custominfo detail(@PathVariable String customId) {
        return customerService.detail(customId);
    }

    /** 新增（customId 空）/编辑（带 customId） */
    @PostMapping
    public Map<String, String> save(@Valid @RequestBody CustomSaveRequest req) {
        String id = customerService.save(req);
        return Collections.singletonMap("customId", id);
    }

    /** 软删除（status=1） */
    @DeleteMapping("/{customId}")
    public void delete(@PathVariable String customId) {
        customerService.changeDelete(customId, 1);
    }

    /** 恢复（status=0） */
    @PostMapping("/{customId}/restore")
    public void restore(@PathVariable String customId) {
        customerService.changeDelete(customId, 0);
    }

    /** 弹屏客户查询（按 custom_id / 手机号） */
    @GetMapping("/dial-search")
    public Custominfo dialSearch(@RequestParam(required = false) String customId,
                                 @RequestParam(required = false) String callNumber) {
        return customerService.findForDial(customId, null, callNumber);
    }

    /** Excel 导出（POI 生成 xlsx，对应老系统 exportCustEcxel） */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(CustomQuery query, Authentication auth) {
        PageResult<Custominfo> result = customerService.page(query, myAgentCode(auth));
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("客户信息");
            String[] headers = {"客户姓名", "性别", "公司", "手机", "电话", "QQ", "邮箱", "城市", "地址", "等级", "类型", "归属坐席"};
            Row head = sheet.createRow(0);
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            style.setFont(font);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = head.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
            }
            int rowIdx = 1;
            for (Custominfo c : result.getList()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nvl(c.getCustomName()));
                row.createCell(1).setCellValue(nvl(c.getCustomSex()));
                row.createCell(2).setCellValue(nvl(c.getCompanyName()));
                row.createCell(3).setCellValue(nvl(c.getCustomMobile()));
                row.createCell(4).setCellValue(nvl(c.getCustomPhone()));
                row.createCell(5).setCellValue(nvl(c.getQqNumber()));
                row.createCell(6).setCellValue(nvl(c.getEmailAddr()));
                row.createCell(7).setCellValue(nvl(c.getCustomCity()));
                row.createCell(8).setCellValue(nvl(c.getCustomAddr()));
                row.createCell(9).setCellValue(c.getCustomLevel() == null ? "" : String.valueOf(c.getCustomLevel()));
                row.createCell(10).setCellValue(nvl(c.getCustType()));
                row.createCell(11).setCellValue(nvl(c.getAgentid()));
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(bos);
            String filename = "custom_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bos.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
