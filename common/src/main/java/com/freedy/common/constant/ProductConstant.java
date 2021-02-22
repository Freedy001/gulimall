package com.freedy.common.constant;

/**
 * @author Freedy
 * @date 2021/2/4 23:03
 */
public class ProductConstant {
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),ATTR_TYPE_SALE(0,"销售属性");
        private int code;
        private String mes;

        AttrEnum(int code, String mes) {
            this.code = code;
            this.mes = mes;
        }

        public int getCode() {
            return code;
        }

        public String getMes() {
            return mes;
        }
    }

    public enum Status{
        NEW_SPU(0,"新建"),
        SPU_UP(1,"商品上架"),
        SPU_DOWN(2,"商品下架");
        private int code;
        private String mes;

        Status(int code, String mes) {
            this.code = code;
            this.mes = mes;
        }

        public int getCode() {
            return code;
        }

        public String getMes() {
            return mes;
        }
    }
}
