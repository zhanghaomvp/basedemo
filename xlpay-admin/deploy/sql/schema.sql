DROP DATABASE IF EXISTS xinyufu;
CREATE DATABASE xinyufu character set utf8;
USE xinyufu;
SET FOREIGN_KEY_CHECKS=0;
SET sql_mode='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';

CREATE TABLE `company_user` (
	id INT UNSIGNED AUTO_INCREMENT,
	`company` INT UNSIGNED,
	`phone` VARCHAR ( 20 ),
	`password` CHAR ( 60 ),
	STATUS TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX company_user_phone_index ( phone )
);

CREATE TABLE `company` (
	id INT UNSIGNED AUTO_INCREMENT,
	`name` VARCHAR ( 50 ),
	contact VARCHAR ( 50 ),
	phone VARCHAR ( 20 ),
	email VARCHAR ( 100 ),
	social_credit_code VARCHAR ( 100 ) NOT NULL UNIQUE,
	functions SMALLINT,
	STATUS TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX company_social_credit_code_index ( social_credit_code )
);

CREATE TABLE `company_member` (
	id INT UNSIGNED AUTO_INCREMENT,
	`company` INT UNSIGNED,
	ic_no VARCHAR ( 50 ),
	`name` VARCHAR ( 50 ),
	phone VARCHAR ( 20 ),
	department VARCHAR ( 100 ),
	employee_no VARCHAR ( 50 ),
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX company_member_ic_no_index ( ic_no )
);


CREATE TABLE `store_user` (
	id INT UNSIGNED AUTO_INCREMENT,
	`store` INT UNSIGNED,
	`phone` VARCHAR ( 20 ),
	`password` CHAR ( 60 ),
	STATUS TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX store_user_phone_index ( phone )
);

CREATE TABLE `store` (
	id INT UNSIGNED AUTO_INCREMENT,
	`name` VARCHAR ( 50 ),
	contact VARCHAR ( 50 ),
	phone VARCHAR ( 20 ),
	email VARCHAR ( 100 ),
	address VARCHAR ( 200 ),
	social_credit_code VARCHAR ( 100 ),
	business_license VARCHAR ( 200 ),
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` )
);

CREATE TABLE `company_store_relation` (
	id INT UNSIGNED AUTO_INCREMENT,
	`company` INT UNSIGNED,
	`store` INT UNSIGNED,
	`relation` INT UNSIGNED,
	`apply_releation` INT UNSIGNED,
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX company_store_relation_company_index ( company ),
	INDEX company_store_relation_store_index ( store )
);

--
CREATE TABLE `wallet_cash` (
	id INT UNSIGNED AUTO_INCREMENT,
	`company_member` INT UNSIGNED,
	`cash_balance` DECIMAL ( 13, 2 ),
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX wallet_cash_company_member_index ( company_member )
);

CREATE TABLE `wallet_credit` (
	id INT UNSIGNED AUTO_INCREMENT,
	`company_member` INT UNSIGNED,
	`credit_balance` DECIMAL ( 13, 2 ),
	`credit_quota` DECIMAL ( 13, 2 ),
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ) ,
	INDEX wallet_credit_company_member_index ( company_member )
);

CREATE TABLE `wallet_cash_flow` (
	 id INT UNSIGNED AUTO_INCREMENT,
	`wallet_cash` INT UNSIGNED,
	`deal` INT UNSIGNED,
	`amount` DECIMAL ( 13, 2 ),
	`balance` DECIMAL ( 13, 2 ),
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX wallet_cash_flow_wallet_cash_index ( wallet_cash )
);
ALTER TABLE `wallet_cash_flow` ADD INDEX wallet_cash_flow_created_index ( `created` );
ALTER TABLE `wallet_cash_flow` ADD COLUMN `type` TINYINT NOT NULL;
ALTER TABLE `wallet_cash_flow` ADD COLUMN `info` VARCHAR ( 200 ) NOT NULL;

CREATE TABLE `wallet_credit_flow` (
	 id INT UNSIGNED AUTO_INCREMENT,
	`wallet_credit` INT UNSIGNED,
	`deal` INT UNSIGNED,
	`amount` DECIMAL ( 13, 2 ),
	`balance` DECIMAL ( 13, 2 ),
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX wallet_credit_flow_wallet_credit_index ( wallet_credit )
);
ALTER TABLE `wallet_credit_flow` ADD INDEX wallet_credit_flow_created_index ( `created` );
ALTER TABLE `wallet_credit_flow` ADD COLUMN `type` TINYINT NOT NULL;
ALTER TABLE `wallet_credit_flow` ADD COLUMN `info` VARCHAR ( 200 ) NOT NULL;
ALTER TABLE `wallet_credit_flow` ADD COLUMN `quota` DECIMAL ( 13, 2 ) NOT NULL;

CREATE TABLE `deal` (
	 id INT UNSIGNED AUTO_INCREMENT,
	`company` INT UNSIGNED,
	`company_member` INT UNSIGNED,
	`store` INT UNSIGNED,
	`amount` DECIMAL ( 13, 2 ),
	`type` TINYINT,
	`pay_type` TINYINT,
	`info` VARCHAR(250),
	`check_batch` INT UNSIGNED,
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX deal_company_index ( company ),
	INDEX deal_company_member_index ( company_member ),
	INDEX deal_store_index ( store )
);

ALTER TABLE `deal` ADD INDEX deal_created_index ( `created` ) ;
ALTER TABLE `deal` ADD INDEX deal_updated_index ( `updated` ) ;
ALTER TABLE `deal` ADD INDEX deal_check_batch ( `check_batch` ) ;


-- 7月05日
CREATE TABLE `attachment` (
	`id` INT UNSIGNED AUTO_INCREMENT,
	`category` SMALLINT,
	`file_name` VARCHAR ( 100 ),
	`file_type` INT UNSIGNED,
	`resoure` VARCHAR ( 100 ),
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY ( `id` )
);

CREATE TABLE `checks` (
	`batch` INT UNSIGNED AUTO_INCREMENT,
	`company` INT UNSIGNED,
	`store` INT UNSIGNED,
	`pay_type` TINYINT,
	`total_deal_count` INT,
	`total_deal_amonut` DECIMAL ( 13, 2 ),
	`attachments` VARCHAR ( 300 ),
	`info` VARCHAR ( 300 ),
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `batch` ),
	INDEX checks_company_index ( company ),
	INDEX checks_store_index ( store )
);

ALTER TABLE `store` modify COLUMN `business_license` INT UNSIGNED;

CREATE TABLE `checks_record` (
	`id` INT UNSIGNED AUTO_INCREMENT,
	`check_batch` INT UNSIGNED,
	`action` TINYINT,
	`operator` INT UNSIGNED,
	`info` VARCHAR ( 300 ),
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX checks_record_check_batch_index ( check_batch )
);

ALTER TABLE `deal` ADD COLUMN `ic_no` VARCHAR ( 50 );
ALTER TABLE `deal` ADD INDEX deal_ic_no_index ( `ic_no` ) ;

CREATE TABLE
IF
	NOT EXISTS `pay_user` (
		id INT UNSIGNED AUTO_INCREMENT,
		ic_no VARCHAR ( 50 ),
		`password` CHAR ( 60 ),
		functions SMALLINT,
		identity_flag TINYINT,
		`status` TINYINT,
		created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
		updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
		PRIMARY KEY ( `id` ),
	INDEX pay_user_ic_no_index ( ic_no )
	);

ALTER TABLE `pay_user` ADD COLUMN `phone` VARCHAR ( 20 );
ALTER TABLE `pay_user` ADD COLUMN `locked_dead_line` TIMESTAMP;