// Point this at your deployed Render/Railway URL after backend deploy.
// Left as localhost for local testing; update before deploying to Netlify.
const API_BASE = window.MINISEARCH_API_BASE || "https://minisearchengine-bpkz.onrender.com";

const form = document.getElementById("search-form");
const input = document.getElementById("query-input");
const resultsEl = document.getElementById("results");
const lookupCode = document.getElementById("lookup-code");
const modeButtons = document.querySelectorAll(".mode-btn");
const chips = document.querySelectorAll(".chip");

let mode = "single"; // "single" | "all"

modeButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    modeButtons.forEach((b) => {
      b.classList.remove("active");
      b.setAttribute("aria-selected", "false");
    });
    btn.classList.add("active");
    btn.setAttribute("aria-selected", "true");
    mode = btn.dataset.mode;
    input.placeholder = mode === "single" ? "try: hashmap" : "try: time complexity";
  });
});

chips.forEach((chip) => {
  chip.addEventListener("click", () => {
    input.value = chip.dataset.query;
    runSearch(chip.dataset.query);
  });
});

form.addEventListener("submit", (e) => {
  e.preventDefault();
  const query = input.value.trim();
  if (query) runSearch(query);
});

async function runSearch(query) {
  setStatus("Waking up the server (first request can take up to a minute on a cold start)…");

  try {
    if (mode === "single") {
      await runSingleSearch(query);
    } else {
      await runAllSearch(query);
    }
  } catch (err) {
    setStatus("Couldn't reach the search server. It may still be starting up — try again in a few seconds.", true);
    lookupCode.textContent = `// request failed: ${err.message}`;
  }
}

async function runSingleSearch(keyword) {
  const url = `${API_BASE}/search?keyword=${encodeURIComponent(keyword)}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`status ${res.status}`);
  const data = await res.json();

  lookupCode.textContent =
    `index.get("${keyword.toLowerCase()}")\n` +
    (data.length
      ? `→ { ${data.map((d) => `"${d.file}": ${d.frequency}`).join(", ")} }`
      : `→ null  // word not in index`);

  renderSingleResults(data, keyword);
}

async function runAllSearch(keywordsRaw) {
  const url = `${API_BASE}/searchAll?keywords=${encodeURIComponent(keywordsRaw)}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`status ${res.status}`);
  const data = await res.json();

  const keywords = keywordsRaw.trim().split(/\s+/);
  lookupCode.textContent =
    keywords.map((k) => `index.get("${k.toLowerCase()}").keySet()`).join("\n∩ ") +
    `\n→ [ ${data.map((f) => `"${f}"`).join(", ")} ]`;

  renderAllResults(data);
}

function renderSingleResults(data, keyword) {
  if (!data.length) {
    resultsEl.innerHTML = `<div class="no-results">No file contains "<strong>${escapeHtml(keyword)}</strong>".</div>`;
    return;
  }

  resultsEl.innerHTML = data
    .map(
      (item) => `
      <div class="result-card">
        <span class="result-file">${escapeHtml(item.file)}</span>
        <span class="result-freq">${item.frequency}× match${item.frequency === 1 ? "" : "es"}</span>
      </div>`
    )
    .join("");
}

function renderAllResults(files) {
  if (!files.length) {
    resultsEl.innerHTML = `<div class="no-results">No single file contains all of those keywords.</div>`;
    return;
  }

  resultsEl.innerHTML = files
    .map(
      (file) => `
      <div class="result-card">
        <span class="result-file">${escapeHtml(file)}</span>
        <span class="result-freq">all keywords present</span>
      </div>`
    )
    .join("");
}

function setStatus(message, isError = false) {
  resultsEl.innerHTML = `<div class="status-msg${isError ? " error" : ""}">${escapeHtml(message)}</div>`;
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}
