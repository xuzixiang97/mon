package com.footlocer.mon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author jht
 * @since 2024-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("stockx_data")
public class StockxData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sku;

    private Double size;

    @TableField("StockX")
    private Integer StockX;

    private Integer price;

    @TableField("source_size")
    private String sourceSize;

    @TableField("sales_Amount")
    private Integer salesAmount;

    @TableField("priceUS")
    private Integer priceUS;

    private String jobName;

    private Date createTime;


}
