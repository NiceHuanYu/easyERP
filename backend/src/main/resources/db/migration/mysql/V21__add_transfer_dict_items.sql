-- 补充调拨出/入库的字典项（用 309/310，因为 307=盘点调整, 308=报废）
INSERT INTO t_sys_dict_item (id, dict_id, label, value, sort, status, create_time, update_time) VALUES
(309, 3, '调拨出库', 'TRANSFER_OUT', 9, 1, NOW(), NOW()),
(310, 3, '调拨入库', 'TRANSFER_IN', 10, 1, NOW(), NOW());
