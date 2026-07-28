package top.huanyu666.backend.modules.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.common.utils.CodeGenerator;
import top.huanyu666.backend.modules.base.entity.BomDetail;
import top.huanyu666.backend.modules.base.entity.BomHeader;
import top.huanyu666.backend.modules.base.mapper.BomDetailMapper;
import top.huanyu666.backend.modules.base.mapper.BomHeaderMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/base/boms", "/api/v1/base-data/boms"})
@RequiredArgsConstructor
public class BomController {

    private final BomHeaderMapper bomHeaderMapper;
    private final BomDetailMapper bomDetailMapper;

    @SaCheckPermission("base-data:bom:view")
    @GetMapping
    public ApiResponse<PageResult<BomHeader>> list(PageParam param,
                                                    @RequestParam(required = false) Long productMaterialId,
                                                    @RequestParam(required = false) String bomNo) {
        LambdaQueryWrapper<BomHeader> qw = new LambdaQueryWrapper<>();
        if (productMaterialId != null) qw.eq(BomHeader::getProductMaterialId, productMaterialId);
        if (bomNo != null && !bomNo.isBlank()) qw.like(BomHeader::getBomNo, bomNo);
        qw.orderByDesc(BomHeader::getCreateTime);
        Page<BomHeader> page = bomHeaderMapper.selectPage(new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base-data:bom:view")
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        BomHeader header = bomHeaderMapper.selectById(id);
        if (header == null) throw new BusinessException("BOM不存在");
        List<BomDetail> details = bomDetailMapper.selectList(
                new LambdaQueryWrapper<BomDetail>().eq(BomDetail::getBomId, id));
        return ApiResponse.ok(Map.of("header", header, "details", details));
    }

    @SaCheckPermission("base-data:bom:create")
    @PostMapping
    @Transactional
    public ApiResponse<BomHeader> create(@RequestBody Map<String, Object> body) {
        BomHeader header = mapToHeader(body);
        header.setBomNo(CodeGenerator.generate("BOM", () -> {
            BomHeader last = bomHeaderMapper.selectOne(
                    new LambdaQueryWrapper<BomHeader>().select(BomHeader::getBomNo)
                            .orderByDesc(BomHeader::getBomNo).last("LIMIT 1"));
            return last != null ? last.getBomNo() : null;
        }));
        bomHeaderMapper.insert(header);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detailList = (List<Map<String, Object>>) body.get("details");
        if (detailList != null) {
            for (Map<String, Object> d : detailList) {
                BomDetail detail = new BomDetail();
                detail.setBomId(header.getId());
                detail.setMaterialId(Long.valueOf(d.get("materialId").toString()));
                detail.setQuantity(new java.math.BigDecimal(d.get("quantity").toString()));
                detail.setUnit((String) d.getOrDefault("unit", null));
                detail.setRemark((String) d.getOrDefault("remark", null));
                bomDetailMapper.insert(detail);
            }
        }
        return ApiResponse.ok(header);
    }

    @SaCheckPermission("base-data:bom:edit")
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BomHeader header = mapToHeader(body);
        header.setId(id);
        bomHeaderMapper.updateById(header);

        bomDetailMapper.delete(new LambdaQueryWrapper<BomDetail>().eq(BomDetail::getBomId, id));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detailList = (List<Map<String, Object>>) body.get("details");
        if (detailList != null) {
            for (Map<String, Object> d : detailList) {
                BomDetail detail = new BomDetail();
                detail.setBomId(id);
                detail.setMaterialId(Long.valueOf(d.get("materialId").toString()));
                detail.setQuantity(new java.math.BigDecimal(d.get("quantity").toString()));
                detail.setUnit((String) d.getOrDefault("unit", null));
                detail.setRemark((String) d.getOrDefault("remark", null));
                bomDetailMapper.insert(detail);
            }
        }
        return ApiResponse.ok();
    }

    @SaCheckPermission("base-data:bom:delete")
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bomDetailMapper.delete(new LambdaQueryWrapper<BomDetail>().eq(BomDetail::getBomId, id));
        bomHeaderMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private BomHeader mapToHeader(Map<String, Object> body) {
        BomHeader h = new BomHeader();
        if (body.containsKey("productMaterialId") && body.get("productMaterialId") != null)
            h.setProductMaterialId(Long.valueOf(body.get("productMaterialId").toString()));
        if (body.containsKey("version")) h.setVersion((String) body.get("version"));
        if (body.containsKey("status")) h.setStatus(Integer.valueOf(body.get("status").toString()));
        return h;
    }
}
