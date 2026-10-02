/**
 * WHAT IT DOES:
 * Renders a single content idea card inside a Kanban column.
 * Shows title, score badge, status, and quick action buttons.
 *
 * WHY IT EXISTS:
 * Each idea needs a compact visual representation in the Kanban board.
 * The card communicates the most important info at a glance.
 *
 * HOW IT CONNECTS:
 * Rendered by KanbanColumn.tsx for each idea in a status bucket.
 * Clicking it calls onViewDetails() to open IdeaDetailDrawer.
 * Uses useSortable from @dnd-kit/sortable for drag-and-drop.
 */

import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical, Eye, CheckCircle, XCircle, TrendingUp, Target, Zap } from 'lucide-react'
import { ContentIdea } from '../../services/api'
import { useUpdateIdeaStatus } from './useIdeas'
import toast from 'react-hot-toast'

interface IdeaCardProps {
  idea: ContentIdea
  onViewDetails: (idea: ContentIdea) => void
}

function ScoreBadge({ score }: { score: number }) {
  if (score === 0) return null

  const color =
    score >= 80
      ? 'bg-green-100 text-green-800'
      : score >= 60
      ? 'bg-yellow-100 text-yellow-800'
      : 'bg-red-100 text-red-800'

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-bold ${color}`}>
      {Math.round(score)}
    </span>
  )
}

function ScoreBreakdown({ idea }: { idea: ContentIdea }) {
  if (idea.totalScore === 0) return null

  return (
    <div className="mt-2 flex items-center gap-3 text-xs text-gray-500">
      <span className="flex items-center gap-1">
        <TrendingUp className="w-3 h-3 text-blue-400" />
        {Math.round(idea.trendScore)}
      </span>
      <span className="flex items-center gap-1">
        <Target className="w-3 h-3 text-purple-400" />
        {Math.round(idea.relevanceScore)}
      </span>
      <span className="flex items-center gap-1">
        <Zap className="w-3 h-3 text-orange-400" />
        {Math.round(idea.engagementScore)}
      </span>
    </div>
  )
}

export default function IdeaCard({ idea, onViewDetails }: IdeaCardProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } =
    useSortable({ id: idea.id })

  const updateStatus = useUpdateIdeaStatus()

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  }

  const handleSelect = (e: React.MouseEvent) => {
    e.stopPropagation()
    updateStatus.mutate(
      { ideaId: idea.id, status: 'SELECTED' },
      {
        onSuccess: () => toast.success('Idea moved to Selected'),
        onError: () => toast.error('Failed to update idea status'),
      }
    )
  }

  const handleReject = (e: React.MouseEvent) => {
    e.stopPropagation()
    updateStatus.mutate(
      { ideaId: idea.id, status: 'REJECTED' },
      {
        onSuccess: () => toast.success('Idea rejected'),
        onError: () => toast.error('Failed to update idea status'),
      }
    )
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="bg-white rounded-lg border border-gray-200 p-3 shadow-sm hover:shadow-md transition-shadow cursor-pointer group"
      onClick={() => onViewDetails(idea)}
    >
      {/* Card Header */}
      <div className="flex items-start gap-2">
        {/* Drag Handle */}
        <div
          {...attributes}
          {...listeners}
          className="mt-0.5 text-gray-300 hover:text-gray-500 cursor-grab active:cursor-grabbing flex-shrink-0"
          onClick={(e) => e.stopPropagation()}
        >
          <GripVertical className="w-4 h-4" />
        </div>

        {/* Title */}
        <h3 className="flex-1 text-sm font-medium text-gray-900 line-clamp-2 leading-tight">
          {idea.title}
        </h3>

        {/* Score Badge */}
        <ScoreBadge score={idea.totalScore} />
      </div>

      {/* Description */}
      <p className="mt-2 text-xs text-gray-500 line-clamp-2 ml-6">{idea.description}</p>

      {/* Score Breakdown */}
      <div className="ml-6">
        <ScoreBreakdown idea={idea} />
      </div>

      {/* Action Buttons */}
      <div className="mt-3 ml-6 flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
        <button
          onClick={(e) => { e.stopPropagation(); onViewDetails(idea) }}
          className="flex items-center gap-1 px-2 py-1 text-xs text-gray-600 hover:text-indigo-600 hover:bg-indigo-50 rounded transition"
        >
          <Eye className="w-3 h-3" />
          Details
        </button>

        {idea.status !== 'SELECTED' && idea.status !== 'REJECTED' && (
          <>
            <button
              onClick={handleSelect}
              disabled={updateStatus.isPending}
              className="flex items-center gap-1 px-2 py-1 text-xs text-gray-600 hover:text-green-600 hover:bg-green-50 rounded transition disabled:opacity-50"
            >
              <CheckCircle className="w-3 h-3" />
              Select
            </button>

            <button
              onClick={handleReject}
              disabled={updateStatus.isPending}
              className="flex items-center gap-1 px-2 py-1 text-xs text-gray-600 hover:text-red-600 hover:bg-red-50 rounded transition disabled:opacity-50"
            >
              <XCircle className="w-3 h-3" />
              Reject
            </button>
          </>
        )}
      </div>
    </div>
  )
}
