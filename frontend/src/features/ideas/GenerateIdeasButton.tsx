/**
 * WHAT IT DOES:
 * Button that triggers AI-powered idea generation.
 * Opens a modal to select a niche and set the idea count.
 *
 * WHY IT EXISTS:
 * Users need an easy way to request new content ideas from the AI Engine.
 * This component handles the discovery trigger flow with niche selection.
 *
 * HOW IT CONNECTS:
 * Used in IdeasPage.tsx header.
 * Calls useTriggerDiscovery mutation → POST /discovery/trigger.
 */

import { useState } from 'react'
import { X, Sparkles, Loader2 } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { nicheApi } from '../../services/api'
import { useWorkspace } from '../workspace/useWorkspace'
import { useTriggerDiscovery } from './useIdeas'
import toast from 'react-hot-toast'

export default function GenerateIdeasButton() {
  const [isOpen, setIsOpen] = useState(false)
  const [selectedNicheId, setSelectedNicheId] = useState('')
  const [count, setCount] = useState(5)

  const { currentWorkspace } = useWorkspace()
  const triggerDiscovery = useTriggerDiscovery()

  // Fetch niches for the current workspace
  const { data: nichesResponse, isLoading: isLoadingNiches } = useQuery({
    queryKey: ['niches', currentWorkspace?.id],
    queryFn: () => nicheApi.getAll(currentWorkspace!.id),
    enabled: !!currentWorkspace && isOpen,
  })

  const niches = nichesResponse?.data.data || []

  const handleGenerate = () => {
    if (!selectedNicheId) {
      toast.error('Please select a niche')
      return
    }

    triggerDiscovery.mutate(
      { nicheId: selectedNicheId, count },
      {
        onSuccess: () => {
          toast.success('🎉 Idea generation started! New ideas will appear in ~30 seconds.')
          setIsOpen(false)
          setSelectedNicheId('')
          setCount(5)
        },
        onError: () => {
          toast.error('Failed to start idea generation')
        },
      }
    )
  }

  return (
    <>
      <button
        onClick={() => setIsOpen(true)}
        className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition font-medium text-sm shadow-sm"
      >
        <Sparkles className="w-4 h-4" />
        Generate Ideas
      </button>

      {/* Modal */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
            {/* Header */}
            <div className="flex items-center justify-between p-6 border-b">
              <h2 className="text-xl font-semibold text-gray-900">Generate Content Ideas</h2>
              <button
                onClick={() => setIsOpen(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Body */}
            <div className="p-6 space-y-4">
              {isLoadingNiches ? (
                <div className="text-center py-4 text-gray-500">Loading niches...</div>
              ) : niches.length === 0 ? (
                <div className="text-center py-4 text-gray-500">
                  <p>No niches found. Create a niche first to generate ideas.</p>
                </div>
              ) : (
                <>
                  {/* Niche Selection */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Select Niche
                    </label>
                    <select
                      value={selectedNicheId}
                      onChange={(e) => setSelectedNicheId(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    >
                      <option value="">Choose a niche...</option>
                      {niches.map((niche) => (
                        <option key={niche.id} value={niche.id}>
                          {niche.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Count Selection */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Number of Ideas
                    </label>
                    <input
                      type="number"
                      min="1"
                      max="10"
                      value={count}
                      onChange={(e) => setCount(Math.max(1, Math.min(10, parseInt(e.target.value) || 1)))}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    />
                    <p className="text-xs text-gray-500 mt-1">Generate between 1-10 ideas (recommended: 5)</p>
                  </div>

                  {/* Info */}
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                    <p className="text-sm text-blue-800">
                      💡 AI will generate ideas based on your niche's keywords and target audience.
                      This takes ~20-30 seconds.
                    </p>
                  </div>
                </>
              )}
            </div>

            {/* Footer */}
            <div className="flex items-center justify-end gap-3 p-6 border-t bg-gray-50">
              <button
                onClick={() => setIsOpen(false)}
                className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition"
                disabled={triggerDiscovery.isPending}
              >
                Cancel
              </button>
              <button
                onClick={handleGenerate}
                disabled={triggerDiscovery.isPending || !selectedNicheId || niches.length === 0}
                className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {triggerDiscovery.isPending ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Generating...
                  </>
                ) : (
                  <>
                    <Sparkles className="w-4 h-4" />
                    Generate
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
