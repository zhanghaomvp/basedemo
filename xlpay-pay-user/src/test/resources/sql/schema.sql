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
