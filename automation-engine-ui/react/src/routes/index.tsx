import { createFileRoute } from '@tanstack/react-router'
import NavBar from '@/components/NavBar'
import AutomationBuilderPage from '@/components/AutomationBuilderPage'

export const Route = createFileRoute('/')({
  component: Home,
})

function Home() {
  return (
    <div className="min-h-screen bg-background">
      <NavBar />
      <AutomationBuilderPage />
    </div>
  )
}
