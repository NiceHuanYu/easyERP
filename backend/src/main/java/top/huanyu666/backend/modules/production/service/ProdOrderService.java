package top.huanyu666.backend.modules.production.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.huanyu666.backend.common.enums.DocumentStatus;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.modules.base.entity.BomDetail;
import top.huanyu666.backend.modules.base.entity.BomHeader;
import top.huanyu666.backend.modules.base.mapper.BomDetailMapper;
import top.huanyu666.backend.modules.base.mapper.BomHeaderMapper;
import top.huanyu666.backend.modules.production.entity.*;
import top.huanyu666.backend.modules.production.mapper.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产工单服务 —— 封装下达、领料、完工等核心业务逻辑。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProdOrderService {

    private final ProdOrderMapper orderMapper;
    private final ProdOrderBomMapper orderBomMapper;
    private final ProdPickingMapper pickingMapper;
    private final ProdPickingItemMapper pickingItemMapper;
    private final ProdFinishMapper finishMapper;
    private final ProdFinishItemMapper finishItemMapper;
    private final BomHeaderMapper bomHeaderMapper;
    private final BomDetailMapper bomDetailMapper;

    /**
     * 下达工单：DRAFT → RELEASED，从 BOM 生成物料需求。
     */
    @Transactional
    public void release(Long id) {
        ProdOrder order = getOrder(id);
        if (!DocumentStatus.DRAFT.eq(order.getStatus())) {
            throw new BusinessException("仅草稿状态的工单可下达");
        }

        BomHeader bomHeader = bomHeaderMapper.selectOne(
                new LambdaQueryWrapper<BomHeader>()
                        .eq(BomHeader::getProductMaterialId, order.getMaterialId())
                        .eq(BomHeader::getStatus, 1)
                        .orderByDesc(BomHeader::getCreateTime)
                        .last("LIMIT 1"));

        if (bomHeader == null) {
            throw new BusinessException("该物料未配置 BOM，无法下达");
        }

        List<BomDetail> details = bomDetailMapper.selectList(
                new LambdaQueryWrapper<BomDetail>().eq(BomDetail::getBomId, bomHeader.getId()));

        if (details.isEmpty()) {
            throw new BusinessException("BOM 无明细，无法下达");
        }

        for (BomDetail detail : details) {
            ProdOrderBom bom = new ProdOrderBom();
            bom.setOrderId(id);
            bom.setMaterialId(detail.getMaterialId());
            BigDecimal requiredQty = order.getPlanQuantity()
                    .multiply(detail.getQuantity())
                    .setScale(4, RoundingMode.HALF_UP);
            bom.setRequiredQty(requiredQty);
            bom.setPickedQty(BigDecimal.ZERO);
            bom.setCreateTime(LocalDateTime.now());
            bom.setUpdateTime(LocalDateTime.now());
            orderBomMapper.insert(bom);
        }

        order.setStatus(DocumentStatus.RELEASED.getCode());
        orderMapper.updateById(order);
        log.info("工单 {} 已下达，生成 {} 条物料需求", order.getOrderNo(), details.size());
    }

    /**
     * 创建领料单：查未领完的物料需求 → 创建领料单主表 + 明细。
     */
    @Transactional
    public ProdPicking createPicking(Long orderId, ProdPicking picking) {
        ProdOrder order = getOrder(orderId);

        LambdaQueryWrapper<ProdOrderBom> bomQw = new LambdaQueryWrapper<>();
        bomQw.eq(ProdOrderBom::getOrderId, orderId);
        bomQw.apply("required_qty - picked_qty > 0");
        List<ProdOrderBom> bomList = orderBomMapper.selectList(bomQw);

        if (bomList.isEmpty()) {
            throw new BusinessException("该工单没有待领物料");
        }

        picking.setOrderId(orderId);
        picking.setStatus(DocumentStatus.DRAFT.getCode());
        picking.setPickingDate(LocalDate.now());
        pickingMapper.insert(picking);

        for (ProdOrderBom bom : bomList) {
            BigDecimal remaining = bom.getRequiredQty().subtract(bom.getPickedQty());
            ProdPickingItem item = new ProdPickingItem();
            item.setPickingId(picking.getId());
            item.setMaterialId(bom.getMaterialId());
            item.setRequestQty(remaining);
            item.setActualQty(BigDecimal.ZERO);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            pickingItemMapper.insert(item);
        }

        log.info("工单 {} 创建领料单 {}，{} 条明细", order.getOrderNo(), picking.getPickingNo(), bomList.size());
        return picking;
    }

    /**
     * 创建完工入库单。
     */
    @Transactional
    public ProdFinish createFinish(Long orderId, ProdFinish finish) {
        ProdOrder order = getOrder(orderId);

        finish.setOrderId(orderId);
        finish.setStatus(DocumentStatus.DRAFT.getCode());
        finish.setFinishDate(LocalDate.now());
        finishMapper.insert(finish);

        ProdFinishItem item = new ProdFinishItem();
        item.setFinishId(finish.getId());
        item.setMaterialId(order.getMaterialId());
        item.setQuantity(order.getPlanQuantity());
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        finishItemMapper.insert(item);

        log.info("工单 {} 创建完工入库单 {}", order.getOrderNo(), finish.getFinishNo());
        return finish;
    }

    /**
     * 标记完工 → COMPLETED。
     */
    @Transactional
    public void finish(Long id) {
        ProdOrder order = getOrder(id);
        order.setStatus(DocumentStatus.COMPLETED.getCode());
        orderMapper.updateById(order);
        log.info("工单 {} 已完工", order.getOrderNo());
    }

    /**
     * 删除（仅草稿）—— 同时删除物料需求。
     */
    @Transactional
    public void delete(Long id) {
        ProdOrder order = getOrder(id);
        if (!DocumentStatus.DRAFT.eq(order.getStatus())) {
            throw new BusinessException("只有草稿状态可删除");
        }
        orderBomMapper.delete(new LambdaQueryWrapper<ProdOrderBom>().eq(ProdOrderBom::getOrderId, id));
        orderMapper.deleteById(id);
    }

    private ProdOrder getOrder(Long id) {
        ProdOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        return order;
    }

    // ---- read-only queries ----

    public ProdOrder getById(Long id) {
        return getOrder(id);
    }

    public List<ProdPicking> getPickingsByOrderId(Long orderId) {
        return pickingMapper.selectList(
                new LambdaQueryWrapper<ProdPicking>().eq(ProdPicking::getOrderId, orderId));
    }

    public List<ProdFinish> getFinishingsByOrderId(Long orderId) {
        return finishMapper.selectList(
                new LambdaQueryWrapper<ProdFinish>().eq(ProdFinish::getOrderId, orderId));
    }

    public List<ProdOrderBom> getMaterialRequirements(Long orderId) {
        return orderBomMapper.selectList(
                new LambdaQueryWrapper<ProdOrderBom>().eq(ProdOrderBom::getOrderId, orderId));
    }
}
