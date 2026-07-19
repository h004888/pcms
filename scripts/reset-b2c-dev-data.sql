-- DEV/DEMO ONLY - DESTRUCTIVE - DO NOT RUN IN PRODUCTION

-- Preflight counts
SELECT 'pcms_user.users CUSTOMER' AS scope, COUNT(*) AS row_count FROM pcms_user.users WHERE role = 'CUSTOMER';
SELECT 'pcms_customer.customers' AS scope, COUNT(*) AS row_count FROM pcms_customer.customers;
SELECT 'pcms_customer_portal.customer_addresses' AS scope, COUNT(*) AS row_count FROM pcms_customer_portal.customer_addresses;
SELECT 'pcms_order.orders' AS scope, COUNT(*) AS row_count FROM pcms_order.orders;
SELECT 'pcms_payment.payments' AS scope, COUNT(*) AS row_count FROM pcms_payment.payments;

-- Remove B2C portal state only. Preserve catalog and content seed data.
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM pcms_customer_portal.customer_addresses;
DELETE FROM pcms_customer_portal.customer_family;
DELETE FROM pcms_customer_portal.customer_favorites;
DELETE FROM pcms_customer_portal.customer_notif_settings;
DELETE FROM pcms_customer_portal.product_reviews;
DELETE FROM pcms_customer_portal.review;
DELETE FROM pcms_customer_portal.vaccination_ledger;
DELETE FROM pcms_customer_portal.vaccine_bookings;
DELETE FROM pcms_customer_portal.voucher_usages;
DELETE FROM pcms_customer_portal.wallet_transactions;
DELETE FROM pcms_customer_portal.cart_items;
DELETE FROM pcms_customer_portal.carts;
SET FOREIGN_KEY_CHECKS = 1;

-- Remove order and payment test state. Preserve coupons and order_sequence.
DELETE FROM pcms_payment.payments;
DELETE FROM pcms_payment.webhook_events;
DELETE FROM pcms_payment.outbox_events;
DELETE FROM pcms_order.order_items;
DELETE FROM pcms_order.orders;
DELETE FROM pcms_order.saga_steps;
DELETE FROM pcms_order.saga_instances;
DELETE FROM pcms_order.outbox_events;
DELETE FROM pcms_order.dead_letter_events;

-- Remove customer profiles, then CUSTOMER auth state only.
DELETE FROM pcms_customer.loyalty_transactions;
DELETE FROM pcms_customer.customers;

DELETE rt FROM pcms_user.refresh_tokens rt
INNER JOIN pcms_user.users u ON u.id = rt.user_id
WHERE u.role = 'CUSTOMER';
DELETE pwt FROM pcms_user.password_reset_tokens pwt
INNER JOIN pcms_user.users u ON u.id = pwt.user_id
WHERE u.role = 'CUSTOMER';
DELETE evt FROM pcms_user.email_verification_tokens evt
INNER JOIN pcms_user.users u ON u.id = evt.user_id
WHERE u.role = 'CUSTOMER';
DELETE bt FROM pcms_user.blacklisted_tokens bt
INNER JOIN pcms_user.users u ON u.id = bt.user_id
WHERE u.role = 'CUSTOMER';
DELETE al FROM pcms_user.audit_logs al
INNER JOIN pcms_user.users u ON u.id = al.user_id
WHERE u.role = 'CUSTOMER';
DELETE FROM pcms_user.users WHERE role = 'CUSTOMER';

-- Post-reset verification
SELECT COUNT(*) AS remaining_customers FROM pcms_customer.customers;
SELECT COUNT(*) AS remaining_customer_addresses FROM pcms_customer_portal.customer_addresses;
SELECT COUNT(*) AS remaining_customer_users FROM pcms_user.users WHERE role = 'CUSTOMER';
SELECT role, COUNT(*) AS user_count FROM pcms_user.users GROUP BY role ORDER BY role;
