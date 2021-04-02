/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE
DATABASE /*!32312 IF NOT EXISTS*/ mall_wms /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE
mall_wms;

DROP TABLE IF EXISTS undo_log;
CREATE TABLE `undo_log`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT,
    `branch_id`     bigint(20) NOT NULL,
    `xid`           varchar(100) NOT NULL,
    `context`       varchar(128) NOT NULL,
    `rollback_info` longblob     NOT NULL,
    `log_status`    int(11) NOT NULL,
    `log_created`   datetime     NOT NULL,
    `log_modified`  datetime     NOT NULL,
    `ext`           varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS wms_purchase;
CREATE TABLE `wms_purchase`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT,
    `assignee_id`   bigint(20) DEFAULT NULL,
    `assignee_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `phone`         char(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     DEFAULT NULL,
    `priority`      int(11) DEFAULT NULL,
    `status`        int(11) DEFAULT NULL,
    `ware_id`       bigint(20) DEFAULT NULL,
    `amount`        decimal(18, 4)                                                DEFAULT NULL,
    `create_time`   datetime                                                      DEFAULT NULL,
    `update_time`   datetime                                                      DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购信息';

DROP TABLE IF EXISTS wms_purchase_detail;
CREATE TABLE `wms_purchase_detail`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `purchase_id` bigint(20) DEFAULT NULL COMMENT '采购单id',
    `sku_id`      bigint(20) DEFAULT NULL COMMENT '采购商品id',
    `sku_num`     int(11) DEFAULT NULL COMMENT '采购数量',
    `sku_price`   decimal(18, 4) DEFAULT NULL COMMENT '采购金额',
    `ware_id`     bigint(20) DEFAULT NULL COMMENT '仓库id',
    `status`      int(11) DEFAULT NULL COMMENT '状态[0新建，1已分配，2正在采购，3已完成，4采购失败]',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购细节';

DROP TABLE IF EXISTS wms_ware_info;
CREATE TABLE `wms_ware_info`
(
    `id`       bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '仓库名',
    `address`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '仓库地址',
    `areacode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '区域编码',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='仓库信息';

DROP TABLE IF EXISTS wms_ware_order_task;
CREATE TABLE `wms_ware_order_task`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `order_id`         bigint(20) DEFAULT NULL COMMENT 'order_id',
    `order_sn`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'order_sn',
    `consignee`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收货人',
    `consignee_tel`    char(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     DEFAULT NULL COMMENT '收货人电话',
    `delivery_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '配送地址',
    `order_comment`    varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订单备注',
    `payment_way`      tinyint(1) DEFAULT NULL COMMENT '付款方式【 1:在线付款 2:货到付款】',
    `task_status`      tinyint(4) DEFAULT NULL COMMENT '任务状态',
    `order_body`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订单描述',
    `tracking_no`      char(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     DEFAULT NULL COMMENT '物流单号',
    `create_time`      datetime                                                      DEFAULT NULL COMMENT 'create_time',
    `ware_id`          bigint(20) DEFAULT NULL COMMENT '仓库id',
    `task_comment`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工作单备注',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存工作单';

DROP TABLE IF EXISTS wms_ware_order_task_detail;
CREATE TABLE `wms_ware_order_task_detail`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `sku_id`      bigint(20) DEFAULT NULL COMMENT 'sku_id',
    `sku_name`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'sku_name',
    `sku_num`     int(11) DEFAULT NULL COMMENT '购买个数',
    `task_id`     bigint(20) DEFAULT NULL COMMENT '工作单id',
    `ware_id`     bigint(20) DEFAULT NULL,
    `lock_status` int(11) DEFAULT NULL COMMENT '1-锁定 2-解锁 3-扣减',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存工作单';

DROP TABLE IF EXISTS wms_ware_sku;
CREATE TABLE `wms_ware_sku`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `sku_id`       bigint(20) DEFAULT NULL COMMENT 'sku_id',
    `ware_id`      bigint(20) DEFAULT NULL COMMENT '仓库id',
    `stock`        int(11) DEFAULT NULL COMMENT '库存数',
    `sku_name`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'sku_name',
    `stock_locked` int(11) DEFAULT NULL COMMENT '锁定库存',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品库存';


/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
