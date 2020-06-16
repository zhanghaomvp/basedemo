
CREATE TABLE IF NOT EXISTS `company` (
	id INT UNSIGNED AUTO_INCREMENT,
	`name` VARCHAR ( 50 ),
	login_name VARCHAR ( 50 ),
	`password` CHAR ( 60 ),
	contact VARCHAR ( 50 ),
	phone VARCHAR ( 20 ),
	email VARCHAR ( 100 ),
	social_credit_code VARCHAR ( 100 ),
	functions SMALLINT,
	STATUS TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
  INDEX login_name_index ( login_name ),
  INDEX social_credit_code_index ( social_credit_code )
);

CREATE TABLE IF NOT EXISTS `wallet` (
	id INT UNSIGNED AUTO_INCREMENT,
	`cash_balance` DECIMAL ( 13, 2 ),
	`credit_balance` DECIMAL ( 13, 2 ),
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` )
);


CREATE TABLE IF NOT EXISTS `company_member` (
	id INT UNSIGNED AUTO_INCREMENT,
	`company` INT UNSIGNED,
	wallet INT UNSIGNED,
	ic_no VARCHAR ( 50 ),
	`name` VARCHAR ( 50 ),
	phone VARCHAR ( 20 ),
	department VARCHAR ( 100 ),
	employee_no VARCHAR ( 50 ),
	`status` TINYINT,
	created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY ( `id` ),
	INDEX ic_no_index ( ic_no )
);