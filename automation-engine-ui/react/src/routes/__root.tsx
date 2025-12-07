import { Outlet, createRootRoute } from '@tanstack/react-router'
import { TanStackRouterDevtoolsPanel } from '@tanstack/react-router-devtools'
import { TanStackDevtools } from '@tanstack/react-devtools'
import { FormProvider, useForm } from 'react-hook-form'
import { AutomationEngineProvider } from '@/providers/AutomationEngineProvider'
import { Toaster } from 'sonner'

function Providers({ children }: { children: React.ReactNode }) {
  const methods = useForm({
    defaultValues: {},
    mode: 'onBlur',
  })

  return (
    <FormProvider {...methods}>
      <AutomationEngineProvider>{children}</AutomationEngineProvider>
    </FormProvider>
  )
}

export const Route = createRootRoute({
  component: () => (
    <Providers>
      <Outlet />
      <Toaster richColors position="top-right" />
      <TanStackDevtools
        config={{
          position: 'bottom-right',
        }}
        plugins={[
          {
            name: 'Tanstack Router',
            render: <TanStackRouterDevtoolsPanel />,
          },
        ]}
      />
    </Providers>
  ),
})
