-- V8__add_payment_reconciled_amount.sql — 收付款单增加已核销金额
ALTER TABLE t_fin_payment ADD COLUMN reconciled_amount DECIMAL(18,2) DEFAULT 0;
COMMENT ON COLUMN t_fin_payment.reconciled_amount IS '已核销金额';
