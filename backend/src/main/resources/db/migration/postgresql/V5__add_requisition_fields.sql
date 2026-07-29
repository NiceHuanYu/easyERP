-- V5__add_requisition_fields.sql (PostgreSQL) — 采购申请增加申请人、申请日期
ALTER TABLE t_pur_requisition ADD COLUMN applicant_id BIGINT DEFAULT NULL;
ALTER TABLE t_pur_requisition ADD COLUMN req_date     DATE   DEFAULT NULL;

COMMENT ON COLUMN t_pur_requisition.applicant_id IS '申请人（员工ID）';
COMMENT ON COLUMN t_pur_requisition.req_date IS '申请日期';
