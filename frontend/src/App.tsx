import { useEffect, useState } from 'react';
import AuthPage from './pages/AuthPage';
import Dashboard from './pages/Dashboard';
import './styles/Dashboard.css';

type ThemeMode = 'light' | 'dark';
type ViewMode = 'dashboard' | 'login' | 'signup';

function getInitialTheme(): ThemeMode {
  const savedTheme = window.localStorage.getItem('theme-mode');

  if (savedTheme === 'light' || savedTheme === 'dark') {
    return savedTheme;
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function App() {
  const [theme, setTheme] = useState<ThemeMode>(getInitialTheme);
  const [view, setView] = useState<ViewMode>('dashboard');

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem('theme-mode', theme);
  }, [theme]);

  const sharedProps = {
    theme,
    onToggleTheme: () =>
      setTheme((currentTheme) => (currentTheme === 'light' ? 'dark' : 'light')),
    onGoHome: () => setView('dashboard'),
    onGoLogin: () => setView('login'),
    onGoSignup: () => setView('signup'),
  };

  if (view === 'login' || view === 'signup') {
    return <AuthPage {...sharedProps} mode={view} />;
  }

  return <Dashboard {...sharedProps} />;
}

export default App;
