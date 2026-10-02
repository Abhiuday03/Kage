/**
 * WHAT IT DOES:
 * Dashboard home page showing overview stats and quick actions.
 * Displays idea counts by status and recently discovered ideas.
 *
 * WHY IT EXISTS:
 * Users need a high-level view of their content pipeline status.
 * This page provides quick stats and entry points to key actions.
 *
 * HOW IT CONNECTS:
 * Rendered by Dashboard.tsx when user visits /dashboard.
 * Uses ideaApi to fetch statistics and recent ideas.
 */

import { useQuery } from '@tanstack/react-query'
import { Lightbulb, TrendingUp, Target, Plus } from 'lucide-react'
import { Link } from 'react-router-dom'
import { ideaApi } from '../../services/api'
import { useWorkspace } from '../workspace/useWorkspace'

interface StatCardProps {
  title: string
  value: number | string
  icon: React.ComponentType<{ className?: string }>
  color: string
  isLoading?: boolean
}

function StatCard({ title, value, icon: Icon, color, isLoading = false }: StatCardProps) {
  return (
    <div className="bg-white rounded-lg p-6 border border-gray-200">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-gray-600 font-medium">{title}</p>
          {isLoading ? (
            <div className="h-8 w-16 bg-gray-200 rounded animate-pulse mt-2" />
          ) : (
            <p className="text-3xl font-bold text-gray-900 mt-2">{value}</p>
          )}
        </div>
        <div className={`p-3 rounded-lg ${color}`}>
          <Icon className="w-6 h-6" />
        </div>
      </div>
    </div>
  )
}

function RecentIdeasSkeleton() {
  return (
    <div className="space-y-3">
      {[1, 2, 3].map((i) => (
        <div key={i} className="p-4 border rounded-lg">
          <div className="h-4 bg-gray-200 rounded animate-pulse mb-2" />
          <div className="h-3 bg-gray-200 rounded animate-pulse w-3/4" />
        </div>
      ))}
    </div>
  )
}

export default function DashboardHome() {
  const { currentWorkspace } = useWorkspace()

  // Fetch ideas by status for statistics
  const { data: discoveredIdeas, isLoading: isLoadingDiscovered } = useQuery({
    queryKey: ['ideas', 'DISCOVERED', currentWorkspace?.id],
    queryFn: () => ideaApi.getByStatus('DISCOVERED', 0, 1),
    enabled: !!currentWorkspace,
  })

  const { data: scoredIdeas, isLoading: isLoadingScored } = useQuery({
    queryKey: ['ideas', 'SCORED', currentWorkspace?.id],
    queryFn: () => ideaApi.getByStatus('SCORED', 0, 1),
    enabled: !!currentWorkspace,
  })

  const { data: selectedIdeas, isLoading: isLoadingSelected } = useQuery({
    queryKey: ['ideas', 'SELECTED', currentWorkspace?.id],
    queryFn: () => ideaApi.getByStatus('SELECTED', 0, 1),
    enabled: !!currentWorkspace,
  })

  // Fetch recent ideas (top 5 by score)
  const { data: recentIdeasResponse, isLoading: isLoadingRecent } = useQuery({
    queryKey: ['ideas', 'recent', currentWorkspace?.id],
    queryFn: () => ideaApi.getAll(0, 5),
    enabled: !!currentWorkspace,
  })

  const recentIdeas = recentIdeasResponse?.data.data.content || []
  const isLoadingStats = isLoadingDiscovered || isLoadingScored || isLoadingSelected

  if (!currentWorkspace) {
    return (
      <div className="p-8">
        <div className="text-center">
          <Lightbulb className="w-12 h-12 mx-auto text-gray-300 mb-4" />
          <p className="text-gray-500">Select a workspace to get started</p>
        </div>
      </div>
    )
  }

  return (
    <div className="p-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-2">
          Welcome back! Here's what's happening in {currentWorkspace.name}.
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <StatCard
          title="New Ideas"
          value={discoveredIdeas?.data.data.totalElements || 0}
          icon={Lightbulb}
          color="bg-blue-100 text-blue-600"
          isLoading={isLoadingStats}
        />
        <StatCard
          title="Scored Ideas"
          value={scoredIdeas?.data.data.totalElements || 0}
          icon={TrendingUp}
          color="bg-purple-100 text-purple-600"
          isLoading={isLoadingStats}
        />
        <StatCard
          title="Selected Ideas"
          value={selectedIdeas?.data.data.totalElements || 0}
          icon={Target}
          color="bg-green-100 text-green-600"
          isLoading={isLoadingStats}
        />
      </div>

      {/* Quick Actions & Recent Ideas */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Quick Actions */}
        <div className="bg-white rounded-lg p-6 border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
          <div className="space-y-3">
            <Link
              to="/dashboard/ideas"
              className="flex items-center gap-3 p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition"
            >
              <div className="p-2 bg-indigo-100 text-indigo-600 rounded-lg">
                <Lightbulb className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-medium text-gray-900">View Ideas Board</h3>
                <p className="text-sm text-gray-500">Manage your content ideas pipeline</p>
              </div>
            </Link>

            <Link
              to="/dashboard/ideas?action=generate"
              className="flex items-center gap-3 p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition"
            >
              <div className="p-2 bg-green-100 text-green-600 rounded-lg">
                <Plus className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-medium text-gray-900">Generate New Ideas</h3>
                <p className="text-sm text-gray-500">Let AI discover trending topics</p>
              </div>
            </Link>
          </div>
        </div>

        {/* Recent Ideas */}
        <div className="bg-white rounded-lg p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Top Ideas</h2>
            <Link
              to="/dashboard/ideas"
              className="text-sm text-indigo-600 hover:text-indigo-800 font-medium"
            >
              View all →
            </Link>
          </div>

          {isLoadingRecent ? (
            <RecentIdeasSkeleton />
          ) : recentIdeas.length > 0 ? (
            <div className="space-y-3">
              {recentIdeas.map((idea) => (
                <div key={idea.id} className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h3 className="font-medium text-gray-900 line-clamp-1">{idea.title}</h3>
                      <p className="text-sm text-gray-500 mt-1 line-clamp-2">{idea.description}</p>
                    </div>
                    {idea.totalScore > 0 && (
                      <div className="ml-4">
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                          idea.totalScore >= 80
                            ? 'bg-green-100 text-green-800'
                            : idea.totalScore >= 60
                            ? 'bg-yellow-100 text-yellow-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {Math.round(idea.totalScore)}
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              <Lightbulb className="w-8 h-8 mx-auto mb-3 text-gray-300" />
              <p>No ideas yet. Generate your first batch!</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}