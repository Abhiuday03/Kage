import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { AuthResponse } from '../../services/api'

interface AuthContextType {
  user: AuthResponse['user'] | null
  token: string | null
  login: (authData: AuthResponse) => void
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthResponse['user'] | null>(null)
  const [token, setToken] = useState<string | null>(null)

  useEffect(() => {
    // Restore session from localStorage on mount
    const savedToken = localStorage.getItem('auth_token')
    const savedUser = localStorage.getItem('user')

    if (savedToken && savedUser) {
      setToken(savedToken)
      setUser(JSON.parse(savedUser))
    }
  }, [])

  const login = (authData: AuthResponse) => {
    setToken(authData.token)
    setUser(authData.user)
    localStorage.setItem('auth_token', authData.token)
    localStorage.setItem('user', JSON.stringify(authData.user))
  }

  const logout = () => {
    setToken(null)
    setUser(null)
    localStorage.removeItem('auth_token')
    localStorage.removeItem('user')
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        login,
        logout,
        isAuthenticated: !!token,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
