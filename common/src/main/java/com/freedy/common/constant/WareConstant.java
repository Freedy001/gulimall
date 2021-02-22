package com.freedy.common.constant;

/**
 * @author Freedy
 * @date 2021/2/7 22:50
 */
public class WareConstant {
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        RECEIVE(2,"已领取"),
        FINISH(3,"已完成"),
        HASERROR(4,"有异常");

        private int code;
        private String mes;

        PurchaseStatusEnum(int code, String mes) {
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

    public enum DetailEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        BUYING(2,"正在采购"),
        FINISH(3,"已完成"),
        HASERROR(4,"以失败");

        private int code;
        private String mes;

        DetailEnum(int code, String mes) {
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
