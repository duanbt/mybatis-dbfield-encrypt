CREATE TABLE `person`
(
    `id`           varchar(64) NOT NULL,
    `name`         varchar(64)  DEFAULT NULL,
    `identity`     varchar(100) DEFAULT NULL,
    `phone_number` varchar(100) DEFAULT NULL,
    `company_code` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT '人员信息'
  DEFAULT CHARSET = utf8;