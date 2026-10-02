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

// Content Ideas API
export type IdeaStatus =
  | 'DISCOVERED'
  | 'SCORED'
  | 'SELECTED'
  | 'BRIEFED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'REJECTED'
  | 'ARCHIVED'

export interface ContentIdea {
  id: string
  workspaceId: string
  nicheId: string | null
  title: string
  description: string
  sourceUrl: string | null
  trendScore: number
  relevanceScore: number
  engagementScore: number
  totalScore: number
  status: IdeaStatus
  discoveredAt: string
  createdAt: string
  updatedAt: string
}

export interface IdeasPageResponse {
  content: ContentIdea[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
}

export interface TriggerDiscoveryRequest {
  nicheId: string
  count?: number
}

export interface TriggerDiscoveryResponse {
  status: string
  workspaceId: string
}

export interface ScoreIdeaResponse {
  ideaId: string
  trendScore: number
  relevanceScore: number
  engagementScore: number
  totalScore: number
  scoringReasoning: string
}

export const ideaApi = {
  // Get all ideas for current workspace (paginated)
  getAll: (page: number = 0, size: number = 20) =>
    apiClient.get<ApiResponse<IdeasPageResponse>>('/ideas', {
      params: { page, size },
    }),

  // Get ideas filtered by status
  getByStatus: (status: IdeaStatus, page: number = 0, size: number = 20) =>
    apiClient.get<ApiResponse<IdeasPageResponse>>(`/ideas/status/${status}`, {
      params: { page, size },
    }),

  // Get a single idea by ID
  getById: (ideaId: string) =>
    apiClient.get<ApiResponse<ContentIdea>>(`/ideas/${ideaId}`),

  // Trigger AI-powered idea generation (async)
  // Returns 202 Accepted immediately, results arrive via webhook
  triggerDiscovery: (nicheId: string, count?: number) =>
    apiClient.post<ApiResponse<TriggerDiscoveryResponse>>('/discovery/trigger', null, {
      params: { nicheId, count },
    }),

  // Score a single idea using AI Engine
  scoreIdea: (ideaId: string, nicheId: string) =>
    apiClient.post<ApiResponse<ScoreIdeaResponse>>(`/ideas/${ideaId}/score`, null, {
      params: { nicheId },
    }),

  // Update idea status (move between Kanban columns)
  updateStatus: (ideaId: string, status: IdeaStatus) =>
    apiClient.patch<ApiResponse<ContentIdea>>(`/ideas/${ideaId}/status`, null, {
      params: { status },
    }),

  // Delete an idea
  delete: (ideaId: string) =>
    apiClient.delete<ApiResponse<void>>(`/ideas/${ideaId}`),
}

// Workspace header injection
// Add X-Workspace-ID header to all requests if workspace is set
apiClient.interceptors.request.use((config) => {
  const workspaceId = localStorage.getItem('current_workspace_id')
  if (workspaceId) {
    config.headers['X-Workspace-ID'] = workspaceId
  }
  return config
})
