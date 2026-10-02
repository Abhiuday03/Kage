/**
 * WHAT IT DOES:
 * Manages the currently selected workspace and provides it to the entire app.
 * Fetches available workspaces on mount and persists selection to localStorage.
 *
 * WHY IT EXISTS:
 * Multi-tenant systems need to track which workspace the user is working in.
 * All API calls must include the workspace ID in the X-Workspace-ID header.
 * This context centralizes that logic and provides hooks to access it.
 *
 * HOW IT CONNECTS:
 * Wraps the app in App.tsx → provides useWorkspace() hook to all components
 */

import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { useQuery } from '@tanstack/react-query'
import { workspaceApi, Workspace } from '../../services/api'

interface WorkspaceContextType {
  workspaces: Workspace[]
  currentWorkspace: Workspace | null
  setCurrentWorkspace: (workspace: Workspace) => void
  isLoadingWorkspaces: boolean
  workspacesError: Error | null
}

const WorkspaceContext = createContext<WorkspaceContextType | undefined>(undefined)

export function WorkspaceProvider({ children }: { children: ReactNode }) {
  const [currentWorkspace, setCurrentWorkspaceState] = useState<Workspace | null>(null)

  // Fetch workspaces
  const { data: workspacesResponse, isLoading: isLoadingWorkspaces, error: workspacesError } = useQuery({
    queryKey: ['workspaces'],
    queryFn: () => workspaceApi.getAll(),
  })

  const workspaces = workspacesResponse?.data.data || []

  // Initialize workspace selection from localStorage or auto-select first
  useEffect(() => {
    if (workspaces.length === 0) return

    const savedWorkspaceId = localStorage.getItem('current_workspace_id')

    // Try to restore from localStorage
    if (savedWorkspaceId) {
      const saved = workspaces.find((ws) => ws.id === savedWorkspaceId)
      if (saved) {
        setCurrentWorkspaceState(saved)
        return
      }
    }

    // Fall back to first workspace
    const firstWorkspace = workspaces[0]
    setCurrentWorkspaceState(firstWorkspace)
    localStorage.setItem('current_workspace_id', firstWorkspace.id)
  }, [workspaces])

  const setCurrentWorkspace = (workspace: Workspace) => {
    setCurrentWorkspaceState(workspace)
    localStorage.setItem('current_workspace_id', workspace.id)
  }

  return (
    <WorkspaceContext.Provider
      value={{
        workspaces,
        currentWorkspace,
        setCurrentWorkspace,
        isLoadingWorkspaces,
        workspacesError: workspacesError as Error | null,
      }}
    >
      {children}
    </WorkspaceContext.Provider>
  )
}

export function useWorkspace() {
  const context = useContext(WorkspaceContext)
  if (!context) {
    throw new Error('useWorkspace must be used within WorkspaceProvider')
  }
  return context
}
