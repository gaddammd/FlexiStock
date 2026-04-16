import { useCallback, useEffect, useMemo, useState } from 'react';
import './App.css';
import {
  adjustQuantity,
  createProduct,
  deleteProduct,
  fetchProducts,
  fetchReceipts,
  fetchUsers,
  login,
  requestAdminAccess,
  receiptsUrl,
  signup,
  updateProduct,
  updateUserRole,
  uploadReceipt,
  validateToken,
} from './api';
import AuthShell from './components/AuthShell';
import ProductDetail from './components/ProductDetail';
import ProductForm from './components/ProductForm';
import ReceiptUpload from './components/ReceiptUpload';
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

  const handleAuthChange = (field, value) => {
    setAuthForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    setError('');
    setInfo('');

    try {
      const loginResult = await login(authForm.email, authForm.password);
      const tokenValue = typeof loginResult === 'string' ? loginResult : loginResult?.token;
      if (!tokenValue) {
        throw new Error('Invalid login response');
      }

      setToken(tokenValue);
      localStorage.setItem(STORAGE_KEY.token, tokenValue);

      if (loginResult?.user) {
        setUser(loginResult.user);
        localStorage.setItem(STORAGE_KEY.user, JSON.stringify(loginResult.user));
      }

      setInfo('Logged in successfully');
      setAuthForm({ name: '', email: '', password: '' });
    } catch (err) {
      setError(err.message);
    }
  };

  const [searchQuery, setSearchQuery] = useState('');
  const [sortKey, setSortKey] = useState('name');
  const [sortDirection, setSortDirection] = useState('asc');
  const [filterCategory, setFilterCategory] = useState('all');
  const [filterStock, setFilterStock] = useState('all');
  const [notifications, setNotifications] = useState({});
  const [receipts, setReceipts] = useState([]);
  const [receiptForm, setReceiptForm] = useState({
    file: null,
    storeName: '',
    description: '',
    date: new Date().toISOString().slice(0, 10),
  });

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
  const categories = useMemo(() => {
    return Array.from(new Set(products.map((product) => product.category || '').filter(Boolean))).sort();
  }, [products]);
  const userNotifications = useMemo(() => {
    if (!user?.id) {
      return [];
    }
    return notifications[user.id] || [];
  }, [notifications, user]);

  const displayProducts = useMemo(() => {
    const normalizedQuery = searchQuery.trim().toLowerCase();

    const filtered = products
      .filter((product) => {
        if (!normalizedQuery) {
          return true;
        }

        const haystack = [product.name, product.sku, product.category, product.location, product.description]
          .filter(Boolean)
          .join(' ')
          .toLowerCase();

        return haystack.includes(normalizedQuery);
      })
      .filter((product) => {
        if (filterCategory === 'all') {
          return true;
        }
        return product.category === filterCategory;
      })
      .filter((product) => {
        if (filterStock === 'all') {
          return true;
        }

        const lowStockThreshold = product.lowStockThreshold ?? 10;
        const isLow = product.quantity <= lowStockThreshold;

        if (filterStock === 'low') {
          return isLow;
        }
        if (filterStock === 'in-stock') {
          return !isLow;
        }
        return true;
      });

    return filtered.sort((a, b) => {
      const direction = sortDirection === 'asc' ? 1 : -1;

      if (sortKey === 'quantity') {
        return direction * ((a.quantity ?? 0) - (b.quantity ?? 0));
      }

      const aValue = (a[sortKey] || '').toString().toLowerCase();
      const bValue = (b[sortKey] || '').toString().toLowerCase();
      return direction * aValue.localeCompare(bValue);
    });
  }, [products, searchQuery, filterCategory, filterStock, sortKey, sortDirection]);

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

  const handleNotifyUser = (userId) => {
    setNotifications((prev) => ({
      ...prev,
      [userId]: [
        ...(prev[userId] || []),
        {
          id: Date.now(),
          message: 'You have a package waiting in the suite.',
          receivedAt: new Date().toLocaleString(),
        },
      ],
    }));
    setInfo('Package notification sent to user');
  };

  const handleDismissNotification = (notificationId) => {
    if (!user?.id) {
      return;
    }
    setNotifications((prev) => ({
      ...prev,
      [user.id]: (prev[user.id] || []).filter((note) => note.id !== notificationId),
    }));
    setInfo('Notification removed');
  };

  const handleReceiptFormChange = (field, value) => {
    setReceiptForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleReceiptFileChange = (file) => {
    setReceiptForm((prev) => ({ ...prev, file }));
  };

  const loadReceipts = useCallback(async () => {
    if (!token) {
      return;
    }
    setLoading(true);
    setError('');
    try {
      const result = await fetchReceipts(token, dbMode);
      setReceipts(result || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [token, dbMode]);

  useEffect(() => {
    if (!token) {
      return;
    }
    loadProducts();
  }, [token, dbMode, loadProducts]);

  useEffect(() => {
    if (page !== 'receipts' || !isAdmin) {
      return;
    }
    loadReceipts();
  }, [page, isAdmin, loadReceipts]);

  const handleReceiptSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setInfo('');

    if (!receiptForm.file) {
      setError('Please select a PDF to upload.');
      return;
    }
    if (!receiptForm.storeName.trim()) {
      setError('Please enter the store name.');
      return;
    }

    const formData = new FormData();
    formData.append('file', receiptForm.file);
    formData.append('receiptFile', receiptForm.file);
    formData.append('pdfReceipt', receiptForm.file);
    formData.append('storeName', receiptForm.storeName.trim());
    formData.append('description', receiptForm.description.trim());
    formData.append('receiptDate', receiptForm.date);

    try {
      const uploadedReceipt = await uploadReceipt(token, formData, dbMode);
      setReceipts((prev) => [uploadedReceipt, ...prev]);
      setReceiptForm({
        file: null,
        storeName: '',
        description: '',
        date: new Date().toISOString().slice(0, 10),
      });
      setInfo(uploadedReceipt?.message || 'Receipt uploaded successfully');
    } catch (err) {
      setError(err.message);
    }
  };

    const downloadReceiptFile = async (receiptId) => {
      setError('');
      setLoading(true);
      try {
        const response = await fetch(`${receiptsUrl(dbMode)}/${receiptId}/file`, {
          method: 'GET',
          headers: {
            'X-Auth-Token': token,
          },
        });

        if (!response.ok) {
          const text = await response.text();
          throw new Error(text || response.statusText || 'Failed to download receipt');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
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

  const resetInventoryFilters = () => {
    setSearchQuery('');
    setSortKey('name');
    setSortDirection('asc');
    setFilterCategory('all');
    setFilterStock('all');
  };

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

  const lowStockItems = products.filter((product) => product.quantity <= (product.lowStockThreshold ?? 10));
  const lowStockCount = lowStockItems.length;


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

      <div className="inventory-controls">
        <label className="search-input">
          <span className="search-label">Search inventory</span>
          <div className="search-field">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search name, SKU, category, location"
            />
          </div>
        </label>

        <div className="inventory-action-row">
          <label className="control-chip">
            <span className="chip-icon">🧩</span>
            <select value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)}>
              <option value="all">Category: All</option>
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </label>

          <label className="control-chip">
            <span className="chip-icon">⚠️</span>
            <select value={filterStock} onChange={(e) => setFilterStock(e.target.value)}>
              <option value="all">Stock: All</option>
              <option value="low">Low stock only</option>
              <option value="in-stock">In stock</option>
            </select>
          </label>

          <label className="control-chip">
            <span className="chip-icon">↕️</span>
            <select value={sortKey} onChange={(e) => setSortKey(e.target.value)}>
              <option value="name">Sort: Name</option>
              <option value="category">Sort: Category</option>
              <option value="quantity">Sort: Quantity</option>
              <option value="location">Sort: Location</option>
            </select>
          </label>

          <label className="control-chip">
            <span className="chip-icon">🔽</span>
            <select value={sortDirection} onChange={(e) => setSortDirection(e.target.value)}>
              <option value="asc">Asc</option>
              <option value="desc">Desc</option>
            </select>
          </label>

          <button type="button" className="reset-button small-button" onClick={resetInventoryFilters}>
            Reset
          </button>
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
            {displayProducts.length ? (
              displayProducts.map((product) => (
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
              ))
            ) : (
              <tr>
                <td colSpan="5">No products match your search or filters.</td>
              </tr>
            )}
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
                <td className="actions-cell">
                  <button className="link-button" onClick={() => handleNotifyUser(userItem.id)}>
                    Notify Package
                  </button>
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

  const receiptsSection = (
    <section className="receipt-upload-page">
      <div className="page-action-bar">
        <h2>Receipt Upload</h2>
      </div>
      <form className="receipt-form" onSubmit={handleReceiptSubmit}>
        <label>
          PDF Receipt
          <input
            type="file"
            accept="application/pdf"
            onChange={(e) => handleReceiptFileChange(e.target.files?.[0] || null)}
          />
        </label>
        <label>
          Store name
          <input
            value={receiptForm.storeName}
            onChange={(e) => handleReceiptFormChange('storeName', e.target.value)}
          />
        </label>
        <label>
          Description
          <textarea
            value={receiptForm.description}
            onChange={(e) => handleReceiptFormChange('description', e.target.value)}
          />
        </label>
        <label>
          Receipt date
          <input
            type="date"
            value={receiptForm.date}
            onChange={(e) => handleReceiptFormChange('date', e.target.value)}
          />
        </label>
        <div className="form-actions">
          <button type="submit" className="primary-button">
            Upload receipt
          </button>
        </div>
      </form>

      <div className="receipt-list">
        <h3>Uploaded receipts</h3>
        {receipts.length ? (
          <table className="receipt-table">
            <thead>
              <tr>
                <th>Stored file</th>
                <th>Store</th>
                <th>Description</th>
                <th>Date</th>
                <th>Uploaded</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {receipts.map((receipt) => {
                const fileUrl =
                  receipt.fileUrl ||
                  receipt.url ||
                  receipt.downloadUrl ||
                  receipt.receiptUrl ||
                  receipt.filePath ||
                  receipt.path ||
                  null;
                const fileName = receipt.storedFileName || receipt.fileName || receipt.originalName || 'Receipt file';

                return (
                  <tr key={receipt.id}>
                    <td>{fileName}</td>
                    <td>{receipt.storeName}</td>
                    <td>{receipt.description || '—'}</td>
                    <td>{receipt.date}</td>
                    <td>{receipt.uploadedAt}</td>
                    <td>
                      {receipt.id ? (
                        <button className="link-button" type="button" onClick={() => downloadReceiptFile(receipt.id)}>
                          View
                        </button>
                      ) : fileUrl ? (
                        <a className="link-button" href={fileUrl} target="_blank" rel="noreferrer">
                          View
                        </a>
                      ) : (
                        <span>No file link</span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        ) : (
          <p>No receipts uploaded yet.</p>
        )}
      </div>
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

      {userNotifications.length > 0 && (
        <div className="notification-card">
          <h3>Your notifications</h3>
          <ul>
            {userNotifications.map((note) => (
              <li key={note.id} className="notification-item">
                <div>
                  {note.message} <span className="notification-time">({note.receivedAt})</span>
                </div>
                <button
                  type="button"
                  className="link-button small-button"
                  onClick={() => handleDismissNotification(note.id)}
                >
                  Mark picked up
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}

      <div className="recent-card">
        <h3>Low Stock Items</h3>
        {lowStockItems.length ? (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Category</th>
                <th>Quantity</th>
                <th>Location</th>
              </tr>
            </thead>
            <tbody>
              {lowStockItems.map((product) => (
                <tr key={product.id} className={product.quantity <= (product.lowStockThreshold ?? 10) ? 'low-stock' : ''}>
                  <td>{product.name}</td>
                  <td>{product.category}</td>
                  <td>{product.quantity}</td>
                  <td>{product.location || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>No low stock items right now.</p>
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
        {page === 'receipts' && isAdmin && receiptsSection}
        {page === 'metrics' && metricsSection}
        {page === 'profile' && profileSection}
      </main>
    </div>
  );
}

export default App;
