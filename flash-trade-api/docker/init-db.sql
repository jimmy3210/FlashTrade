-- ===================================================
-- FlashTradeAPI Docker 数据库初始化脚本
-- 创建开发 / 测试 / 生产三个数据库并授权
-- ===================================================

CREATE DATABASE IF NOT EXISTS `flash_trade_dev`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `flash_trade_test`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `flash_trade_prod`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON `flash_trade_dev`.*  TO 'flashtrade'@'%';
GRANT ALL PRIVILEGES ON `flash_trade_test`.* TO 'flashtrade'@'%';
GRANT ALL PRIVILEGES ON `flash_trade_prod`.* TO 'flashtrade'@'%';

FLUSH PRIVILEGES;
