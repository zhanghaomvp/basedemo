CREATE TABLE `company` (
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
  INDEX login_name_index ( login_name )
);