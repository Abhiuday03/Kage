/**
 * WHAT IT DOES:
 * Renders a single Kanban column for a given IdeaStatus.
 * Shows a header with count, loading skeletons, and a list of IdeaCards.
 *
 * WHY IT EXISTS:
 * Each status bucket (DISCOVERED, SCORED, etc.) needs its own visual column.
 * This component handles the per-column query and layout.
 *
 * HOW IT CONNECTS:
 * Rendered by KanbanBoard.tsx once per status.
 * Uses useDroppable from @dnd-kit/core to accept dragged cards.
 */

import { useDroppable } from '@dnd-kit/core'
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable'
import { ContentIdea, IdeaStatus } from '../../services/api'
import { useIdeasByStatus } from './useIdeas'
import IdeaCard from './IdeaCard'

interface ColumnConfig {
  label: string
  headerColor: string
  bodyColor: string
  dotColor: string
}

const COLUMN_CONFIGS: Record<IdeaStatus, ColumnConfig> = {
  DISCOVERED: {
    label: 'Discovered',
    headerColor: 'text-blue-700',
    bodyColor: 'bg-blue-50 border-blue-200',
    dotColor: 'bg-blue-500',
  },
  SCORED: {
    label: 'Scored',
    headerColor: 'text-purple-700',
    bodyColor: 'bg-purple-50 border-purple-200',
    dotColor: 'bg-purple-500',
  },
  SELECTED: {
    label: 'Selected',
    headerColor: 'text-orange-700',
    bodyColor: 'bg-orange-50 border-orange-200',
    dotColor: 'bg-orange-500',
  },
  BRIEFED: {
    label: 'Briefed',
    headerColor: 'text-green-700',
    bodyColor: 'bg-green-50 border-green-200',
    dotColor: 'bg-green-500',
  },
  REJECTED: {
    label: 'Rejected',
    headerColor: 'text-gray-500',
    bodyColor: 'bg-gray-50 border-gray-200',
    dotColor: 'bg-gray-400',
  },
  IN_PROGRESS: {
    label: 'In Progress',
    headerColor: 'text-teal-700',
    bodyColor: 'bg-teal-50 border-teal-200',
    dotColor: 'bg-teal-500',
  },
  COMPLETED: {
    label: 'Completed',
    headerColor: 'text-emerald-700',
    bodyColor: 'bg-emerald-50 border-emerald-200',
    dotColor: 'bg-emerald-500',
  },
  ARCHIVED: {
    label: 'Archived',
    headerColor: 'text-gray-400',
    bodyColor: 'bg-gray-50 border-gray-100',
    dotColor: 'bg-gray-300',
  },
}

function SkeletonCard() {
  return (
    <div className="bg-white rounded-lg border border-gray-200 p-3 animate-pulse">
      <div className="h-3 bg-gray-200 rounded w-3/4 mb-2" />
      <div className="h-3 bg-gray-200 rounded w-1/2 mb-3" />
      <div className="h-2 bg-gray-100 rounded w-full mb-1" />
      <div className="h-2 bg-gray-100 rounded w-5/6" />
    </div>
  )
}

interface KanbanColumnProps {
  status: IdeaStatus
  onViewDetails: (idea: ContentIdea) => void
}

export default function KanbanColumn({ status, onViewDetails }: KanbanColumnProps) {
  const config = COLUMN_CONFIGS[status]
  const { data: ideas = [], isLoading } = useIdeasByStatus(status)

  const { setNodeRef, isOver } = useDroppable({ id: status })

  return (
    <div className="flex flex-col w-72 flex-shrink-0">
      {/* Column Header */}
      <div className="flex items-center gap-2 mb-3 px-1">
        <div className={`w-2.5 h-2.5 rounded-full ${config.dotColor}`} />
        <h2 className={`text-sm font-semibold ${config.headerColor}`}>{config.label}</h2>
        <span className="ml-auto text-xs font-medium text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">
          {isLoading ? '…' : ideas.length}
        </span>
      </div>

      {/* Column Body */}
      <div
        ref={setNodeRef}
        className={`flex-1 rounded-xl border-2 border-dashed p-2 transition-colors min-h-[200px] ${
          config.bodyColor
        } ${isOver ? 'ring-2 ring-indigo-400 ring-offset-1' : ''}`}
      >
        {isLoading ? (
          <div className="space-y-2">
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </div>
        ) : (
          <SortableContext items={ideas.map((i) => i.id)} strategy={verticalListSortingStrategy}>
            <div className="space-y-2">
              {ideas.map((idea) => (
                <IdeaCard key={idea.id} idea={idea} onViewDetails={onViewDetails} />
              ))}
              {ideas.length === 0 && (
                <div className="text-center py-8 text-xs text-gray-400">
                  No ideas here yet
                </div>
              )}
            </div>
          </SortableContext>
        )}
      </div>
    </div>
  )
}
