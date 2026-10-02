import { Routes, Route } from 'react-router-dom'
import Sidebar from './Sidebar'
import DashboardHome from './DashboardHome'
import IdeasPage from '../ideas/IdeasPage'

// Placeholder components for future phases
function BrandPage() {
  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-4">Brand Settings</h1>
      <div className="bg-gray-50 border border-gray-200 rounded-lg p-6">
        <p className="text-gray-600">
          Brand configuration from Phase 1. This will be enhanced in future phases.
        </p>
      </div>
    </div>
  )
}

export default function Dashboard() {
  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <Sidebar />

      {/* Main Content */}
      <div className="flex-1 overflow-auto">
        <Routes>
          <Route index element={<DashboardHome />} />
          <Route path="ideas" element={<IdeasPage />} />
          <Route path="brand" element={<BrandPage />} />
        </Routes>
      </div>
    </div>
  )
}