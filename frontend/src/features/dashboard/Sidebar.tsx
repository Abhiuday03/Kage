/**
 * WHAT IT DOES:
 * Left sidebar navigation for the main app layout.
 * Shows navigation links for different app sections and workspace switcher.
 *
 * WHY IT EXISTS:
 * The app needs consistent navigation between Ideas, Research, Content, etc.
 * The sidebar provides a persistent navigation structure with visual hierarchy.
 *
 * HOW IT CONNECTS:
 * Used by Dashboard.tsx as the main navigation component.
 * Uses React Router for navigation and WorkspaceSwitcher for workspace selection.
 */

import { NavLink } from 'react-router-dom'
import {
  Home,
  Lightbulb,
  Search,
  FileText,
  Image,
  Palette,
  Settings,
  LogOut
} from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import WorkspaceSwitcher from '../workspace/WorkspaceSwitcher'

interface NavItemProps {
  to: string
  icon: React.ComponentType<{ className?: string }>
  label: string
  isStub?: boolean
}

function NavItem({ to, icon: Icon, label, isStub = false }: NavItemProps) {
  const baseClasses = 'flex items-center gap-3 px-3 py-2 rounded-lg transition-colors text-sm font-medium'
  const activeClasses = 'bg-indigo-100 text-indigo-700'
  const inactiveClasses = isStub
    ? 'text-gray-400 cursor-not-allowed'
    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'

  if (isStub) {
    return (
      <div className={`${baseClasses} ${inactiveClasses}`}>
        <Icon className="w-5 h-5" />
        {label}
        <span className="ml-auto text-xs bg-gray-200 text-gray-600 px-2 py-1 rounded">Soon</span>
      </div>
    )
  }

  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `${baseClasses} ${isActive ? activeClasses : inactiveClasses}`
      }
    >
      <Icon className="w-5 h-5" />
      {label}
    </NavLink>
  )
}

export default function Sidebar() {
  const { logout } = useAuth()

  return (
    <div className="w-64 bg-white border-r border-gray-200 flex flex-col h-full">
      {/* Header */}
      <div className="p-6 border-b border-gray-200">
        <h1 className="text-xl font-bold text-gray-900">KAGE</h1>
        <p className="text-sm text-gray-500 mt-1">Content OS</p>
      </div>

      {/* Workspace Switcher */}
      <div className="p-4 border-b border-gray-200">
        <label className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-2 block">
          Workspace
        </label>
        <WorkspaceSwitcher />
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4">
        <div className="space-y-1">
          <NavItem to="/dashboard" icon={Home} label="Dashboard" />
          <NavItem to="/dashboard/ideas" icon={Lightbulb} label="Ideas" />
          <NavItem to="/dashboard/research" icon={Search} label="Research" isStub />
          <NavItem to="/dashboard/content" icon={FileText} label="Content" isStub />
          <NavItem to="/dashboard/media" icon={Image} label="Media" isStub />
        </div>

        <div className="mt-8 pt-4 border-t border-gray-200">
          <div className="space-y-1">
            <NavItem to="/dashboard/brand" icon={Palette} label="Brand" />
            <NavItem to="/dashboard/settings" icon={Settings} label="Settings" isStub />
          </div>
        </div>
      </nav>

      {/* Footer */}
      <div className="p-4 border-t border-gray-200">
        <button
          onClick={logout}
          className="flex items-center gap-3 px-3 py-2 rounded-lg transition-colors text-sm font-medium text-gray-600 hover:bg-gray-100 hover:text-gray-900 w-full"
        >
          <LogOut className="w-5 h-5" />
          Sign Out
        </button>
      </div>
    </div>
  )
}