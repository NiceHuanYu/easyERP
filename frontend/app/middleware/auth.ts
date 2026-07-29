import { useAuthStore } from '../../stores/auth'

export default defineNuxtRouteMiddleware(async (to) => {
  // Allow access to login page; redirect logged-in users to home
  if (to.path === '/login') {
    if (authStore.isLoggedIn) return navigateTo('/')
    return
  }

  const authStore = useAuthStore()

  if (!authStore.isLoggedIn) {
    return navigateTo({
      path: '/login',
      query: { redirect: to.fullPath },
    })
  }

  // Fetch user info if not already loaded (check by name being empty)
  if (!authStore.userInfo.name) {
    try {
      await authStore.fetchUserInfo()
    } catch {
      // If fetching fails (e.g. token expired), redirect to login
      authStore.logout()
      return navigateTo({
        path: '/login',
        query: { redirect: to.fullPath },
      })
    }
  }
})
