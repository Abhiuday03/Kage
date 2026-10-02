/**
 * WHAT IT DOES:
 * Renders a dropdown to switch between available workspaces.
 * Shows the current workspace and allows users to select a different one.
 *
 * WHY IT EXISTS:
 * Users with multiple workspaces need a quick way to switch between them.
 * This is typically placed in the header or sidebar.
 *
 * HOW IT CONNECTS:
 * Uses useWorkspace() to get available workspaces and current selection.
 * Calls setCurrentWorkspace() to switch workspaces.
 */

import { useState, useRef, useEffect } from 'react'
import { ChevronDown } from 'lucide-react'
import { useWorkspace } from './WorkspaceContext'

export default function WorkspaceSwitcher() {
  const { workspaces, currentWorkspace, setCurrentWorkspace } = useWorkspace()
  const [isOpen, setIsOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside)
      return () => document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [isOpen])

  if (workspaces.length === 0) {
    return <div className="text-sm text-gray-500">No workspaces</div>
  }

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-100 transition text-sm font-medium text-gray-700"
      >
        <span className="truncate max-w-xs">{currentWorkspace?.name || 'Select workspace'}</span>
        <ChevronDown className={`w-4 h-4 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
      </button>

      {isOpen && (
        <div className="absolute top-full left-0 mt-1 w-56 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
          <div className="max-h-60 overflow-y-auto">
            {workspaces.map((workspace) => (
              <button
                key={workspace.id}
                onClick={() => {
                  setCurrentWorkspace(workspace)
                  setIsOpen(false)
                }}
                className={`w-full text-left px-4 py-2 text-sm transition ${
                  workspace.id === currentWorkspace?.id
                    ? 'bg-indigo-50 text-indigo-700 font-medium'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <div className="font-medium">{workspace.name}</div>
                <div className="text-xs text-gray-500">{workspace.description || 'No description'}</div>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
