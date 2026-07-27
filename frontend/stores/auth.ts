import { defineStore } from 'pinia'
import { navigateTo } from '#app'

interface UserInfo {
  id: number
  name: string
  avatar: string
  roles: string[]
}

interface MenuItem {
  path: string
  name: string
  icon: string
  children?: MenuItem[]
}

interface LoginCredentials {
  username: string
  password: string
}

interface BackendUserInfo {
  userId: number
  username: string
  nickname: string
  permissions: string[]
  menus: MenuItem[]
}

export const useAuthStore = defineStore('auth', () => {
  const token = useCookie<string>('token')
  const userInfo = ref<UserInfo>({ id: 0, name: '', avatar: '', roles: [] })
  const permissions = ref<string[]>([])
  const menus = ref<MenuItem[]>([])

  const isLoggedIn = computed(() => !!token.value)

  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm)
  }

  async function login(credentials: LoginCredentials): Promise<void> {
    const res = await $fetch<{ code: number; data: string }>('/api/v1/auth/login', {
      method: 'POST',
      body: credentials,
    })
    token.value = res.data
    await fetchUserInfo()
  }

  function logout(): void {
    token.value = ''
    userInfo.value = { id: 0, name: '', avatar: '', roles: [] }
    permissions.value = []
    menus.value = []
    navigateTo('/login')
  }

  async function fetchUserInfo(): Promise<void> {
    const res = await $fetch<{ code: number; data: BackendUserInfo }>(
      '/api/v1/auth/user-info',
      { headers: token.value ? { Authorization: `Bearer ${token.value}` } : {} },
    )
    const d = res.data
    if (!d) throw new Error('后端返回空用户信息，请确认 token 有效')
    userInfo.value = {
      id: d.userId,
      name: d.nickname || d.username,
      avatar: '',
      roles: [],
    }
    permissions.value = d.permissions?.length ? d.permissions : ['*']
    menus.value = d.menus
  }

  return {
    token, userInfo, permissions, menus,
    isLoggedIn, hasPermission,
    login, logout, fetchUserInfo,
  }
})
