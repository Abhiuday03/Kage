import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const apiClient = axios.create({
  baseURL: `${API_BASE_URL}/api/v1`,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add auth token to all requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Handle 401 responses (expired token)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Type-safe API response wrapper
export interface ApiResponse<T> {
  success: boolean
  data: T
  error: { code: string; message: string } | null
  timestamp: number
}

// Auth API
export interface RegisterRequest {
  fullName: string
  email: string
  password: string
  organizationName: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  user: {
    id: string
    email: string
    fullName: string
  }
  organization: {
    id: string
    name: string
    slug: string
  }
}

export const authApi = {
  register: (data: RegisterRequest) =>
    apiClient.post<ApiResponse<AuthResponse>>('/auth/register', data),

  login: (data: LoginRequest) =>
    apiClient.post<ApiResponse<AuthResponse>>('/auth/login', data),
}

// Workspace API
export interface Workspace {
  id: string
  name: string
  slug: string
  description: string | null
  isActive: boolean
  createdAt: number
}

export interface CreateWorkspaceRequest {
  name: string
  description?: string
}

export const workspaceApi = {
  getAll: () =>
    apiClient.get<ApiResponse<Workspace[]>>('/workspaces'),

  getById: (id: string) =>
    apiClient.get<ApiResponse<Workspace>>(`/workspaces/${id}`),

  create: (data: CreateWorkspaceRequest) =>
    apiClient.post<ApiResponse<Workspace>>('/workspaces', data),
}

// Brand API
export interface BrandProfile {
  id: string
  workspaceId: string
  brandName: string | null
  industry: string | null
  targetAudience: string | null
  toneOfVoice: string | null
  writingStyle: string | null
  primaryColor: string | null
  secondaryColor: string | null
  fontPrimary: string | null
  fontSecondary: string | null
  logoUrl: string | null
  brandGuidelines: string | null
  wordsToAvoid: string[] | null
  preferredPhrases: string[] | null
  ctaPreferences: string | null
}

export const brandApi = {
  get: (workspaceId: string) =>
    apiClient.get<ApiResponse<BrandProfile>>(`/workspaces/${workspaceId}/brand`),

  update: (workspaceId: string, data: Partial<BrandProfile>) =>
    apiClient.put<ApiResponse<BrandProfile>>(`/workspaces/${workspaceId}/brand`, data),
}

// Niche API
export interface Niche {
  id: string
  workspaceId: string
  name: string
  description: string | null
  keywords: string[] | null
  targetAudience: string | null
  isActive: boolean
}

export interface CreateNicheRequest {
  name: string
  description?: string
  keywords?: string[]
  targetAudience?: string
}

export const nicheApi = {
  getAll: (workspaceId: string) =>
    apiClient.get<ApiResponse<Niche[]>>(`/workspaces/${workspaceId}/niches`),

  create: (workspaceId: string, data: CreateNicheRequest) =>
    apiClient.post<ApiResponse<Niche>>(`/workspaces/${workspaceId}/niches`, data),

  update: (workspaceId: string, nicheId: string, data: Partial<CreateNicheRequest>) =>
    apiClient.put<ApiResponse<Niche>>(`/workspaces/${workspaceId}/niches/${nicheId}`, data),

  delete: (workspaceId: string, nicheId: string) =>
    apiClient.delete<ApiResponse<void>>(`/workspaces/${workspaceId}/niches/${nicheId}`),
}
