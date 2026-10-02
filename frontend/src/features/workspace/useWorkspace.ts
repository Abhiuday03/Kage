/**
 * WHAT IT DOES:
 * Re-exports the useWorkspace hook for cleaner imports.
 *
 * WHY IT EXISTS:
 * Allows components to import { useWorkspace } from './useWorkspace'
 * instead of having to import from WorkspaceContext.tsx.
 *
 * HOW IT CONNECTS:
 * Simple re-export pattern for better developer experience.
 */

export { useWorkspace } from './WorkspaceContext'