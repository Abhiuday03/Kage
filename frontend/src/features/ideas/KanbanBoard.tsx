/**
 * WHAT IT DOES:
 * Orchestrates the full Kanban board with drag-and-drop between columns.
 * Wraps all columns in a DndContext and handles card drops.
 *
 * WHY IT EXISTS:
 * @dnd-kit requires a single DndContext parent to coordinate all draggable/droppable
 * elements. This component owns that context and dispatches status updates on drop.
 *
 * HOW IT CONNECTS:
 * Rendered by IdeasPage.tsx.
 * Renders one KanbanColumn per status.
 * On drop → calls useUpdateIdeaStatus mutation.
 */

import { useState } from 'react'
import {
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  PointerSensor,
  useSensor,
  useSensors,
  closestCorners,
} from '@dnd-kit/core'
import { ContentIdea, IdeaStatus } from '../../services/api'
import { useUpdateIdeaStatus, KANBAN_STATUSES } from './useIdeas'
import KanbanColumn from './KanbanColumn'
import IdeaCard from './IdeaCard'
import toast from 'react-hot-toast'

interface KanbanBoardProps {
  onViewDetails: (idea: ContentIdea) => void
}

export default function KanbanBoard({ onViewDetails }: KanbanBoardProps) {
  const [activeIdea, setActiveIdea] = useState<ContentIdea | null>(null)
  const updateStatus = useUpdateIdeaStatus()

  // PointerSensor with a small activation distance so accidental drags don't fire
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 8 },
    })
  )

  const handleDragStart = (event: DragStartEvent) => {
    // The dragged item's data comes via the SortableContext item id
    // We don't have the idea object here, but we track it for the overlay
    const idea = event.active.data.current?.idea as ContentIdea | undefined
    if (idea) setActiveIdea(idea)
  }

  const handleDragEnd = (event: DragEndEvent) => {
    setActiveIdea(null)
    const { active, over } = event

    if (!over) return

    const ideaId = active.id as string
    const targetStatus = over.id as IdeaStatus

    // Only update if dropped onto a different status column
    if (!KANBAN_STATUSES.includes(targetStatus)) return

    updateStatus.mutate(
      { ideaId, status: targetStatus },
      {
        onSuccess: () => toast.success(`Moved to ${targetStatus.toLowerCase()}`),
        onError: () => toast.error('Failed to move idea'),
      }
    )
  }

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCorners}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      {/* Horizontally scrollable board */}
      <div className="flex gap-4 overflow-x-auto pb-4 min-h-[calc(100vh-200px)]">
        {KANBAN_STATUSES.map((status) => (
          <KanbanColumn
            key={status}
            status={status}
            onViewDetails={onViewDetails}
          />
        ))}
      </div>

      {/* Drag Overlay: ghost card shown while dragging */}
      <DragOverlay>
        {activeIdea ? (
          <div className="opacity-90 rotate-2">
            <IdeaCard idea={activeIdea} onViewDetails={() => {}} />
          </div>
        ) : null}
      </DragOverlay>
    </DndContext>
  )
}
