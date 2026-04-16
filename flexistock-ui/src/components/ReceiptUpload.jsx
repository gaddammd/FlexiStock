import React from 'react';

export default function ReceiptUpload({
  receiptForm,
  handleReceiptFormChange,
  handleReceiptFileChange,
  handleReceiptSubmit,
  receipts,
}) {
  return (
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
                const fileUrl = receipt.fileUrl || receipt.url || receipt.downloadUrl || receipt.receiptUrl || receipt.filePath;
                const fileName = receipt.storedFileName || receipt.originalName || receipt.fileName || 'Receipt file';
                return (
                  <tr key={receipt.id}>
                    <td>
                      {fileUrl ? (
                        <a href={fileUrl} target="_blank" rel="noreferrer">
                          {fileName}
                        </a>
                      ) : (
                        fileName
                      )}
                    </td>
                    <td>{receipt.storeName}</td>
                    <td>{receipt.description || '—'}</td>
                    <td>{receipt.date}</td>
                    <td>{receipt.uploadedAt}</td>
                    <td>
                      {fileUrl ? (
                        <a className="link-button" href={fileUrl} target="_blank" rel="noreferrer">
                          View
                        </a>
                      ) : (
                        <span>Not available</span>
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
}
