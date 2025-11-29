import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './app/globals.css'
import App from './components/App'
import AutomationBuilderPage from './components/AutomationBuilderPage'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App>
      <AutomationBuilderPage />
    </App>
  </StrictMode>,
)

