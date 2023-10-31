package cn.bitoffer.seckill.model;

import cn.bitoffer.seckill.common.BaseModel;

import java.io.Serializable;

public class Goods extends BaseModel implements Serializable {
    /**
     * Id
     */
    private Long ID;

    private String goodsNum;
    /**
     * Name
     */
    private String goodsName;
    private Float price;

    private String picUrl;
    private Long Seller;

    public Long getGoodsID() {
        return ID;
    }

    @Override
    public String toString() {
        return "Example{" +
                "goodsID=" + ID +
                ", goodsName='" + goodsName + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                '}';
    }
}
