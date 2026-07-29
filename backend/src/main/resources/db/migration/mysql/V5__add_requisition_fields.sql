-- V5__add_requisition_fields.sql — 采购申请增加申请人、申请日期
ALTER TABLE t_pur_requisition ADD COLUMN applicant_id BIGINT DEFAULT NULL COMMENT '申请人（员工ID）';
ALTER TABLE t_pur_requisition ADD COLUMN req_date     DATE   DEFAULT NULL COMMENT '申请日期';
