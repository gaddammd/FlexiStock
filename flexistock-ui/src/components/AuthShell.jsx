import React from 'react';

export default function AuthShell({
  authMode,
  authForm,
  handleAuthChange,
  handleLogin,
  handleSignup,
  setAuthMode,
}) {
  return (
    <div className="auth-shell">
      <div className="auth-card">
        <div className="auth-hero">
          <p className="eyebrow">Welcome to FlexiStock</p>
          <h2>{authMode === 'login' ? 'Sign in to your account' : 'Create a new account'}</h2>
          <p className="auth-description">
            Manage inventory, track stock, and keep your team aligned with a cleaner workflow.
          </p>
        </div>
        <form onSubmit={authMode === 'login' ? handleLogin : handleSignup}>
          {authMode === 'signup' && (
            <label>
              Name
              <input
                value={authForm.name}
                onChange={(e) => handleAuthChange('name', e.target.value)}
                placeholder="Enter your name"
              />
            </label>
          )}
          <label>
            Email
            <input
              type="email"
              value={authForm.email}
              onChange={(e) => handleAuthChange('email', e.target.value)}
              placeholder="Enter your email"
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={authForm.password}
              onChange={(e) => handleAuthChange('password', e.target.value)}
              placeholder="Enter your password"
            />
          </label>
          <button type="submit" className="primary-button auth-submit-button">
            {authMode === 'login' ? 'Login' : 'Sign Up'}
          </button>
        </form>
        <div className="auth-switch">
          {authMode === 'login' ? (
            <p>
              Need an account?{' '}
              <button type="button" onClick={() => setAuthMode('signup')}>
                Sign Up
              </button>
            </p>
          ) : (
            <p>
              Already have an account?{' '}
              <button type="button" onClick={() => setAuthMode('login')}>
                Login
              </button>
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
