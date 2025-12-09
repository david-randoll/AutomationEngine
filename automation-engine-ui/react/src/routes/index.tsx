import { createFileRoute } from '@tanstack/react-router'
import NavBar from '@/components/NavBar'
import AutomationBuilderPage from '@/components/AutomationBuilderPage'
import type { UIMode } from '@/types/types'

type SearchParams = {
  mode?: UIMode
}

export const Route = createFileRoute('/')({
  component: Home,
  validateSearch: (search: Record<string, unknown>): SearchParams => {
    const mode = search.mode as UIMode | undefined
    if (mode && ['interactive', 'code', 'workflow'].includes(mode)) {
      return { mode }
    }
    return {}
  },
})

function Home() {
  return (
    <div className="min-h-screen bg-background">
      <NavBar />
      <AutomationBuilderPage />
    </div>
  )
}
