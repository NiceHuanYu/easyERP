-- V7__add_fin_due_date.sql — 应收/应付增加到期日字段
ALTER TABLE t_fin_payable ADD COLUMN due_date DATE DEFAULT NULL;
ALTER TABLE t_fin_receivable ADD COLUMN due_date DATE DEFAULT NULL;
COMMENT ON COLUMN t_fin_payable.due_date IS '应付日期';
COMMENT ON COLUMN t_fin_receivable.due_date IS '应收日期';
