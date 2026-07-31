ALTER TABLE t_sales_order ADD COLUMN customer_po_no VARCHAR(100) DEFAULT NULL COMMENT '客户采购订单号' AFTER delivery_date;
