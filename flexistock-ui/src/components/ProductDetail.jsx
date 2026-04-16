import React from 'react';

export default function ProductDetail({
  product,
  quantityChange,
  setQuantityChange,
  handleAdjustQuantity,
  isAdmin,
  openEditProduct,
  handleDeleteProduct,
}) {
  if (!product) {
    return (
      <div className="product-detail-card">
        <h2>No product selected</h2>
        <p>Select a product from inventory to see details and update stock.</p>
      </div>
    );
  }

  return (
    <div className="product-detail-card">
      <div className="product-detail-header">
        <h2>{product.name}</h2>
        <div className="content-actions">
          {isAdmin && (
            <>
              <button className="secondary-button" onClick={() => openEditProduct(product)}>
                Edit
              </button>
              <button className="danger" onClick={() => handleDeleteProduct(product.id)}>
                Delete
              </button>
            </>
          )}
        </div>
      </div>
      <div className="product-detail-grid">
        <div className="product-detail-summary">
          <p><strong>SKU:</strong> {product.sku}</p>
          <p><strong>Category:</strong> {product.category}</p>
          <p><strong>Quantity:</strong> {product.quantity}</p>
          <p><strong>Location:</strong> {product.location || '-'}</p>
          <p><strong>Low stock threshold:</strong> {product.lowStockThreshold}</p>
          <p><strong>Description:</strong> {product.description || 'No description'}</p>
          <div>
            <strong>Attributes:</strong>
            {product.attributes && Object.keys(product.attributes).length ? (
              <ul>
                {Object.entries(product.attributes).map(([key, value]) => (
                  <li key={key}>
                    <strong>{key}:</strong> {value}
                  </li>
                ))}
              </ul>
            ) : (
              <p>No dynamic attributes</p>
            )}
          </div>
        </div>
        <div className="quantity-update">
          <label>
            New quantity
            <input
              type="number"
              value={quantityChange}
              onChange={(e) => setQuantityChange(e.target.value)}
            />
          </label>
          <button className="primary-button" onClick={() => handleAdjustQuantity(product.id)}>
            Save quantity
          </button>
        </div>
      </div>
    </div>
  );
}
