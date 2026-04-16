import React from 'react';

export default function ProductForm({
  isEditing,
  productForm,
  handleProductFormChange,
  handleAttributeChange,
  addAttributeRow,
  removeAttributeRow,
  handleProductSubmit,
  resetProductForm,
}) {
  return (
    <section className="product-form-page">
      <div className="page-action-bar">
        <h2>{isEditing ? 'Edit Product' : 'Add New Product'}</h2>
      </div>
      <form onSubmit={handleProductSubmit} className="product-form">
        <label>
          SKU
          <input
            required
            value={productForm.sku}
            onChange={(e) => handleProductFormChange('sku', e.target.value)}
            disabled={isEditing}
          />
        </label>
        <label>
          Name
          <input
            required
            value={productForm.name}
            onChange={(e) => handleProductFormChange('name', e.target.value)}
          />
        </label>
        <label>
          Category
          <input
            required
            value={productForm.category}
            onChange={(e) => handleProductFormChange('category', e.target.value)}
          />
        </label>
        <label>
          Quantity
          <input
            type="number"
            min="0"
            value={productForm.quantity}
            onChange={(e) => handleProductFormChange('quantity', e.target.value)}
          />
        </label>
        <label>
          Location
          <input
            value={productForm.location}
            onChange={(e) => handleProductFormChange('location', e.target.value)}
          />
        </label>
        <label>
          Low stock threshold
          <input
            type="number"
            min="0"
            value={productForm.lowStockThreshold}
            onChange={(e) => handleProductFormChange('lowStockThreshold', e.target.value)}
          />
        </label>
        <label>
          Description
          <textarea
            value={productForm.description}
            onChange={(e) => handleProductFormChange('description', e.target.value)}
          />
        </label>
        <div className="attributes-section">
          <h3>Dynamic Attributes</h3>
          {productForm.attributes.map((attribute, index) => (
            <div className="attribute-row" key={`attribute-${index}`}>
              <input
                placeholder="Attribute name"
                value={attribute.key}
                onChange={(e) => handleAttributeChange(index, 'key', e.target.value)}
              />
              <input
                placeholder="Attribute value"
                value={attribute.value}
                onChange={(e) => handleAttributeChange(index, 'value', e.target.value)}
              />
              {productForm.attributes.length > 1 && (
                <button type="button" className="small-button danger" onClick={() => removeAttributeRow(index)}>
                  Remove
                </button>
              )}
            </div>
          ))}
          <button type="button" className="secondary-button" onClick={addAttributeRow}>
            Add attribute
          </button>
        </div>
        <div className="form-actions">
          <button type="submit" className="primary-button">
            {isEditing ? 'Save changes' : 'Create product'}
          </button>
          <button type="button" className="secondary-button" onClick={resetProductForm}>
            Reset
          </button>
        </div>
      </form>
    </section>
  );
}
