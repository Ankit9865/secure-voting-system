import { useEffect, useState } from "react";
import { api } from "../api";

export default function Vote() {
  const [candidates, setCandidates] = useState([]);
  const [hasVoted, setHasVoted] = useState(false);
  const [justVoted, setJustVoted] = useState(false);
  const [selected, setSelected] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    Promise.all([api.candidates(), api.voteStatus()])
      .then(([list, status]) => {
        setCandidates(list);
        setHasVoted(status.hasVoted);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  async function submit(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      await api.vote(selected);
      setHasVoted(true);
      setJustVoted(true);
    } catch (err) {
      setError(err.message);
      if (err.status === 409) setHasVoted(true);
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <p className="status">Loading the ballot…</p>;

  if (hasVoted) {
    return (
      <section className="panel narrow">
        <h1>{justVoted ? "Your vote is recorded" : "You have already voted"}</h1>
        <p>Ballots are anonymous: the system stores your choice without your name attached. Results are shared by the election administrator.</p>
      </section>
    );
  }

  return (
    <section className="panel">
      <h1>Cast your vote</h1>
      <p className="lede">Choose one candidate. You can vote only once, and a recorded vote cannot be changed.</p>
      {candidates.length === 0 ? (
        <p className="status">No candidates have been added yet. Check back soon.</p>
      ) : (
        <form onSubmit={submit}>
          <fieldset className="ballot">
            <legend>Official ballot</legend>
            {candidates.map((c) => (
              <label key={c.id} className={`row ${selected === c.id ? "chosen" : ""}`}>
                <input type="radio" name="candidate" value={c.id} checked={selected === c.id} onChange={() => setSelected(c.id)} />
                <span className="box" aria-hidden="true" />
                <span className="who">
                  <strong>{c.name}</strong>
                  <small>{c.party}</small>
                </span>
              </label>
            ))}
          </fieldset>
          {error && <p className="error" role="alert">{error}</p>}
          <button className="primary" disabled={selected === null || busy}>{busy ? "Recording…" : "Cast vote"}</button>
        </form>
      )}
    </section>
  );
}
