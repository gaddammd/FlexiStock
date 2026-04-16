const usersApiBase = process.env.REACT_APP_USERS_API_BASE_URL || "http://localhost:8081";
const inventoryApiBase = process.env.REACT_APP_INVENTORY_API_BASE_URL || "http://localhost:8082";

async function request(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  const response = await fetch(url, { ...options, headers });
  const text = await response.text();
  let body = null;

  if (text) {
    try {
      body = JSON.parse(text);
    } catch (err) {
      body = { message: text };
    }
  }

  if (!response.ok) {
    const message = body?.message || body?.error || response.statusText;
    throw new Error(message || "Request failed");
  }

  return body;
}

export const login = async (email, password) => {
  return request(`${usersApiBase}/login`, {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
};

export const signup = async (name, email, password) => {
  return request(`${usersApiBase}/signup`, {
    method: "POST",
    body: JSON.stringify({ name, email, password }),
  });
};

export const validateToken = async (token) => {
  return request(`${usersApiBase}/auth/validate`, {
    method: "GET",
    headers: { "X-Auth-Token": token },
  });
};

export const inventoryUrl = (dbMode) => `${inventoryApiBase}/api/v1/${dbMode}`;
export const receiptsUrl = (dbMode) => `${inventoryUrl(dbMode)}/receipts`;

export const fetchProducts = async (dbMode) => {
  return request(`${inventoryUrl(dbMode)}/products`);
};

export const createProduct = async (dbMode, token, product) => {
  return request(`${inventoryUrl(dbMode)}/products`, {
    method: "POST",
    headers: { "X-Auth-Token": token },
    body: JSON.stringify(product),
  });
};

export const updateProduct = async (dbMode, token, productId, product) => {
  return request(`${inventoryUrl(dbMode)}/products/${productId}`, {
    method: "PUT",
    headers: { "X-Auth-Token": token },
    body: JSON.stringify(product),
  });
};

export const adjustQuantity = async (dbMode, token, productId, payload) => {
  return request(`${inventoryUrl(dbMode)}/products/${productId}/quantity`, {
    method: "PATCH",
    headers: { "X-Auth-Token": token },
    body: JSON.stringify(payload),
  });
};

export const deleteProduct = async (dbMode, token, productId) => {
  return request(`${inventoryUrl(dbMode)}/products/${productId}`, {
    method: "DELETE",
    headers: { "X-Auth-Token": token },
  });
};

export const fetchUsers = async (token) => {
  return request(`${usersApiBase}/users`, {
    method: "GET",
    headers: { "X-Auth-Token": token },
  });
};

export const updateUserRole = async (token, userId, approve) => {
  return request(`${usersApiBase}/update-user-role/${userId}`, {
    method: "PUT",
    headers: { "X-Auth-Token": token },
    body: JSON.stringify({ approve }),
  });
};

export const requestAdminAccess = async (token) => {
  return request(`${usersApiBase}/request-admin-access`, {
    method: "POST",
    headers: { "X-Auth-Token": token },
  });
};

export const fetchReceipts = async (token, dbMode) => {
  return request(receiptsUrl(dbMode), {
    method: "GET",
    headers: { "X-Auth-Token": token },
  });
};

export const uploadReceipt = async (token, formData, dbMode) => {
  const response = await fetch(receiptsUrl(dbMode), {
    method: "POST",
    headers: {
      "X-Auth-Token": token,
    },
    body: formData,
  });

  const text = await response.text();
  let body = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch (err) {
      body = { message: text };
    }
  }

  if (!response.ok) {
    const message = body?.message || body?.error || response.statusText;
    throw new Error(message || "Request failed");
  }

  return body;
};
