-- One-time MySQL migration after changing JPA entity IDs from BigInteger to Long.
-- Run this manually against food_db1 while the backend is stopped.
--
-- Strongly recommended before running:
--   mysqldump -u root -p food_db1 > food_db1_backup_before_bigint.sql
--
-- This keeps table data. It drops current FK constraints, changes ID/FK columns
-- to BIGINT, then recreates the FK constraints used by the application mapping.

USE food_db1;

DROP PROCEDURE IF EXISTS food_add_fk_if_missing;
DROP PROCEDURE IF EXISTS food_alter_column_if_exists;
DROP PROCEDURE IF EXISTS food_drop_all_foreign_keys;
DROP PROCEDURE IF EXISTS food_migrate_id_columns_to_bigint;

DELIMITER $$

CREATE PROCEDURE food_alter_column_if_exists(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_column_definition TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @sql = CONCAT(
            'ALTER TABLE `', p_table_name, '` ',
            'MODIFY COLUMN `', p_column_name, '` ', p_column_definition
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE food_drop_all_foreign_keys()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_table_name VARCHAR(128);
    DECLARE v_constraint_name VARCHAR(128);

    DECLARE fk_cursor CURSOR FOR
        SELECT TABLE_NAME, CONSTRAINT_NAME
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND CONSTRAINT_TYPE = 'FOREIGN KEY';

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN fk_cursor;

    drop_loop: LOOP
        FETCH fk_cursor INTO v_table_name, v_constraint_name;
        IF done = 1 THEN
            LEAVE drop_loop;
        END IF;

        SET @sql = CONCAT(
            'ALTER TABLE `', v_table_name, '` ',
            'DROP FOREIGN KEY `', v_constraint_name, '`'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END LOOP;

    CLOSE fk_cursor;
END$$

CREATE PROCEDURE food_add_fk_if_missing(
    IN p_constraint_name VARCHAR(128),
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_ref_table_name VARCHAR(128),
    IN p_ref_column_name VARCHAR(128)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    )
    AND EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_ref_table_name
          AND COLUMN_NAME = p_ref_column_name
    )
    AND NOT EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND CONSTRAINT_NAME = p_constraint_name
    ) THEN
        SET @sql = CONCAT(
            'ALTER TABLE `', p_table_name, '` ',
            'ADD CONSTRAINT `', p_constraint_name, '` ',
            'FOREIGN KEY (`', p_column_name, '`) ',
            'REFERENCES `', p_ref_table_name, '` (`', p_ref_column_name, '`)'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE food_migrate_id_columns_to_bigint()
BEGIN
    CALL food_drop_all_foreign_keys();

    -- Primary key / identity columns.
    CALL food_alter_column_if_exists('accounts', 'account_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('carts', 'cart_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('cart_items', 'cart_item_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('categories', 'category_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('flash_sales', 'flash_sale_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('menu_sizes', 'menu_size_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('menus', 'menu_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('order_details', 'order_detail_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('orders', 'order_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('realtime_chat_messages', 'chat_message_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('realtime_chat_sessions', 'chat_session_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('refresh_tokens', 'id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('reviews', 'review_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('toppings', 'topping_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('user_vouchers', 'id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('vouchers', 'voucher_id', 'BIGINT NOT NULL AUTO_INCREMENT');
    CALL food_alter_column_if_exists('wards', 'ward_id', 'BIGINT NOT NULL AUTO_INCREMENT');

    -- Foreign key columns.
    CALL food_alter_column_if_exists('carts', 'account_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('cart_items', 'cart_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('cart_items', 'menu_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('cart_items', 'menu_size_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('menus', 'category_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('menu_sizes', 'menu_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('orders', 'account_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('orders', 'ward_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('order_details', 'order_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('order_details', 'menu_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('order_details', 'menu_size_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('realtime_chat_messages', 'chat_session_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('realtime_chat_messages', 'sender_account_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('realtime_chat_sessions', 'account_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('refresh_tokens', 'account_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('reviews', 'account_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('reviews', 'menu_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('reviews', 'order_detail_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('reviews', 'order_id', 'BIGINT NULL');
    CALL food_alter_column_if_exists('user_vouchers', 'account_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('user_vouchers', 'voucher_id', 'BIGINT NOT NULL');

    -- Join table columns.
    CALL food_alter_column_if_exists('cart_item_toppings', 'cart_item_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('cart_item_toppings', 'topping_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('flash_sale_items', 'flash_sale_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('flash_sale_items', 'menu_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('menu_toppings', 'menu_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('menu_toppings', 'topping_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('order_detail_toppings', 'order_detail_id', 'BIGINT NOT NULL');
    CALL food_alter_column_if_exists('order_detail_toppings', 'topping_id', 'BIGINT NOT NULL');

    -- Recreate application foreign keys with stable names.
    CALL food_add_fk_if_missing('fk_carts_account', 'carts', 'account_id', 'accounts', 'account_id');
    CALL food_add_fk_if_missing('fk_cart_items_cart', 'cart_items', 'cart_id', 'carts', 'cart_id');
    CALL food_add_fk_if_missing('fk_cart_items_menu', 'cart_items', 'menu_id', 'menus', 'menu_id');
    CALL food_add_fk_if_missing('fk_cart_items_menu_size', 'cart_items', 'menu_size_id', 'menu_sizes', 'menu_size_id');
    CALL food_add_fk_if_missing('fk_cart_item_toppings_cart_item', 'cart_item_toppings', 'cart_item_id', 'cart_items', 'cart_item_id');
    CALL food_add_fk_if_missing('fk_cart_item_toppings_topping', 'cart_item_toppings', 'topping_id', 'toppings', 'topping_id');
    CALL food_add_fk_if_missing('fk_flash_sale_items_flash_sale', 'flash_sale_items', 'flash_sale_id', 'flash_sales', 'flash_sale_id');
    CALL food_add_fk_if_missing('fk_flash_sale_items_menu', 'flash_sale_items', 'menu_id', 'menus', 'menu_id');
    CALL food_add_fk_if_missing('fk_menu_sizes_menu', 'menu_sizes', 'menu_id', 'menus', 'menu_id');
    CALL food_add_fk_if_missing('fk_menus_category', 'menus', 'category_id', 'categories', 'category_id');
    CALL food_add_fk_if_missing('fk_menu_toppings_menu', 'menu_toppings', 'menu_id', 'menus', 'menu_id');
    CALL food_add_fk_if_missing('fk_menu_toppings_topping', 'menu_toppings', 'topping_id', 'toppings', 'topping_id');
    CALL food_add_fk_if_missing('fk_orders_account', 'orders', 'account_id', 'accounts', 'account_id');
    CALL food_add_fk_if_missing('fk_orders_ward', 'orders', 'ward_id', 'wards', 'ward_id');
    CALL food_add_fk_if_missing('fk_order_details_order', 'order_details', 'order_id', 'orders', 'order_id');
    CALL food_add_fk_if_missing('fk_order_details_menu', 'order_details', 'menu_id', 'menus', 'menu_id');
    CALL food_add_fk_if_missing('fk_order_details_menu_size', 'order_details', 'menu_size_id', 'menu_sizes', 'menu_size_id');
    CALL food_add_fk_if_missing('fk_order_detail_toppings_order_detail', 'order_detail_toppings', 'order_detail_id', 'order_details', 'order_detail_id');
    CALL food_add_fk_if_missing('fk_order_detail_toppings_topping', 'order_detail_toppings', 'topping_id', 'toppings', 'topping_id');
    CALL food_add_fk_if_missing('fk_realtime_chat_messages_session', 'realtime_chat_messages', 'chat_session_id', 'realtime_chat_sessions', 'chat_session_id');
    CALL food_add_fk_if_missing('fk_realtime_chat_messages_sender', 'realtime_chat_messages', 'sender_account_id', 'accounts', 'account_id');
    CALL food_add_fk_if_missing('fk_realtime_chat_sessions_account', 'realtime_chat_sessions', 'account_id', 'accounts', 'account_id');
    CALL food_add_fk_if_missing('fk_refresh_tokens_account', 'refresh_tokens', 'account_id', 'accounts', 'account_id');
    CALL food_add_fk_if_missing('fk_reviews_account', 'reviews', 'account_id', 'accounts', 'account_id');
    CALL food_add_fk_if_missing('fk_reviews_menu', 'reviews', 'menu_id', 'menus', 'menu_id');
    CALL food_add_fk_if_missing('fk_reviews_order_detail', 'reviews', 'order_detail_id', 'order_details', 'order_detail_id');
    CALL food_add_fk_if_missing('fk_reviews_order', 'reviews', 'order_id', 'orders', 'order_id');
    CALL food_add_fk_if_missing('fk_user_vouchers_account', 'user_vouchers', 'account_id', 'accounts', 'account_id');
    CALL food_add_fk_if_missing('fk_user_vouchers_voucher', 'user_vouchers', 'voucher_id', 'vouchers', 'voucher_id');
END$$

DELIMITER ;

CALL food_migrate_id_columns_to_bigint();

DROP PROCEDURE IF EXISTS food_migrate_id_columns_to_bigint;
DROP PROCEDURE IF EXISTS food_drop_all_foreign_keys;
DROP PROCEDURE IF EXISTS food_alter_column_if_exists;
DROP PROCEDURE IF EXISTS food_add_fk_if_missing;

SELECT 'ID/FK columns migrated to BIGINT. Restart the backend and check Hibernate DDL warnings.' AS result;
