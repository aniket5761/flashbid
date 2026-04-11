import { Navigate, Route, Routes } from "react-router-dom";
import AppShell from "./component/AppShell";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Products from "./pages/Products";
import CreateProduct from "./pages/CreateProduct";
import ProductDetail from "./pages/ProductDetail";
import Profile from "./pages/Profile";
import SellerStudio from "./pages/SellerStudio";
import AdminDashboard from "./pages/AdminDashboard";
import Users from "./pages/Users";
import { useAuth } from "./state/AuthContext";


function ProtectedRoute({ children, allowRoles }) {
  const { token, user } = useAuth();
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  if (allowRoles?.length && !allowRoles.includes(user?.role)) {
    return <Navigate to="/" replace />;
  }
  return children;
}

export default function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/products" element={<Products/>} />
        <Route path="/products/:productId" element={<ProductDetail />} />
        <Route
          path="/products/new"
          element={
            <ProtectedRoute allowRoles={["SELLER", "ADMIN"]}>
              <CreateProduct />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/seller"
          element={
            <ProtectedRoute>
              <SellerStudio />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowRoles={["ADMIN"]}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/users"
          element={
            <ProtectedRoute allowRoles={["ADMIN"]}>
              <Users />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppShell>
  );
}
