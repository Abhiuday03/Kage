import { useAuth } from '../auth/AuthContext'
import { useQuery } from '@tanstack/react-query'
import { workspaceApi } from '../../services/api'
import { LogOut, Settings, Users, Lightbulb, FileText, Image } from 'lucide-react'

export default function Dashboard() {
  const { user, logout } = useAuth()

  const { data: workspacesResponse, isLoading } = useQuery({
    queryKey: ['workspaces'],
    queryFn: () => workspaceApi.getAll(),
  })

  const workspaces = workspacesResponse?.data.data || []

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-gray-900">ContentOS</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-700">Welcome, {user?.fullName}</span>
              <button
                onClick={logout}
                className="flex items-center px-3 py-2 text-sm text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition"
              >
                <LogOut className="w-4 h-4 mr-2" />
                Sign Out
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Phase 1 Status Banner */}
        <div className="mb-8 bg-indigo-50 border border-indigo-200 rounded-lg p-6">
          <h2 className="text-lg font-semibold text-indigo-900 mb-2">
            🎯 Phase 1 Complete — ContentOS Foundation
          </h2>
          <p className="text-indigo-800 mb-4">
            Multi-tenant authentication, workspace management, and brand configuration are ready.
          </p>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-white rounded-lg p-4 border border-indigo-200">
              <div className="flex items-center text-green-600 mb-2">
                <Users className="w-5 h-5 mr-2" />
                <span className="font-medium">Authentication ✓</span>
              </div>
              <p className="text-sm text-gray-600">JWT-based secure auth with multi-tenancy</p>
            </div>
            <div className="bg-white rounded-lg p-4 border border-indigo-200">
              <div className="flex items-center text-green-600 mb-2">
                <Settings className="w-5 h-5 mr-2" />
                <span className="font-medium">Workspaces ✓</span>
              </div>
              <p className="text-sm text-gray-600">Organization and workspace management</p>
            </div>
            <div className="bg-white rounded-lg p-4 border border-indigo-200">
              <div className="flex items-center text-blue-600 mb-2">
                <Lightbulb className="w-5 h-5 mr-2" />
                <span className="font-medium">Phase 2 Next</span>
              </div>
              <p className="text-sm text-gray-600">AI topic discovery and research</p>
            </div>
            <div className="bg-white rounded-lg p-4 border border-indigo-200">
              <div className="flex items-center text-blue-600 mb-2">
                <Image className="w-5 h-5 mr-2" />
                <span className="font-medium">Phase 3 Later</span>
              </div>
              <p className="text-sm text-gray-600">Image and video generation pipeline</p>
            </div>
          </div>
        </div>

        {/* Workspaces */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Your Workspaces</h3>

          {isLoading ? (
            <div className="flex justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
            </div>
          ) : workspaces.length > 0 ? (
            <div className="grid gap-4">
              {workspaces.map((workspace) => (
                <div key={workspace.id} className="border rounded-lg p-4 hover:bg-gray-50 transition">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="font-medium text-gray-900">{workspace.name}</h4>
                      <p className="text-sm text-gray-500">{workspace.description || 'No description'}</p>
                    </div>
                    <div className="flex items-center space-x-2">
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        workspace.isActive
                          ? 'bg-green-100 text-green-800'
                          : 'bg-gray-100 text-gray-800'
                      }`}>
                        {workspace.isActive ? 'Active' : 'Inactive'}
                      </span>
                      <button className="text-indigo-600 hover:text-indigo-800 text-sm font-medium">
                        Configure →
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              <FileText className="w-12 h-12 mx-auto mb-4 text-gray-300" />
              <p>No workspaces found. Create your first workspace to get started.</p>
              <button className="mt-4 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg transition">
                Create Workspace
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}