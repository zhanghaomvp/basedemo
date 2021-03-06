-- $2a$10$UU4xXSNv9FIkgHc/wN79GeeePSWYZm79TXEtgV0iTHM9Erk3fur8i   kobekaka1
INSERT INTO `company`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `functions`, `STATUS`)
 VALUES (1, '中国电科', NULL, '17360026771', NULL, 'cetcxl', 1, 1);
INSERT INTO `company`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `functions`, `STATUS`)
 VALUES (2, '三十所', NULL, '17360126771', NULL, 'cetcxl-30', 1, 1);

INSERT INTO `company_user`(`id`, `company`, `phone`, `password`, `STATUS`)
VALUES (1, 1, '17360026771', '$2a$10$TcTaUT0x6USCDexzR.McsegoKMs/izIvO4SbjTcr7Wq3kY6hDozhy', 1);
INSERT INTO `company_user`(`id`, `company`, `phone`, `password`, `STATUS`)
VALUES (2, 1, '17360126771', '$2a$10$TcTaUT0x6USCDexzR.McsegoKMs/izIvO4SbjTcr7Wq3kY6hDozhy', 1);

--
INSERT INTO `company_member`(`id`, `company`, `ic_no`, `name`, `phone`, `department`, `employee_no`, `status`)
VALUES (1, 1, '511528198909010018', '张浩浩', '17360026771', '区块链', '13063', 1);
INSERT INTO `company_member`(`id`, `company`, `ic_no`, `name`, `phone`, `department`, `employee_no`, `status`)
VALUES (2, 1, '511528198909010019', '张浩浩1', '17360026772', '三十所', '13064', 1);
INSERT INTO `company_member`(`id`, `company`, `ic_no`, `name`, `phone`, `department`, `employee_no`, `status`)
VALUES (3, 2, '511528198909010018', '张浩浩', '17360026771', '区块链', '13063', 1);

--
INSERT INTO `wallet_cash`(`id`, `company_member`, `cash_balance`, `status`)
VALUES (1, 1, 0.00, 1);
INSERT INTO `wallet_cash`(`id`, `company_member`, `cash_balance`, `status`)
VALUES (2, 2, 0.00, 1);
INSERT INTO `wallet_cash`(`id`, `company_member`, `cash_balance`, `status`)
VALUES (3, 3, 100.00, 1);
--
INSERT INTO `wallet_credit`(`id`, `company_member`, `credit_balance`, `credit_quota`, `status`)
VALUES (1, 1, 50.00, 100.00, 0);
INSERT INTO `wallet_credit`(`id`, `company_member`, `credit_balance`, `credit_quota`, `status`)
VALUES (2, 2, 5.00, 10.00, 0);
INSERT INTO `wallet_credit`(`id`, `company_member`, `credit_balance`, `credit_quota`, `status`)
VALUES (3, 3, 5.00, 10.00, 0);
--
INSERT INTO `store`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `business_license`, `status`, `address`)
VALUES (1, 'shop1', 'shop1', '19900000001', NULL, 'shop1', NULL, 1, 'address');
INSERT INTO `store`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `business_license`, `status`, `address`)
VALUES (2, 'shop2', 'shop2', '19900000002', NULL, 'shop2', NULL, 1, 'address');
INSERT INTO `store`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `business_license`, `status`, `address`)
VALUES (3, 'shop3', 'shop3', '19900000003', NULL, 'shop3', NULL, 1, 'address');
INSERT INTO `store`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `business_license`, `status`, `address`)
VALUES (4, 'shop4', 'shop4', '19900000004', NULL, 'shop4', NULL, 1, 'address');

INSERT INTO `store_user`(`id`, `store`, `phone`, `password`, `STATUS`)
VALUES (1, 2, '17360026771', '$2a$10$qS.2WYtbfKCCi0DA9eehAe/E6z7TadzFQW1nVGdtUUAjkeVsiOyAy', 1);

--
INSERT INTO `company_store_relation`(`id`, `company`, `store`, `relation`, `apply_releation`, `status`)
VALUES (1, 1, 1, 1, NULL, 1);
INSERT INTO `company_store_relation`(`id`, `company`, `store`, `relation`, `apply_releation`, `status`)
VALUES (2, 1, 2, 1, 3, 0);
INSERT INTO `company_store_relation`(`id`, `company`, `store`, `relation`, `apply_releation`, `status`)
VALUES (3, 2, 1, 3, NULL, 1);
INSERT INTO `company_store_relation`(`id`, `company`, `store`, `relation`, `apply_releation`, `status`)
VALUES (4, 1, 3, null, 3, 0);

INSERT INTO `deal`(`id`, `company`, `company_member`, `store`, `ic_no`, `amount`, `type`, `pay_type`, `info`, `check_batch`, `status`, `created`, `updated`)
VALUES (1, 1, 1, 1, '511528198909010018', 1.00, 4, 0, 'test', NULL, 0, '2020-07-06 16:22:28', '2020-07-06 16:22:28');
INSERT INTO `deal`(`id`, `company`, `company_member`, `store`, `ic_no`, `amount`, `type`, `pay_type`, `info`, `check_batch`, `status`)
VALUES (2, 1, 1, 1, '511528198909010018', 2.00, 4, 0, 'test', NULL, 0);
INSERT INTO `deal`(`id`, `company`, `company_member`, `store`, `ic_no`, `amount`, `type`, `pay_type`, `info`, `check_batch`, `status`)
VALUES (3, 1, 1, 1, '511528198909010018', 5.00, 4, 0, 'test', 1, 1);
INSERT INTO `deal`(`id`, `company`, `company_member`, `store`, `ic_no`, `amount`, `type`, `pay_type`, `info`, `check_batch`, `status`)
VALUES (4, 1, 1, 1, '511528198909010018', 5.00, 4, 0, 'test', 2, 1);
INSERT INTO `deal`(`id`, `company`, `company_member`, `store`, `ic_no`, `amount`, `type`, `pay_type`, `info`, `check_batch`, `status`)
VALUES (5, 1, 2, 1, '511528198909010019', 5.00, 5, 1, 'test', 3, 1);

INSERT INTO `checks`(`batch`, `company`, `store`, `pay_type`, `total_deal_count`, `total_deal_amonut`, `attachments`, `info`, `status`)
VALUES (1, 1, 1, 0, 1, 5.00, NULL, '[{"status":"APPLY","info":"apply"}]', 0);
INSERT INTO `checks`(`batch`, `company`, `store`, `pay_type`, `total_deal_count`, `total_deal_amonut`, `attachments`, `info`, `status`)
VALUES (2, 1, 1, 0, 1, 5.00, NULL, '[{"status":"APPLY","info":"apply"},{"status":"REJECT","info":"reject"}]', 0);
INSERT INTO `checks`(`batch`, `company`, `store`, `pay_type`, `total_deal_count`, `total_deal_amonut`, `attachments`, `info`, `status`)
VALUES (3, 1, 1, 1, 1, 5.00, '1,2,3', '[{"status":"APPLY","info":"apply"},{"status":"APPROVAL","info":"approval"}]', 2);

INSERT INTO `checks_record`(`id`, `check_batch`, `action`, `operator`, `info`, `created`)
VALUES (1, 1, 0, 1, NULL, '2020-07-07 18:13:33');
INSERT INTO `checks_record`(`id`, `check_batch`, `action`, `operator`, `info`, `created`)
VALUES (2, 3, 0, 1, NULL, '2020-07-07 17:13:33');
INSERT INTO `checks_record`(`id`, `check_batch`, `action`, `operator`, `info`, `created`)
VALUES (3, 3, 2, 2, NULL, '2020-07-07 18:13:33');
INSERT INTO `checks_record`(`id`, `check_batch`, `action`, `operator`, `info`, `created`)
VALUES (4, 2, 1, 1, NULL, '2020-07-07 18:13:33');

INSERT INTO  `attachment` (`id`, `category`, `file_name`, `file_type`, `resoure`, `status`, `created`, `updated`)
VALUES ('1', '0', 'QQ图片20170220150932.jpg', '0', '1391416637625729024', '0', '2020-07-06 13:52:06', '2020-07-06 13:52:06');
INSERT INTO  `attachment` (`id`, `category`, `file_name`, `file_type`, `resoure`, `status`, `created`, `updated`)
VALUES ('2', '0', '下载.png', '0', '1391440618084892672', '0', '2020-07-06 15:27:06', '2020-07-06 15:27:06');
INSERT INTO  `attachment` (`id`, `category`, `file_name`, `file_type`, `resoure`, `status`, `created`, `updated`)
VALUES ('3', '0', '1.jpg', '0', '1391441287965573120', '0', '2020-07-06 15:29:46', '2020-07-06 15:29:46');
