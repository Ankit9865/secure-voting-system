import { useCallback, useEffect, useState } from "react";
import { api } from "../api";

export default function Admin() {
  const [stats, setStats] = useState(null);
  const [results, setResults] = useState([]);
  const [form, setForm] = useState({ name: "", party: "" });
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  const load = useCallback(async () => {
    try {
      const [s, r] = await Promise.all([api.stats(), api.results()]);
      setStats(s);
      setResults(r);
    } catch (err) {
      setError(err.message);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  async function add(e) {
    e.preventDefault();
    setError("");
    setFieldErrors({});
    try {
      await api.createCandidate(form.name, form.party);
      setForm({ name: "", party: "" });
      await load();
    } catch (err) {
      setError(err.message);
      setFieldErrors(err.fieldErrors);
    }
  }

  async function remove(c) {
    if (!window.confirm(`Remove ${c.name} from the ballot? Votes already cast are kept.`)) return;
    setError("");
    try {
      await api.removeCandidate(c.candidateId);
      await load();
    } catch (err) {
      setError(err.message);
    }
  }

  const total = stats?.votesCast ?? 0;

  return (
    <section className="panel wide">
      <h1>Election results</h1>

      {stats && (
        <dl className="stats">
          <div><dt>Registered voters</dt><dd>{stats.registeredVoters}</dd></div>
          <div><dt>Votes cast</dt><dd>{stats.votesCast}</dd></div>
          <div><dt>Turnout</dt><dd>{stats.turnoutPercent}%</dd></div>
        </dl>
      )}

      {error && <p className="error" role="alert">{error}</p>}

      <table className="results">
        <thead>
          <tr><th scope="col">Candidate</th><th scope="col">Votes</th><th scope="col"><span className="sr">Actions</span></th></tr>
        </thead>
        <tbody>
          {results.length === 0 && (
            <tr><td colSpan="3" className="status">No candidates yet. Add the first one below.</td></tr>
          )}
          {results.map((c) => (
            <tr key={c.candidateId} className={c.active ? "" : "removed"}>
              <td>
                <strong>{c.name}</strong> <small>{c.party}{c.active ? "" : " · removed from ballot"}</small>
                <div className="bar"><span style={{ width: total ? `${(c.votes / total) * 100}%` : "0%" }} /></div>
              </td>
              <td className="num">{c.votes}</td>
              <td>{c.active && <button className="link danger" onClick={() => remove(c)}>Remove</button>}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2>Add a candidate</h2>
      <form onSubmit={add} className="inline">
        <div>
          <label htmlFor="name">Name</label>
          <input id="name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          {fieldErrors.name && <p className="error">{fieldErrors.name}</p>}
        </div>
        <div>
          <label htmlFor="party">Party or affiliation</label>
          <input id="party" value={form.party} onChange={(e) => setForm({ ...form, party: e.target.value })} required />
          {fieldErrors.party && <p className="error">{fieldErrors.party}</p>}
        </div>
        <button className="primary">Add candidate</button>
      </form>
    </section>
  );
}
