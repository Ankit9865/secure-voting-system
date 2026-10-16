import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";

export default function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [busy, setBusy] = useState(false);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setError("");
    setFieldErrors({});
    setBusy(true);
    try {
      await api.register(form.fullName, form.email, form.password);
      await login(form.email, form.password);
      navigate("/vote", { replace: true });
    } catch (err) {
      setError(err.message);
      setFieldErrors(err.fieldErrors);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="panel narrow">
      <h1>Create your voter account</h1>
      <form onSubmit={submit} noValidate>
        <label htmlFor="fullName">Full name</label>
        <input id="fullName" name="fullName" autoComplete="name" value={form.fullName} onChange={change} required />
        {fieldErrors.fullName && <p className="error">{fieldErrors.fullName}</p>}

        <label htmlFor="email">Email</label>
        <input id="email" name="email" type="email" autoComplete="username" value={form.email} onChange={change} required />
        {fieldErrors.email && <p className="error">{fieldErrors.email}</p>}

        <label htmlFor="password">Password</label>
        <input id="password" name="password" type="password" autoComplete="new-password" value={form.password} onChange={change} required />
        <p className="hint">8–64 characters with an uppercase letter, a lowercase letter, a number and a symbol.</p>
        {fieldErrors.password && <p className="error">{fieldErrors.password}</p>}

        {error && !Object.keys(fieldErrors).length && <p className="error" role="alert">{error}</p>}
        <button className="primary" disabled={busy}>{busy ? "Creating account…" : "Create account"}</button>
      </form>
      <p className="aside">Already registered? <Link to="/login">Sign in</Link></p>
    </section>
  );
}
