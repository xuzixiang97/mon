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
@TableName("cn_data")
public class CnData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sku;

    private String size;

    @TableField("pt_price")
    private Integer ptPrice;

    @TableField("plus_price")
    private Integer plusPrice;

    @TableField("js_price")
    private Integer jsPrice;

    @TableField("sizeUS")
    private String sizeUS;

    private String jobName;

    private Date createTime;


}
