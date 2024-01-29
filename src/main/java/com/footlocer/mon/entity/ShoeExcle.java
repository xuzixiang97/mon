package com.footlocer.mon.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author jht
 * @since 2024-01-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("shoe_excle")
public class ShoeExcle implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sku;

    private Integer stockxPrice;

    private Double stockxHandPrice;

    private Integer dewuPrice;

    private Double dewuHandPrice;

    private Double priceDifference;

    private Integer saleAmount;

    private String jobName;

    private Date createTime;


}
