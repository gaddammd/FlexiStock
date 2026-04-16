import React from 'react';

export default function Sidebar({ page, setPage, isAdmin, logout, user, handleLoadUsers }) {
  return (
    <aside className="sidebar">
      <div>
        <div className="brand">FlexiStock</div>
        <div className="sidebar-user">
          <p>{user?.name ?? 'Guest'}</p>
          <p className="sidebar-role">{user?.role ?? 'User'}</p>
        </div>
      </div>

      <nav className="sidebar-nav">
        <button className={page === 'dashboard' ? 'active' : ''} onClick={() => setPage('dashboard')}>
          Dashboard
        </button>
        <button className={page === 'inventory' ? 'active' : ''} onClick={() => setPage('inventory')}>
          Inventory
        </button>
        {isAdmin && (
          <button className={page === 'users' ? 'active' : ''} onClick={handleLoadUsers}>
            Users
          </button>
        )}
        {isAdmin && (
          <button className={page === 'receipts' ? 'active' : ''} onClick={() => setPage('receipts')}>
            Receipts
          </button>
        )}
        {isAdmin && (
          <button className={page === 'metrics' ? 'active' : ''} onClick={() => setPage('metrics')}>
            Metrics
          </button>
        )}
        <button className={page === 'profile' ? 'active' : ''} onClick={() => setPage('profile')}>
          Profile
        </button>
      </nav>

      <div className="sidebar-footer">
        <button className="danger logout-button" onClick={logout}>
          Logout
        </button>
      </div>
    </aside>
  );
}
