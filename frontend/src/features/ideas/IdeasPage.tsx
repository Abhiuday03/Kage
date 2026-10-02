/**
 * WHAT IT DOES:
 * Main ideas page containing the Kanban board and generate button.
 * Manages the detail drawer state.
 *
 * WHY IT EXISTS:
 * This is the primary interface for managing content ideas through their lifecycle.
 * Users view, sort, and act on ideas here.
 *
 * HOW IT CONNECTS:
 * Rendered by Dashboard.tsx at /dashboard/ideas route.
 * Contains KanbanBoard and IdeaDetailDrawer components.
 */

import { useState } from 'react'
import { Lightbulb } from 'lucide-react'
import { ContentIdea } from '../../services/api'
import { useWorkspace } from '../workspace/useWorkspace'
import KanbanBoard from './KanbanBoard'
import GenerateIdeasButton from './GenerateIdeasButton'
import IdeaDetailDrawer from './IdeaDetailDrawer'

export default function IdeasPage() {
  const { currentWorkspace } = useWorkspace()
  const [selectedIdea, setSelectedIdea] = useState<ContentIdea | null>(null)

  if (!currentWorkspace) {
    return (
      <div className="p-8">
        <div className="text-center">
          <Lightbulb className="w-12 h-12 mx-auto text-gray-300 mb-4" />
          <p className="text-gray-500">Select a workspace to view ideas</p>
        </div>
      </div>
    )
  }

  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="px-8 py-6 border-b bg-white">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Content Ideas</h1>
            <p className="text-gray-600 mt-1">
              Manage your content pipeline from discovery to briefing
            </p>
          </div>
          <GenerateIdeasButton />
        </div>
      </div>

      {/* Kanban Board */}
      <div className="flex-1 overflow-auto px-8 py-6">
        <KanbanBoard onViewDetails={setSelectedIdea} />
      </div>

      {/* Detail Drawer */}
      <IdeaDetailDrawer
        idea={selectedIdea}
        onClose={() => setSelectedIdea(null)}
      />
    </div>
  )
}
