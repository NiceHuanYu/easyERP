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

interface LoginResponse {
  token: string
  userInfo: UserInfo
  permissions: string[]
  menus: MenuItem[]
}

export const useAuthStore = defineStore('auth', () => {
  // ---- State ----
  const token = ref<string>(import.meta.client ? localStorage.getItem('token') || '' : '')
  const userInfo = ref<UserInfo>({
    id: 0,
    name: '',
    avatar: '',
    roles: [],
  })
  const permissions = ref<string[]>([])
  const menus = ref<MenuItem[]>([])

  // ---- Getters ----
  const isLoggedIn = computed(() => !!token.value)

  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm)
  }

  // ---- Actions ----
  async function login(credentials: LoginCredentials): Promise<void> {
    try {
      const res = await $fetch<{ code: number; data: LoginResponse }>('/api/v1/auth/login', {
        method: 'POST',
        body: credentials,
      })

      const { token: t, userInfo: u, permissions: p, menus: m } = res.data
      token.value = t
      userInfo.value = u
      permissions.value = p
      menus.value = m

      if (import.meta.client) {
        localStorage.setItem('token', t)
      }
    } catch (error: any) {
      console.error('Login failed:', error?.message ?? error)
      throw error
    }
  }

  function logout(): void {
    token.value = ''
    userInfo.value = { id: 0, name: '', avatar: '', roles: [] }
    permissions.value = []
    menus.value = []

    if (import.meta.client) {
      localStorage.removeItem('token')
      navigateTo('/login')
    }
  }

  async function fetchUserInfo(): Promise<void> {
    try {
      const res = await $fetch<{
        code: number
        data: {
          userInfo: UserInfo
          permissions: string[]
          menus: MenuItem[]
        }
      }>('/api/v1/auth/user-info')

      userInfo.value = res.data.userInfo
      permissions.value = res.data.permissions
      menus.value = res.data.menus
    } catch (error: any) {
      console.error('Fetch user info failed:', error?.message ?? error)
      throw error
    }
  }

  return {
    // state
    token,
    userInfo,
    permissions,
    menus,
    // getters
    isLoggedIn,
    hasPermission,
    // actions
    login,
    logout,
    fetchUserInfo,
  }
})
