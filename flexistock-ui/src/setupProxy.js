const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  app.use(
    [
      '/login',
      '/signup',
      '/logout',
      '/auth',
      '/users',
      '/request-admin-access',
      '/update-user-role',
      '/login-history',
    ],
    createProxyMiddleware({
      target: 'http://localhost:8081',
      changeOrigin: true,
    })
  );

  app.use(
    [
      '/api/v1/sql',
      '/api/v1/nosql',
    ],
    createProxyMiddleware({
      target: 'http://localhost:8082',
      changeOrigin: true,
    })
  );
};
