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
	`id` VARCHAR ( 20 ) UNIQUE,
	`wallet_cash` INT UNSIGNED,
	`deal` VARCHAR ( 20 ),
	`amount` DECIMAL ( 13, 2 ),
	`balance` DECIMAL ( 13, 2 ),
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX wallet_cash_flow_wallet_cash_index ( wallet_cash )
);
ALTER TABLE `wallet_cash_flow` ADD INDEX wallet_cash_flow_created_index ( `created` );

CREATE TABLE `wallet_credit_flow` (
	`id` VARCHAR ( 20 ) UNIQUE,
	`wallet_credit` INT UNSIGNED,
	`deal` VARCHAR ( 20 ),
	`amount` DECIMAL ( 13, 2 ),
	`balance` DECIMAL ( 13, 2 ),
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX wallet_credit_flow_wallet_credit_index ( wallet_credit )
);
ALTER TABLE `wallet_credit_flow` ADD INDEX wallet_credit_flow_created_index ( `created` );


CREATE TABLE `deal` (
	`id` VARCHAR ( 20 ) UNIQUE,
	`company` INT UNSIGNED,
	`company_member` INT UNSIGNED,
	`store` INT UNSIGNED,
	`amount` DECIMAL ( 13, 2 ),
	`type` TINYINT,
	`pay_type` TINYINT,
	`info` VARCHAR(250),
	`check` INT UNSIGNED,
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



