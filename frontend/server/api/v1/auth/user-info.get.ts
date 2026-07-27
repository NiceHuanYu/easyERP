import { defineEventHandler } from 'h3'

export default defineEventHandler(() => {
  return {
    code: 200,
    message: 'success',
    data: {
      userInfo: {
        id: 1,
        name: '管理员',
        avatar: '',
        roles: ['admin'],
      },
      permissions: ['*'],
      menus: [
        {
          id: 1,
          name: 'Dashboard',
          path: '/dashboard',
          icon: 'dashboard',
          children: [],
        },
        {
          id: 2,
          name: '系统管理',
          path: '/system',
          icon: 'setting',
          children: [
            { id: 21, name: '用户管理', path: '/system/user', icon: '' },
            { id: 22, name: '角色管理', path: '/system/role', icon: '' },
            { id: 23, name: '字典管理', path: '/system/dict', icon: '' },
          ],
        },
        {
          id: 3,
          name: '商品管理',
          path: '/product',
          icon: 'goods',
          children: [
            { id: 31, name: '商品列表', path: '/product/list', icon: '' },
            { id: 32, name: '商品分类', path: '/product/category', icon: '' },
          ],
        },
        {
          id: 4,
          name: '库存管理',
          path: '/warehouse',
          icon: 'box',
          children: [
            { id: 41, name: '仓库管理', path: '/warehouse/list', icon: '' },
            { id: 42, name: '库存查询', path: '/warehouse/stock', icon: '' },
          ],
        },
        {
          id: 5,
          name: '订单管理',
          path: '/order',
          icon: 'order',
          children: [
            { id: 51, name: '订单列表', path: '/order/list', icon: '' },
          ],
        },
        {
          id: 6,
          name: '客户管理',
          path: '/customer',
          icon: 'user',
          children: [
            { id: 61, name: '客户列表', path: '/customer/list', icon: '' },
          ],
        },
      ],
    },
  }
})
