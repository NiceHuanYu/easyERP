-- V6__add_pur_order_fields.sql (PostgreSQL) — 采购订单增加采购申请、交货日期
ALTER TABLE t_pur_order ADD COLUMN requisition_id BIGINT DEFAULT NULL;
ALTER TABLE t_pur_order ADD COLUMN delivery_date  DATE   DEFAULT NULL;
COMMENT ON COLUMN t_pur_order.requisition_id IS '采购申请ID';
COMMENT ON COLUMN t_pur_order.delivery_date IS '交货日期';
