import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Vote from "./pages/Vote";
import Admin from "./pages/Admin";

const homeFor = (user) => (!user ? "/login" : user.role === "ADMIN" ? "/admin" : "/vote");

// UI guard only. The real enforcement is on the server; this just avoids showing pages
// that the API would reject anyway.
function RequireRole({ role, children }) {
  const { user, loading } = useAuth();
  if (loading) return <p className="status">Loading…</p>;
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== role) return <Navigate to={homeFor(user)} replace />;
  return children;
}

function GuestOnly({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <p className="status">Loading…</p>;
  return user ? <Navigate to={homeFor(user)} replace /> : children;
}

function Header() {
  const { user, logout } = useAuth();
  return (
    <header className="topbar">
      <span className="brand">Open Ballot</span>
      {user && (
        <div className="who">
          <span>{user.fullName}</span>
          <button className="link" onClick={logout}>Sign out</button>
        </div>
      )}
    </header>
  );
}

export default function App() {
  const { user, loading } = useAuth();
  return (
    <>
      <Header />
      <main>
        <Routes>
          <Route path="/" element={loading ? <p className="status">Loading…</p> : <Navigate to={homeFor(user)} replace />} />
          <Route path="/login" element={<GuestOnly><Login /></GuestOnly>} />
          <Route path="/register" element={<GuestOnly><Register /></GuestOnly>} />
          <Route path="/vote" element={<RequireRole role="USER"><Vote /></RequireRole>} />
          <Route path="/admin" element={<RequireRole role="ADMIN"><Admin /></RequireRole>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  );
}
