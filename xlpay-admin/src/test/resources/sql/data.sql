--
INSERT INTO `company`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `functions`, `STATUS`)
 VALUES (1, '中国电科', NULL, '17360026771', NULL, 'cetcxl', 1, 1);
INSERT INTO `company_user`(`id`, `company`, `phone`, `password`, `STATUS`)
VALUES (1, 1, '17360026771', '$2a$10$TcTaUT0x6USCDexzR.McsegoKMs/izIvO4SbjTcr7Wq3kY6hDozhy', 1);

--
INSERT INTO `company_member`(`id`, `company`, `ic_no`, `name`, `phone`, `department`, `employee_no`, `status`)
VALUES (1, 1, '511528198909010018', '张浩浩', '17360026771', '区块链', '13063', 1);
INSERT INTO `company_member`(`id`, `company`, `ic_no`, `name`, `phone`, `department`, `employee_no`, `status`)
VALUES (2, 1, '511528198909010019', '张浩浩1', '17360026772', '区块链', '13064', 1);
--
INSERT INTO `wallet_cash`(`id`, `company_member`, `cash_balance`, `status`)
VALUES (1, 1, 0.00, 1);
INSERT INTO `wallet_cash`(`id`, `company_member`, `cash_balance`, `status`)
VALUES (2, 2, 0.00, 1);
--
INSERT INTO `wallet_credit`(`id`, `company_member`, `credit_balance`, `credit_quota`, `status`)
VALUES (1, 1, 50.00, 100.00, 0);
INSERT INTO `wallet_credit`(`id`, `company_member`, `credit_balance`, `credit_quota`, `status`)
VALUES (2, 2, 0.00, 0.00, 0);
--
INSERT INTO `store_user`(`id`, `store`, `phone`, `password`, `STATUS`)
VALUES (1, 2, '17360026771', '$2a$10$qS.2WYtbfKCCi0DA9eehAe/E6z7TadzFQW1nVGdtUUAjkeVsiOyAy', 1);
INSERT INTO `store`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `business_license`, `status`, `address`)
VALUES (1, 'shop1', 'shop1', '19900000001', NULL, 'shop1', NULL, 1, 'address');
INSERT INTO `store`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `business_license`, `status`, `address`)
VALUES (2, 'shop2', 'shop2', '19900000002', NULL, 'shop2', NULL, 1, 'address');
INSERT INTO `store`(`id`, `name`, `contact`, `phone`, `email`, `social_credit_code`, `business_license`, `status`, `address`)
VALUES (3, 'shop3', 'shop3', '19900000003', NULL, 'shop3', NULL, 1, 'address');

--
INSERT INTO `company_store_relation`(`id`, `company`, `store`, `relation`, `apply_releation`, `status`)
VALUES (1, 1, 1, 1, NULL, 1);
INSERT INTO `company_store_relation`(`id`, `company`, `store`, `relation`, `apply_releation`, `status`)
VALUES (2, 1, 2, 1, 3, 0);
