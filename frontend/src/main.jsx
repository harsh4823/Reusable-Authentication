import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Provider } from 'react-redux'
import { Toaster } from '@/components/ui/sonner'
import { store } from '@/store'
import App from './App'
import './App.css'

const queryClient = new QueryClient()

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <Provider store={store}>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <App />
          <Toaster richColors theme="dark" position="top-right" />
        </BrowserRouter>
      </QueryClientProvider>
    </Provider>
  </StrictMode>
)
