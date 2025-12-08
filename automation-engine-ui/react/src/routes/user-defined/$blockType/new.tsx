import { createFileRoute, notFound } from '@tanstack/react-router'
import NavBar from '@/components/NavBar'
import UserDefinedRegisterPage from '@/components/UserDefinedRegisterPage'
import type { BlockType } from '@/types/user-defined'

const validBlockTypes = ['actions', 'conditions', 'triggers', 'variables'] as const

function isValidBlockType(type: string): type is BlockType {
    return (validBlockTypes as readonly string[]).includes(type)
}

export const Route = createFileRoute('/user-defined/$blockType/new')({
    component: NewUserDefinedPage,
    beforeLoad: ({ params }) => {
        if (!isValidBlockType(params.blockType)) {
            throw notFound()
        }
    },
})

function NewUserDefinedPage() {
    const { blockType } = Route.useParams()

    return (
        <div className="min-h-screen bg-background">
            <NavBar />
            <UserDefinedRegisterPage blockType={blockType as BlockType} />
        </div>
    )
}
