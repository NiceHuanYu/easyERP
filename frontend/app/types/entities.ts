/**
 * easyERP 核心实体类型定义
 *
 * 统一管理跨模块共享的类型，消除组件内分散的类型定义。
 * 所有接口按业务模块组织，便于查找和维护。
 */

// ============================================================
// 通用
// ============================================================

/** 单据状态标签 class 映射键 */
export type StatusClass =
  | 'draft' | 'pending' | 'approved' | 'progress'
  | 'ready' | 'shipped' | 'completed' | 'cancelled'
  | 'confirming' | 'done'

/** 分页参数 */
export interface PaginationParams {
  page: number
  pageSize: number
}

/** 分页响应 */
export interface PaginatedResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

// ============================================================
// 基础资料
// ============================================================

export interface Material {
  code: string
  name: string
  category: string
  spec: string
  unit: string
  stock: number
  safetyStock: number
  price: number
  status: string
  remark: string
}

export interface Product {
  code: string
  name: string
  model: string
  unit: string
  category: string
  status: string
  remark: string
}

export interface BOMItem {
  materialCode: string
  materialName: string
  qty: number
  unit: string
  process: string
  remark: string
}

export interface BOM {
  code: string
  productCode: string
  productName: string
  version: string
  items: BOMItem[]
  status: string
  remark: string
}

export interface Customer {
  code: string
  name: string
  contact: string
  phone: string
  address: string
  status: string
}

export interface Supplier {
  code: string
  name: string
  contact: string
  phone: string
  address: string
  status: string
}

// ============================================================
// 销售
// ============================================================

export interface OrderItem {
  material: string
  qty: number
  unit: string
  amount: number
}

export interface SalesOrder {
  code: string
  customer: string
  items: OrderItem[]
  delivery: string
  status: string
  remark: string
  sc: StatusClass
}

// ============================================================
// 采购
// ============================================================

export interface PurchaseItem {
  material: string
  spec: string
  qty: number
  unit: string
  price: number
  amount: number
  delivery: string
  remark: string
}

export interface PurchaseOrder {
  code: string
  supplier: string
  items: PurchaseItem[]
  delivery: string
  status: string
  remark: string
  sc: StatusClass
}

// ============================================================
// 库存
// ============================================================

export interface InventoryRecord {
  code: string
  type: string
  material: string
  qty: number
  unit: string
  wh: string
  src: string
  date: string
  status: string
  remark: string
  sc: StatusClass
}

// ============================================================
// 生产
// ============================================================

export interface ProductionOrder {
  code: string
  product: string
  qty: number
  unit: string
  workshop: string
  planStart: string
  planEnd: string
  status: string
  remark: string
  sc: StatusClass
}

// ============================================================
// API 通用响应
// ============================================================

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}
