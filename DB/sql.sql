/*
 Navicat Premium Data Transfer

 Source Server         : 139.224.131.2
 Source Server Type    : MySQL
 Source Server Version : 50742 (5.7.42-log)
 Source Host           : 139.224.131.2:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 50742 (5.7.42-log)
 File Encoding         : 65001

 Date: 30/01/2024 10:14:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cn_data
-- ----------------------------
DROP TABLE IF EXISTS `cn_data`;
CREATE TABLE `cn_data`  (
                            `id` int(11) NOT NULL AUTO_INCREMENT,
                            `sku` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                            `size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                            `pt_price` int(11) NULL DEFAULT NULL,
                            `plus_price` int(11) NULL DEFAULT NULL,
                            `js_price` int(11) NULL DEFAULT NULL,
                            `sizeUS` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                            `job_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                            `create_time` datetime NULL DEFAULT NULL,
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8441 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shoe_excle
-- ----------------------------
DROP TABLE IF EXISTS `shoe_excle`;
CREATE TABLE `shoe_excle`  (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT,
                               `job_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                               `sku` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                               `size_us` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                               `size_cn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                               `stockx_price` int(11) NULL DEFAULT NULL,
                               `stockx_hand_price` double NULL DEFAULT NULL,
                               `dewu_price` int(11) NULL DEFAULT NULL,
                               `dewu_hand_price` double NULL DEFAULT NULL,
                               `price_difference` double NULL DEFAULT NULL,
                               `sale_amount` int(11) NULL DEFAULT NULL,
                               `create_time` datetime NULL DEFAULT NULL,
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3619 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for stockx_data
-- ----------------------------
DROP TABLE IF EXISTS `stockx_data`;
CREATE TABLE `stockx_data`  (
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `sku` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                `size` double NULL DEFAULT NULL,
                                `StockX` int(11) NULL DEFAULT NULL,
                                `price` int(11) NULL DEFAULT NULL,
                                `source_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                `sales_Amount` int(11) NULL DEFAULT NULL,
                                `priceUS` int(11) NULL DEFAULT NULL,
                                `job_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                `create_time` datetime NULL DEFAULT NULL,
                                PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5776 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for touch_sku
-- ----------------------------
DROP TABLE IF EXISTS `touch_sku`;
CREATE TABLE `touch_sku`  (
                              `id` int(11) NOT NULL AUTO_INCREMENT,
                              `sku` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                              `update_time` datetime NULL DEFAULT NULL,
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `sku`(`sku`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1409 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for touch_sku_copy1
-- ----------------------------
DROP TABLE IF EXISTS `touch_sku_copy1`;
CREATE TABLE `touch_sku_copy1`  (
                                    `id` int(11) NOT NULL AUTO_INCREMENT,
                                    `sku` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                    `update_time` datetime NULL DEFAULT NULL,
                                    `status` int(11) NOT NULL DEFAULT 0 COMMENT '0-未知  1-已查询',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `sku`(`sku`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1409 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
