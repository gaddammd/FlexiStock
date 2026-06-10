# FlexiStock UI

A modern, flexible inventory management system built with React. FlexiStock provides a comprehensive solution for managing products, tracking stock levels, handling receipts, and managing user access with role-based permissions.

## Features

- **Product Management**: Create, update, delete, and manage product inventory
- **Stock Tracking**: Monitor product quantities and set low stock thresholds
- **Receipt Management**: Upload and track purchase receipts
- **User Authentication**: Secure login and signup with token-based authentication
- **Role-Based Access Control**: Admin and user roles with permission management
- **Multi-Database Support**: Flexible database mode configuration
- **Product Attributes**: Add custom attributes to products for better organization
- **Dashboard**: Overview of inventory status and key metrics

## Prerequisites

- Node.js (v14 or higher)
- npm (v6 or higher)

## Getting Started

### Installation

1. Clone the repository or navigate to the project directory:
```bash
cd flexistock-ui
```

2. Install dependencies:
```bash
npm install
```

### Running the Application

Start the development server:
```bash
npm start
```

The application will open in your browser at [http://localhost:3000](http://localhost:3000). The page will reload when you make changes, and you'll see lint errors in the console.

### Building for Production

Create an optimized production build:
```bash
npm run build
```

This builds the app for production to the `build` folder. The build is minified and filenames include hashes for optimal performance.

### Running Tests

Launch the test runner in interactive watch mode:
```bash
npm test
```

## Project Structure

```
src/
├── App.js              # Main application component
├── App.css             # Main styling
├── index.js            # React entry point
├── api/                # API integration and utilities
│   └── index.js        # API endpoints and requests
├── components/         # React components
│   ├── AuthShell.jsx   # Authentication wrapper
│   ├── ProductForm.jsx # Product creation/editing form
│   ├── ProductDetail.jsx # Product detail view
│   ├── ReceiptUpload.jsx # Receipt upload component
│   └── Sidebar.jsx     # Navigation sidebar
└── setupProxy.js       # Proxy configuration for backend API
```

## Key Components

- **AuthShell**: Handles user authentication and session management
- **ProductForm**: Form for creating and editing products with custom attributes
- **ProductDetail**: Displays detailed product information
- **ReceiptUpload**: File upload functionality for receipts
- **Sidebar**: Navigation and user menu

## Configuration

The application uses localStorage to persist user session data:
- `flexistock-token`: Authentication token
- `flexistock-user`: User profile information
- `flexistock-db-mode`: Database mode configuration

## API Integration

The application communicates with a backend API through the `api/index.js` module. Key endpoints include:
- Authentication (login, signup, validation)
- Product operations (CRUD)
- Receipt management (upload, retrieve)
- User management (roles, permissions)

## Development Notes

- The app uses React hooks for state management
- Authentication tokens are stored in localStorage
- The proxy is configured to forward API requests to the backend server
- Custom attributes can be added to products for flexible data modeling

## Learn More

- [Create React App documentation](https://facebook.github.io/create-react-app/docs/getting-started)
- [React documentation](https://reactjs.org/)

## Troubleshooting

If the build fails to minify or you encounter other issues, refer to the [Create React App troubleshooting guide](https://facebook.github.io/create-react-app/docs/troubleshooting).
