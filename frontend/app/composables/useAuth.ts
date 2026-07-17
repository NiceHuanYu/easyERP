// 硬编码账户
const ACCOUNTS = [
  { username: 'admin', password: 'admin123', name: '管理员', role: 'admin' },
  { username: 'user', password: 'user123', name: '张三', role: 'user' },
]

export const useAuth = () => {
  const user = useState<{ username: string; name: string; role: string } | null>('auth-user', () => null)

  const isLoggedIn = computed(() => user.value !== null)

  // 初始化：从 localStorage 恢复登录状态
  if (import.meta.client) {
    const saved = localStorage.getItem('easyerp-user')
    if (saved) {
      try {
        const parsed = JSON.parse(saved)
        const match = ACCOUNTS.find(a => a.username === parsed.username)
        if (match) {
          user.value = match
        }
      } catch {
        localStorage.removeItem('easyerp-user')
      }
    }
  }

  function login(username: string, password: string): { ok: boolean; message: string } {
    const account = ACCOUNTS.find(a => a.username === username && a.password === password)
    if (account) {
      user.value = { username: account.username, name: account.name, role: account.role }
      if (import.meta.client) {
        localStorage.setItem('easyerp-user', JSON.stringify(user.value))
      }
      return { ok: true, message: '登录成功' }
    }
    return { ok: false, message: '用户名或密码错误' }
  }

  function logout() {
    user.value = null
    if (import.meta.client) {
      localStorage.removeItem('easyerp-user')
    }
    navigateTo('/login')
  }

  return { user, isLoggedIn, login, logout }
}
