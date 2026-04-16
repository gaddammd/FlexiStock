import { useCallback, useEffect, useMemo, useState } from 'react';
import './App.css';
import {
  adjustQuantity,
  createProduct,
  deleteProduct,
  fetchProducts,
  fetchUsers,
  login,
  requestAdminAccess,
  signup,
  updateProduct,
  updateUserRole,
  validateToken,
} from './api';
import AuthShell from './components/AuthShell';
import ProductDetail from './components/ProductDetail';
import ProductForm from './components/ProductForm';
import Sidebar from './components/Sidebar';

const STORAGE_KEY = {
  token: 'flexistock-token',
  user: 'flexistock-user',
  dbMode: 'flexistock-db-mode',
};

const defaultProductForm = {
  sku: '',
  name: '',
  description: '',
  category: '',
  quantity: 0,
  location: '',
  lowStockThreshold: 10,
  attributes: [{ key: '', value: '' }],
};

function App() {
  const [token, setToken] = useState(localStorage.getItem(STORAGE_KEY.token) || '');
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem(STORAGE_KEY.user);
    return stored ? JSON.parse(stored) : null;
  });
  const [isUserValidating, setIsUserValidating] = useState(!!localStorage.getItem(STORAGE_KEY.token));
  const [dbMode, setDbMode] = useState(localStorage.getItem(STORAGE_KEY.dbMode) || 'sql');
  const [page, setPage] = useState('dashboard');
  const [products, setProducts] = useState([]);
  const [users, setUsers] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [productForm, setProductForm] = useState(defaultProductForm);
  const [isEditing, setIsEditing] = useState(false);
  const [quantityChange, setQuantityChange] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');
  const [authMode, setAuthMode] = useState('login');
  const [authForm, setAuthForm] = useState({ name: '', email: '', password: '' });

  useEffect(() => {
    if (!info) {
      return;
    }

    const timer = setTimeout(() => {
      setInfo('');
    }, 30000);

    return () => clearTimeout(timer);
  }, [info]);

  const isAdmin = useMemo(() => user?.role?.toLowerCase() === 'admin', [user]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY.dbMode, dbMode);
  }, [dbMode]);

  useEffect(() => {
    if (!token) {
      setIsUserValidating(false);
      return;
    }

    setIsUserValidating(true);

    validateToken(token)
      .then((userData) => {
        setUser(userData);
        localStorage.setItem(STORAGE_KEY.user, JSON.stringify(userData));
      })
      .catch(() => {
        logout();
      })
      .finally(() => {
        setIsUserValidating(false);
      });
  }, [token]);

  const loadProducts = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const result = await fetchProducts(dbMode);
      setProducts(result);
      setSelectedProduct((prevSelected) => {
        if (!prevSelected) {
          return prevSelected;
        }

        const updatedProduct = result.find((product) => product.id === prevSelected.id);
        return updatedProduct || prevSelected;
      });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [dbMode]);

  const loadUsers = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const result = await fetchUsers(token);
      setUsers(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (token) {
      loadProducts();
      if (isAdmin && page === 'users') {
        loadUsers();
      }
    }
  }, [token, page, isAdmin, loadProducts, loadUsers]);

  const handleAuthChange = (field, value) => {
    setAuthForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    setError('');
    setInfo('');

    try {
      const response = await login(authForm.email, authForm.password);
      localStorage.setItem(STORAGE_KEY.token, response.token);
      setToken(response.token);
      setInfo('Login successful');
      setAuthForm({ name: '', email: '', password: '' });
      setPage('dashboard');
    } catch (err) {
      setError(err.message);
    }
  };

  const handleSignup = async (event) => {
    event.preventDefault();
    setError('');
    setInfo('');

    try {
      await signup(authForm.name, authForm.email, authForm.password);
      setInfo('Signup successful. Please login.');
      setAuthMode('login');
      setAuthForm({ name: '', email: '', password: '' });
    } catch (err) {
      setError(err.message);
    }
  };

  const logout = () => {
    setToken('');
    setUser(null);
    setProducts([]);
    setUsers([]);
    setPage('dashboard');
    localStorage.removeItem(STORAGE_KEY.token);
    localStorage.removeItem(STORAGE_KEY.user);
    setInfo('Logged out successfully');
  };

  const handleDbModeToggle = () => {
    const nextMode = dbMode === 'sql' ? 'nosql' : 'sql';
    setDbMode(nextMode);
    setInfo(`Switched inventory mode to ${nextMode.toUpperCase()}`);
  };

  const openAddProduct = () => {
    setIsEditing(false);
    setProductForm(defaultProductForm);
    setSelectedProduct(null);
    setPage('product-form');
  };

  const openEditProduct = (product) => {
    setIsEditing(true);
    setSelectedProduct(product);
    setProductForm({
      sku: product.sku,
      name: product.name,
      description: product.description || '',
      category: product.category || '',
      quantity: product.quantity || 0,
      location: product.location || '',
      lowStockThreshold: product.lowStockThreshold || 10,
      attributes:
        product.attributes && Object.keys(product.attributes).length
          ? Object.entries(product.attributes).map(([key, value]) => ({ key, value }))
          : [{ key: '', value: '' }],
    });
    setPage('product-form');
  };

  const handleViewProduct = (product) => {
    setSelectedProduct(product);
    setPage('product');
  };

  const handleProductFormChange = (field, value) => {
    setProductForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleAttributeChange = (index, field, value) => {
    setProductForm((prev) => {
      const attributes = [...prev.attributes];
      attributes[index] = { ...attributes[index], [field]: value };
      return { ...prev, attributes };
    });
  };

  const addAttributeRow = () => {
    setProductForm((prev) => ({
      ...prev,
      attributes: [...prev.attributes, { key: '', value: '' }],
    }));
  };

  const removeAttributeRow = (index) => {
    setProductForm((prev) => ({
      ...prev,
      attributes: prev.attributes.filter((_, idx) => idx !== index),
    }));
  };

  const normalizeAttributes = () => {
    return productForm.attributes.reduce((acc, attribute) => {
      const key = attribute.key?.trim();
      const value = attribute.value?.trim();
      if (key) {
        acc[key] = value;
      }
      return acc;
    }, {});
  };

  const handleProductSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setInfo('');

    const payload = {
      sku: productForm.sku,
      name: productForm.name,
      description: productForm.description,
      category: productForm.category,
      quantity: Number(productForm.quantity),
      location: productForm.location,
      lowStockThreshold: Number(productForm.lowStockThreshold),
      attributes: normalizeAttributes(),
    };

    try {
      if (isEditing && selectedProduct) {
        await updateProduct(dbMode, token, selectedProduct.id, payload);
        setInfo('Product updated successfully');
      } else {
        await createProduct(dbMode, token, payload);
        setInfo('Product created successfully');
      }
      setProductForm(defaultProductForm);
      setIsEditing(false);
      setSelectedProduct(null);
      loadProducts();
      setPage('inventory');
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteProduct = async (productId) => {
    if (!window.confirm('Delete this product?')) {
      return;
    }
    setError('');
    setInfo('');
    try {
      await deleteProduct(dbMode, token, productId);
      setInfo('Product deleted successfully');
      loadProducts();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAdjustQuantity = async (productId) => {
    setError('');
    setInfo('');
    try {
      const delta = Number(quantityChange) - (selectedProduct?.quantity ?? 0);
      await adjustQuantity(dbMode, token, productId, { quantityChange: delta, notes: 'Updated from UI' });
      setInfo('Quantity updated successfully');
      loadProducts();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleLoadUsers = async () => {
    setPage('users');
    if (isAdmin) {
      await loadUsers();
    }
  };

  const handleUserRoleUpdate = async (userId, approve) => {
    setError('');
    setInfo('');
    try {
      await updateUserRole(token, userId, approve);
      setInfo('User role updated');
      loadUsers();
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    if (selectedProduct) {
      setQuantityChange(selectedProduct.quantity ?? 0);
    }
  }, [selectedProduct]);

  const handleRequestAdmin = async () => {
    setError('');
    setInfo('');
    try {
      await requestAdminAccess(token);
      setInfo('Admin access requested');
      setUser((prev) => ({ ...prev, adminAccessRequested: true }));
      localStorage.setItem(STORAGE_KEY.user, JSON.stringify({ ...user, adminAccessRequested: true }));
    } catch (err) {
      setError(err.message);
    }
  };

  const lowStockCount = products.filter((product) => product.quantity <= (product.lowStockThreshold ?? 10)).length;
  const recentItems = products.slice(0, 5);


  const inventorySection = (
    <section className="inventory-page">
      <div className="content-header">
        <h2>Inventory</h2>
        <div className="content-actions">
          {isAdmin && (
            <button className="secondary-button" onClick={openAddProduct}>
              Add New Item
            </button>
          )}
        </div>
      </div>

      {loading ? (
        <div className="loading">Loading products…</div>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Category</th>
              <th>Quantity</th>
              <th>Location</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id} className={product.quantity <= (product.lowStockThreshold ?? 10) ? 'low-stock' : ''}>
                <td>{product.name}</td>
                <td>{product.category}</td>
                <td>{product.quantity}</td>
                <td>{product.location || '-'}</td>
                <td className="actions-cell">
                  <button className="link-button" onClick={() => handleViewProduct(product)}>
                    View
                  </button>
                  {isAdmin && (
                    <>
                      <button className="link-button" onClick={() => openEditProduct(product)}>
                        Edit
                      </button>
                      <button className="link-button danger" onClick={() => handleDeleteProduct(product.id)}>
                        Delete
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );

  const productPageSection = (
    <section className="product-detail-page">
      <div className="page-action-bar">
        <button className="secondary-button" onClick={() => setPage('inventory')}>
          ← Back to inventory
        </button>
      </div>
      <ProductDetail
        product={selectedProduct}
        quantityChange={quantityChange}
        setQuantityChange={setQuantityChange}
        handleAdjustQuantity={handleAdjustQuantity}
        isAdmin={isAdmin}
        openEditProduct={openEditProduct}
        handleDeleteProduct={handleDeleteProduct}
      />
    </section>
  );

  const productFormSection = (
    <ProductForm
      isEditing={isEditing}
      productForm={productForm}
      handleProductFormChange={handleProductFormChange}
      handleAttributeChange={handleAttributeChange}
      addAttributeRow={addAttributeRow}
      removeAttributeRow={removeAttributeRow}
      handleProductSubmit={handleProductSubmit}
      resetProductForm={() => setProductForm(defaultProductForm)}
    />
  );

  const usersSection = (
    <section className="users-panel">
      <h2>User Management</h2>
      {loading ? (
        <div className="loading">Loading users…</div>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Admin request</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((userItem) => (
              <tr key={userItem.id}>
                <td>{userItem.name}</td>
                <td>{userItem.email}</td>
                <td>{userItem.role}</td>
                <td>{userItem.adminAccessRequested ? 'Requested' : 'None'}</td>
                <td>
                  {userItem.role?.toLowerCase() !== 'admin' ? (
                    <button className="link-button" onClick={() => handleUserRoleUpdate(userItem.id, true)}>
                      Make Admin
                    </button>
                  ) : (
                    <button className="link-button" onClick={() => handleUserRoleUpdate(userItem.id, false)}>
                      Revoke Admin
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );

  const metricsSection = (
    <section className="metrics-panel">
      <h2>Performance Metrics</h2>
      <div className="metric-grid">
        <div className="metric-card">
          <h3>Active DB</h3>
          <p>{dbMode.toUpperCase()}</p>
        </div>
        <div className="metric-card">
          <h3>Total Items</h3>
          <p>{products.length}</p>
        </div>
        <div className="metric-card">
          <h3>Low Stock</h3>
          <p>{lowStockCount}</p>
        </div>
        <div className="metric-card">
          <h3>Notes</h3>
          <p>Metrics are UI-only until backend metrics are available.</p>
        </div>
      </div>
    </section>
  );

  const profileSection = (
    <section className="profile-panel">
      <h2>Profile</h2>
      <div className="profile-card">
        <p><strong>Name:</strong> {user?.name ?? 'N/A'}</p>
        <p><strong>Email:</strong> {user?.email ?? 'N/A'}</p>
        <p><strong>Role:</strong> {user?.role ?? 'N/A'}</p>
        <p><strong>Admin request:</strong> {user?.adminAccessRequested ? 'Pending' : 'None'}</p>
        {!isAdmin && !user?.adminAccessRequested && (
          <button className="primary-button" onClick={handleRequestAdmin}>
            Request Admin Access
          </button>
        )}
      </div>
    </section>
  );

  const dashboardSection = (
    <section className="dashboard-grid">
      <div className="stat-card">
        <h3>Total Inventory</h3>
        <p>{products.length}</p>
      </div>
      <div className="stat-card">
        <h3>Low Stock Alerts</h3>
        <p>{lowStockCount}</p>
      </div>
      {isAdmin && (
        <div className="stat-card">
          <h3>Total Users</h3>
          <p>{users.length}</p>
        </div>
      )}
      <div className="stat-card">
        <h3>Active DB</h3>
        <p>{dbMode.toUpperCase()}</p>
      </div>
      <div className="dashboard-actions">
        <button className="primary-button" onClick={() => setPage('inventory')}>
          View Inventory
        </button>
        {isAdmin && (
          <button className="secondary-button" onClick={openAddProduct}>
            Add New Item
          </button>
        )}
      </div>
      <div className="recent-card">
        <h3>Recently Added Items</h3>
        {recentItems.length ? (
          <ul>
            {recentItems.map((product) => (
              <li key={product.id}>{product.name}</li>
            ))}
          </ul>
        ) : (
          <p>No inventory yet.</p>
        )}
      </div>
    </section>
  );

  if (!token || isUserValidating) {
    return (
      <div className="auth-shell">
        {error && <div className="badge error">{error}</div>}
        {info && <div className="badge info">{info}</div>}
        {isUserValidating ? <div className="loading">Validating session…</div> : (
          <AuthShell
            authMode={authMode}
            authForm={authForm}
            handleAuthChange={handleAuthChange}
            handleLogin={handleLogin}
            handleSignup={handleSignup}
            setAuthMode={setAuthMode}
          />
        )}
      </div>
    );
  }

  return (
    <div className="app-shell">
      <Sidebar
        page={page}
        setPage={setPage}
        isAdmin={isAdmin}
        logout={logout}
        user={user}
        handleLoadUsers={handleLoadUsers}
      />
      <main className="main-content">
        <div className="top-bar">
          <div>
            <h1>{page.charAt(0).toUpperCase() + page.slice(1)}</h1>
            <p className="top-note">Database mode: {dbMode.toUpperCase()}</p>
          </div>
          {isAdmin && (
            <button className="toggle-button" onClick={handleDbModeToggle}>
              Switch to {dbMode === 'sql' ? 'NoSQL' : 'SQL'}
            </button>
          )}
        </div>

        {error && <div className="badge error">{error}</div>}
        {info && <div className="badge info">{info}</div>}

        {page === 'dashboard' && dashboardSection}
        {page === 'inventory' && inventorySection}
        {page === 'product' && productPageSection}
        {page === 'product-form' && productFormSection}
        {page === 'users' && usersSection}
        {page === 'metrics' && metricsSection}
        {page === 'profile' && profileSection}
      </main>
    </div>
  );
}

export default App;
