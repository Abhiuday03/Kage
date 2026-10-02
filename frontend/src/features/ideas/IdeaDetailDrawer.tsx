/**
 * WHAT IT DOES:
 * Right-side slide-out drawer showing full details of a selected idea.
 * Displays scores, description, and action buttons (Score, Select, Reject).
 *
 * WHY IT EXISTS:
 * Cards show limited info due to space constraints. The drawer provides
 * the full view and allows users to take actions on the idea.
 *
 * HOW IT CONNECTS:
 * Opened by IdeasPage when a card is clicked.
 * Uses useScoreIdea and useUpdateIdeaStatus mutations for actions.
 */

import { X, TrendingUp, Target, Zap, Calendar, CheckCircle, XCircle, Sparkles } from 'lucide-react'
import { useState } from 'react'
import { ContentIdea } from '../../services/api'
import { useScoreIdea, useUpdateIdeaStatus } from './useIdeas'
import { useQuery } from '@tanstack/react-query'
import { nicheApi } from '../../services/api'
import { useWorkspace } from '../workspace/useWorkspace'
import toast from 'react-hot-toast'

interface IdeaDetailDrawerProps {
  idea: ContentIdea | null
  onClose: () => void
}

function ScoreBar({ label, score, icon: Icon, color }: {
  label: string
  score: number
  icon: React.ComponentType<{ className?: string }>
  color: string
}) {
  return (
    <div>
      <div className="flex items-center justify-between text-sm mb-2">
        <div className="flex items-center gap-2">
          <Icon className={`w-4 h-4 ${color}`} />
          <span className="font-medium text-gray-700">{label}</span>
        </div>
        <span className="font-bold text-gray-900">{Math.round(score)}/100</span>
      </div>
      <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
        <div
          className={`h-full ${color.replace('text-', 'bg-')} transition-all duration-500`}
          style={{ width: `${score}%` }}
        />
      </div>
    </div>
  )
}

export default function IdeaDetailDrawer({ idea, onClose }: IdeaDetailDrawerProps) {
  const { currentWorkspace } = useWorkspace()
  const [selectedNicheId, setSelectedNicheId] = useState('')

  const scoreIdea = useScoreIdea()
  const updateStatus = useUpdateIdeaStatus()

  // Fetch niches for scoring
  const { data: nichesResponse } = useQuery({
    queryKey: ['niches', currentWorkspace?.id],
    queryFn: () => nicheApi.getAll(currentWorkspace!.id),
    enabled: !!currentWorkspace && !!idea,
  })

  const niches = nichesResponse?.data.data || []

  if (!idea) return null

  const hasScores = idea.totalScore > 0

  const handleScore = () => {
    if (!selectedNicheId) {
      toast.error('Please select a niche to score against')
      return
    }

    scoreIdea.mutate(
      { ideaId: idea.id, nicheId: selectedNicheId },
      {
        onSuccess: () => {
          toast.success('✨ Idea scored successfully!')
        },
        onError: () => {
          toast.error('Failed to score idea')
        },
      }
    )
  }

  const handleSelect = () => {
    updateStatus.mutate(
      { ideaId: idea.id, status: 'SELECTED' },
      {
        onSuccess: () => {
          toast.success('Idea moved to Selected')
          onClose()
        },
        onError: () => toast.error('Failed to update idea'),
      }
    )
  }

  const handleReject = () => {
    updateStatus.mutate(
      { ideaId: idea.id, status: 'REJECTED' },
      {
        onSuccess: () => {
          toast.success('Idea rejected')
          onClose()
        },
        onError: () => toast.error('Failed to update idea'),
      }
    )
  }

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black bg-opacity-30 z-40 transition-opacity"
        onClick={onClose}
      />

      {/* Drawer */}
      <div className="fixed top-0 right-0 h-full w-full max-w-2xl bg-white shadow-2xl z-50 overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-gray-900">Idea Details</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Status Badge */}
          <div>
            <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${
              idea.status === 'DISCOVERED' ? 'bg-blue-100 text-blue-800' :
              idea.status === 'SCORED' ? 'bg-purple-100 text-purple-800' :
              idea.status === 'SELECTED' ? 'bg-orange-100 text-orange-800' :
              idea.status === 'BRIEFED' ? 'bg-green-100 text-green-800' :
              'bg-gray-100 text-gray-800'
            }`}>
              {idea.status}
            </span>
          </div>

          {/* Title */}
          <div>
            <h3 className="text-2xl font-bold text-gray-900">{idea.title}</h3>
          </div>

          {/* Description */}
          <div>
            <h4 className="text-sm font-semibold text-gray-700 mb-2">Description</h4>
            <p className="text-gray-600 leading-relaxed">{idea.description}</p>
          </div>

          {/* Scores Section */}
          {hasScores ? (
            <div className="bg-gray-50 rounded-lg p-6 space-y-4">
              <h4 className="text-sm font-semibold text-gray-700 mb-4">AI Score Breakdown</h4>

              <ScoreBar
                label="Trend Score"
                score={idea.trendScore}
                icon={TrendingUp}
                color="text-blue-500"
              />

              <ScoreBar
                label="Relevance Score"
                score={idea.relevanceScore}
                icon={Target}
                color="text-purple-500"
              />

              <ScoreBar
                label="Engagement Score"
                score={idea.engagementScore}
                icon={Zap}
                color="text-orange-500"
              />

              {/* Total Score */}
              <div className="pt-4 border-t border-gray-200">
                <div className="flex items-center justify-between">
                  <span className="text-lg font-bold text-gray-900">Total Score</span>
                  <span className={`text-3xl font-bold ${
                    idea.totalScore >= 80 ? 'text-green-600' :
                    idea.totalScore >= 60 ? 'text-yellow-600' :
                    'text-red-600'
                  }`}>
                    {Math.round(idea.totalScore)}
                  </span>
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <p className="text-sm text-blue-800 mb-4">
                💡 This idea hasn't been scored yet. Select a niche and score it to see AI-powered insights!
              </p>

              <div className="space-y-3">
                <select
                  value={selectedNicheId}
                  onChange={(e) => setSelectedNicheId(e.target.value)}
                  className="w-full px-3 py-2 border border-blue-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select a niche...</option>
                  {niches.map((niche) => (
                    <option key={niche.id} value={niche.id}>
                      {niche.name}
                    </option>
                  ))}
                </select>

                <button
                  onClick={handleScore}
                  disabled={scoreIdea.isPending || !selectedNicheId}
                  className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition disabled:opacity-50"
                >
                  <Sparkles className="w-4 h-4" />
                  {scoreIdea.isPending ? 'Scoring...' : 'Score with AI'}
                </button>
              </div>
            </div>
          )}

          {/* Metadata */}
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <div className="flex items-center gap-2 text-gray-500 mb-1">
                <Calendar className="w-4 h-4" />
                Discovered
              </div>
              <div className="font-medium text-gray-900">
                {idea.discoveredAt
                  ? new Date(idea.discoveredAt).toLocaleDateString()
                  : 'N/A'}
              </div>
            </div>
            <div>
              <div className="text-gray-500 mb-1">Created</div>
              <div className="font-medium text-gray-900">
                {new Date(idea.createdAt).toLocaleDateString()}
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          {idea.status !== 'SELECTED' && idea.status !== 'REJECTED' && (
            <div className="flex gap-3 pt-4 border-t">
              <button
                onClick={handleSelect}
                disabled={updateStatus.isPending}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg transition font-medium disabled:opacity-50"
              >
                <CheckCircle className="w-5 h-5" />
                Select for Content
              </button>

              <button
                onClick={handleReject}
                disabled={updateStatus.isPending}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-3 bg-red-600 hover:bg-red-700 text-white rounded-lg transition font-medium disabled:opacity-50"
              >
                <XCircle className="w-5 h-5" />
                Reject
              </button>
            </div>
          )}
        </div>
      </div>
    </>
  )
}
