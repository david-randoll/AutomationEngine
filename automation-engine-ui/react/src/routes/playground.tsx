import { createFileRoute } from '@tanstack/react-router'
import NavBar from '@/components/NavBar'
import { PlaygroundPage } from '@/components/playground'

export const Route = createFileRoute('/playground')({
    component: Playground,
})

function Playground() {
    return (
        <div className="h-screen flex flex-col bg-background">
            <NavBar />
            <PlaygroundPage className="flex-1 overflow-hidden" />
        </div>
    )
}
