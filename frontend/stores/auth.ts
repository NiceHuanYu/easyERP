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

/** Backend raw response from GET /api/v1/auth/user-info */
interface BackendUserInfo {
  userId: number
  username: string
  nickname: string
  permissions: string[]
  menus: MenuItem[]
}

export const useAuthStore = defineStore('auth', () => {
  // ---- State (useCookie for SSR-safe token persistence) ----
  const token = useCookie<string>('token')
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
      // Step 1: login → get token (backend returns ApiResponse<string>)
      const res = await $fetch<{ code: number; data: string }>('/api/v1/auth/login', {
        method: 'POST',
        body: credentials,
      })

      token.value = res.data
      console.log('[auth] login token:', res.data?.substring(0, 50) + '...')

      // Step 2: fetch user info with the token
      await fetchUserInfo()
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

    navigateTo('/login')
  }

  async function fetchUserInfo(): Promise<void> {
    try {
      const res = await $fetch<{ code: number; data: BackendUserInfo }>('/api/v1/auth/user-info', {
        headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
      })

      console.log('[auth] user-info raw response:', JSON.stringify(res))

      const d = res.data
      if (!d) {
        throw new Error('后端返回空用户信息，请确认 token 有效')
      }
      // Map backend flat fields → frontend UserInfo
      userInfo.value = {
        id: d.userId,
        name: d.nickname || d.username,
        avatar: '',
        roles: [],
      }
      permissions.value = d.permissions
      menus.value = d.menus
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
