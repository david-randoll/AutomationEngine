import { createFileRoute } from '@tanstack/react-router'
import NavBar from '@/components/NavBar'
import UserDefinedPage from '@/components/UserDefinedPage'

export const Route = createFileRoute('/user-defined/')({
    component: UserDefinedIndex,
})

function UserDefinedIndex() {
    return (
        <div className="min-h-screen bg-background">
            <NavBar />
            <UserDefinedPage initialTab="actions" />
        </div>
    )
}
