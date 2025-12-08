import { createFileRoute, notFound, useNavigate } from '@tanstack/react-router'
import { useEffect } from 'react'
import NavBar from '@/components/NavBar'
import UserDefinedEditPage from '@/components/UserDefinedEditPage'
import type { BlockType } from '@/types/user-defined'

const validBlockTypes = ['actions', 'conditions', 'triggers', 'variables'] as const

function isValidBlockType(type: string): type is BlockType {
    return (validBlockTypes as readonly string[]).includes(type)
}

export const Route = createFileRoute('/user-defined/$blockType/edit')({
    component: EditUserDefinedPage,
    validateSearch: (search: Record<string, unknown>): { name?: string } => {
        return {
            name: typeof search.name === 'string' ? search.name : undefined,
        }
    },
    beforeLoad: ({ params }) => {
        if (!isValidBlockType(params.blockType)) {
            throw notFound()
        }
    },
})

function EditUserDefinedPage() {
    const { blockType } = Route.useParams()
    const { name } = Route.useSearch()
    const navigate = useNavigate()

    useEffect(() => {
        if (!name) {
            navigate({ to: '/user-defined/$blockType', params: { blockType } })
        }
    }, [name, blockType, navigate])

    if (!name) {
        return (
            <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
                <div className="max-w-7xl mx-auto">
                    <div className="text-center py-12">Redirecting...</div>
                </div>
            </div>
        )
    }

    return (
        <div className="min-h-screen bg-background">
            <NavBar />
            <UserDefinedEditPage blockType={blockType as BlockType} name={name} />
        </div>
    )
}
