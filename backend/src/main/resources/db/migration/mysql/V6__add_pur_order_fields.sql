-- V6__add_pur_order_fields.sql — 采购订单增加采购申请、交货日期
ALTER TABLE t_pur_order ADD COLUMN requisition_id BIGINT DEFAULT NULL COMMENT '采购申请ID';
ALTER TABLE t_pur_order ADD COLUMN delivery_date  DATE   DEFAULT NULL COMMENT '交货日期';
