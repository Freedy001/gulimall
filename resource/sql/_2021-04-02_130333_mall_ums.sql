/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE
DATABASE /*!32312 IF NOT EXISTS*/ mall_ums /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE
mall_ums;

DROP TABLE IF EXISTS ums_growth_change_history;
CREATE TABLE `ums_growth_change_history`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `member_id`    bigint(20) DEFAULT NULL COMMENT 'member_id',
    `create_time`  datetime                                                    DEFAULT NULL COMMENT 'create_time',
    `change_count` int(11) DEFAULT NULL COMMENT '改变的值（正负计数）',
    `note`         varchar(0) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    `source_type`  tinyint(4) DEFAULT NULL COMMENT '积分来源[0-购物，1-管理员修改]',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='成长值变化历史记录';

DROP TABLE IF EXISTS ums_integration_change_history;
CREATE TABLE `ums_integration_change_history`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `member_id`    bigint(20) DEFAULT NULL COMMENT 'member_id',
    `create_time`  datetime                                                      DEFAULT NULL COMMENT 'create_time',
    `change_count` int(11) DEFAULT NULL COMMENT '变化的值',
    `note`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    `source_tyoe`  tinyint(4) DEFAULT NULL COMMENT '来源[0->购物；1->管理员修改;2->活动]',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='积分变化历史记录';

DROP TABLE IF EXISTS ums_member;
CREATE TABLE `ums_member`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `level_id`      bigint(20) DEFAULT NULL COMMENT '会员等级id',
    `username`      char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     DEFAULT NULL COMMENT '用户名',
    `password`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '密码',
    `nickname`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '昵称',
    `mobile`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '手机号码',
    `email`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '邮箱',
    `header`        varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像',
    `gender`        tinyint(4) DEFAULT NULL COMMENT '性别',
    `birth`         date                                                          DEFAULT NULL COMMENT '生日',
    `city`          varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '所在城市',
    `job`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职业',
    `sign`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '个性签名',
    `source_type`   tinyint(4) DEFAULT NULL COMMENT '用户来源',
    `integration`   int(11) DEFAULT NULL COMMENT '积分',
    `growth`        int(11) DEFAULT NULL COMMENT '成长值',
    `status`        tinyint(4) DEFAULT NULL COMMENT '启用状态',
    `create_time`   datetime                                                      DEFAULT NULL COMMENT '注册时间',
    `access_token`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '访问令牌',
    `refresh_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '刷新令牌',
    `expires_in`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '访问令牌的过期时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员';

DROP TABLE IF EXISTS ums_member_collect_spu;
CREATE TABLE `ums_member_collect_spu`
(
    `id`          bigint(20) NOT NULL COMMENT 'id',
    `member_id`   bigint(20) DEFAULT NULL COMMENT '会员id',
    `spu_id`      bigint(20) DEFAULT NULL COMMENT 'spu_id',
    `spu_name`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'spu_name',
    `spu_img`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'spu_img',
    `create_time` datetime                                                      DEFAULT NULL COMMENT 'create_time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员收藏的商品';

DROP TABLE IF EXISTS ums_member_collect_subject;
CREATE TABLE `ums_member_collect_subject`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `subject_id`   bigint(20) DEFAULT NULL COMMENT 'subject_id',
    `subject_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'subject_name',
    `subject_img`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'subject_img',
    `subject_urll` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '活动url',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员收藏的专题活动';

DROP TABLE IF EXISTS ums_member_level;
CREATE TABLE `ums_member_level`
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name`                    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '等级名称',
    `growth_point`            int(11) DEFAULT NULL COMMENT '等级需要的成长值',
    `default_status`          tinyint(4) DEFAULT NULL COMMENT '是否为默认等级[0->不是；1->是]',
    `free_freight_point`      decimal(18, 4)                                                DEFAULT NULL COMMENT '免运费标准',
    `comment_growth_point`    int(11) DEFAULT NULL COMMENT '每次评价获取的成长值',
    `priviledge_free_freight` tinyint(4) DEFAULT NULL COMMENT '是否有免邮特权',
    `priviledge_member_price` tinyint(4) DEFAULT NULL COMMENT '是否有会员价格特权',
    `priviledge_birthday`     tinyint(4) DEFAULT NULL COMMENT '是否有生日特权',
    `note`                    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员等级';

DROP TABLE IF EXISTS ums_member_login_log;
CREATE TABLE `ums_member_login_log`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `member_id`   bigint(20) DEFAULT NULL COMMENT 'member_id',
    `create_time` datetime                                                     DEFAULT NULL COMMENT '创建时间',
    `ip`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'ip',
    `city`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'city',
    `login_type`  tinyint(1) DEFAULT NULL COMMENT '登录类型[1-web，2-app]',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员登录记录';

DROP TABLE IF EXISTS ums_member_receive_address;
CREATE TABLE `ums_member_receive_address`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `member_id`      bigint(20) DEFAULT NULL COMMENT 'member_id',
    `name`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收货人姓名',
    `phone`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '电话',
    `post_code`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '邮政编码',
    `province`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省份/直辖市',
    `city`           varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '城市',
    `region`         varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区',
    `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '详细地址(街道)',
    `areacode`       varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '省市区代码',
    `default_status` tinyint(1) DEFAULT NULL COMMENT '是否默认',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员收货地址';

DROP TABLE IF EXISTS ums_member_statistics_info;
CREATE TABLE `ums_member_statistics_info`
(
    `id`                    bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `member_id`             bigint(20) DEFAULT NULL COMMENT '会员id',
    `consume_amount`        decimal(18, 4) DEFAULT NULL COMMENT '累计消费金额',
    `coupon_amount`         decimal(18, 4) DEFAULT NULL COMMENT '累计优惠金额',
    `order_count`           int(11) DEFAULT NULL COMMENT '订单数量',
    `coupon_count`          int(11) DEFAULT NULL COMMENT '优惠券数量',
    `comment_count`         int(11) DEFAULT NULL COMMENT '评价数',
    `return_order_count`    int(11) DEFAULT NULL COMMENT '退货数量',
    `login_count`           int(11) DEFAULT NULL COMMENT '登录次数',
    `attend_count`          int(11) DEFAULT NULL COMMENT '关注数量',
    `fans_count`            int(11) DEFAULT NULL COMMENT '粉丝数量',
    `collect_product_count` int(11) DEFAULT NULL COMMENT '收藏的商品数量',
    `collect_subject_count` int(11) DEFAULT NULL COMMENT '收藏的专题活动数量',
    `collect_comment_count` int(11) DEFAULT NULL COMMENT '收藏的评论数量',
    `invite_friend_count`   int(11) DEFAULT NULL COMMENT '邀请的朋友数量',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='会员统计信息';

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



INSERT INTO ums_member(id, level_id, username, password, nickname, mobile, email, header, gender, birth, city, job,
                       sign, source_type, integration, growth, status, create_time, access_token, refresh_token,
                       expires_in)
VALUES (3, 1, 'freedy', '$2a$10$E71lQws7W1W2G/sUXrQy7uRbmGC8K9Jz36cwb7RwOiUvcYZ2mq0Xy', NULL, NULL, '985948228@qq.com',
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2021-03-11 12:28:36', NULL, NULL, NULL),
       (8, 1, 'Freedyamazing', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1,
        '1970-01-19 16:45:55', '795693d791685230dc6098797af2277d',
        'e8add786e5da2ed4c2aeea99883205111f84dd2e2098c93e722f2e5468519c0b', '86400');



INSERT INTO ums_member_level(id, name, growth_point, default_status, free_freight_point, comment_growth_point,
                             priviledge_free_freight, priviledge_member_price, priviledge_birthday, note)
VALUES (1, '普通会员', 0, 1, 299.0000, 10, 0, 0, 1, '初级会员'),
       (2, '铜牌会员', 3000, 0, 199.0000, 30, 0, 1, 1, '铜牌会员'),
       (3, '银牌', 5000, 0, 99.0000, 50, 0, 1, 1, '银牌会员'),
       (4, '金牌会员', 10000, 0, 0.0000, 100, 1, 1, 1, '金牌会员');


INSERT INTO ums_member_receive_address(id, member_id, name, phone, post_code, province, city, region, detail_address,
                                       areacode, default_status)
VALUES (1, 8, '你爸爸', '110', '123321', '湖北', '武汉', '蕊尼玛啊', '想在哪都行', '什么玩意', 1),
       (2, 8, '你爷爷', '119', '2333', '湖北', '武汉', '热i你大爷', '详细尼玛啊', '不知道是啥玩意', 0);


/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;