package com.freedy.mall.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/2/6 15:45
 */
@Data
public class SpuSaveVo implements Serializable {


    /**
     * spuName : 华为 HUAWEI P40 Pro+
     * spuDescription : 麒麟990 5G SoC芯片 5000万超感知徕卡五摄 100倍双目变焦 8GB+256GB陶瓷黑全网通5G
     * catalogId : 225
     * brandId : 1
     * weight : 0.21
     * publishStatus : 0
     * decript : []
     * images : [“”，“”]
     * bounds : {"buyBounds":500,"growBounds":500}
     * baseAttrs : [{},{}]
     * skus : [{},{}]
     */

    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;
    private BoundsBean bounds;
    private List<String> images;
    private List<BaseAttrsBean> baseAttrs;
    private List<SkusBean> skus;
    private List<String> decript;

    @Data
    public static class BoundsBean implements Serializable {
        /**
         * buyBounds : 500
         * growBounds : 500
         */

        private BigDecimal buyBounds;
        private BigDecimal growBounds;
    }

    @Data
    public static class BaseAttrsBean implements Serializable {
        /**
         * attrId : 6
         * attrValues : 162.9mm
         * showDesc : 1
         */

        private Long attrId;
        private String attrValues;
        private int showDesc;
    }

    @Data
    public static class SkusBean implements Serializable {
        /**
         * attr : [{"attrId":5,"attrName":"运行内存","attrValue":"8GB"},{"attrId":13,"attrName":"机身颜色","attrValue":"陶瓷黑"}]
         * skuName : 华为 HUAWEI P40 Pro+ 8GB 陶瓷黑
         * price : 7988
         * skuTitle : 华为 HUAWEI P40 Pro+ 8GB 陶瓷黑  麒麟990 5G SoC芯片 5000万超感知徕卡五摄 100倍双目变焦
         * skuSubtitle : 【nova8系列新品上市】麒麟985芯片，稀缺货源限量抢购！猛戳》》》华为手机热销爆款，限量抢购查看>
         * images : [{"imgUrl":"","defaultImg":0},{"imgUrl":"https://freedy.oss-cn-shanghai.aliyuncs.com/2021-02-06//dade88c3-b3cd-4298-8a64-f7cc0eac2d5e_2a542499846ace02.jpg","defaultImg":1}]
         * descar : ["8GB","陶瓷黑"]
         * fullCount : 3
         * discount : 0.98
         * countStatus : 1
         * fullPrice : 10000
         * reducePrice : 100
         * priceStatus : 1
         * memberPrice : [{"id":2,"name":"铜牌会员","price":7888},{"id":3,"name":"银牌","price":7777},{"id":4,"name":"金牌会员","price":7500}]
         */

        private String skuName;
        private BigDecimal price;
        private String skuTitle;
        private String skuSubtitle;
        private int fullCount;
        private BigDecimal discount;
        private int countStatus;
        private BigDecimal fullPrice;
        private BigDecimal reducePrice;
        private int priceStatus;
        private List<AttrBean> attr;
        private List<ImagesBean> images;
        private List<String> descar;
        private List<MemberPriceBean> memberPrice;

        @Data
        public static class AttrBean implements Serializable {
            /**
             * attrId : 5
             * attrName : 运行内存
             * attrValue : 8GB
             */

            private Long attrId;
            private String attrName;
            private String attrValue;
        }

        @Data
        public static class ImagesBean implements Serializable {
            /**
             * imgUrl :
             * defaultImg : 0
             */

            private String imgUrl;
            private int defaultImg;
        }

        @Data
        public static class MemberPriceBean implements Serializable {
            /**
             * id : 2
             * name : 铜牌会员
             * price : 7888
             */

            private Long id;
            private String name;
            private BigDecimal price;
        }
    }
}

