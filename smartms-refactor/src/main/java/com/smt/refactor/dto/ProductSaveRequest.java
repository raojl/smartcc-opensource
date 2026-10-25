package com.smt.refactor.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 产品新增/编辑请求（老系统 ProductsManagerController saveOrupdate）
 */
@Data
public class ProductSaveRequest {

    private Long id;
    @NotBlank(message = "产品编号不能为空")
    private String productId;
    @NotBlank(message = "产品名称不能为空")
    private String productName;
    private BigDecimal productUnitPrice;
    private String productType;
}
