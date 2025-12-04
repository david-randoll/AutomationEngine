import { StrictMode, useState } from 'react'
import { createRoot } from 'react-dom/client'
import './app/globals.css'
import App from './components/App'
import AutomationBuilderPage from './components/AutomationBuilderPage'
import UserDefinedManager from './components/UserDefinedManager'

type Page = 'builder' | 'user-defined'

const Main = () => {
  const [currentPage, setCurrentPage] = useState<Page>('builder')

  return (
    <App>
      <nav className="bg-white border-b shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <div className="flex items-center gap-4 h-14">
            <span className="font-semibold text-gray-900">Automation Engine</span>
            <div className="flex gap-2">
              <button
                onClick={() => setCurrentPage('builder')}
                className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
                  currentPage === 'builder'
                    ? 'bg-primary text-primary-foreground'
                    : 'text-gray-600 hover:bg-gray-100'
                }`}
              >
                Automation Builder
              </button>
              <button
                onClick={() => setCurrentPage('user-defined')}
                className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
                  currentPage === 'user-defined'
                    ? 'bg-primary text-primary-foreground'
                    : 'text-gray-600 hover:bg-gray-100'
                }`}
              >
                User-Defined Types
              </button>
            </div>
          </div>
        </div>
      </nav>
      {currentPage === 'builder' ? <AutomationBuilderPage /> : <UserDefinedManager />}
    </App>
  )
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Main />
  </StrictMode>,
)

