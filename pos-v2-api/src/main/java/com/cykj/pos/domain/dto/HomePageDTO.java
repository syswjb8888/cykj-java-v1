package com.cykj.pos.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class HomePageDTO {
    /**
     * 商户id
     */
    @ApiModelProperty(value="商户id")
    private Long merchId;
    /**
     * 用户id
     */
    @ApiModelProperty(value="用户id")
    private Long userId;
    /**
     * 当前日期 年月
     */
    @ApiModelProperty(value="当前日期")
    private String yearMonth;


}
