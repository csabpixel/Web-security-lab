document.addEventListener("DOMContentLoaded", () => {

    // TAB KEZELÉS

    const mainTabs = document.querySelectorAll(".main-tab");
    const mainSections = document.querySelectorAll(".main-section");

    mainTabs.forEach(tab => {
        tab.addEventListener("click", () => {
            mainTabs.forEach(t => t.classList.remove("active"));
            mainSections.forEach(section => section.classList.remove("active"));

            tab.classList.add("active");
            const target = document.getElementById(tab.dataset.mainTab);
            if (target) target.classList.add("active");
        });
    });

    const subTabs = document.querySelectorAll(".sub-tab");

    subTabs.forEach(tab => {
        tab.addEventListener("click", () => {
            const parent = tab.closest(".main-section");
            if (!parent) return;

            parent.querySelectorAll(".sub-tab").forEach(t => t.classList.remove("active"));
            parent.querySelectorAll(".sub-section").forEach(s => s.classList.remove("active"));

            tab.classList.add("active");
            const target = document.getElementById(tab.dataset.subTab);
            if (target) target.classList.add("active");
        });
    });

    // SQL INJECTION

    const searchBtn = document.getElementById("searchBtn");
    const queryInput = document.getElementById("query");
    const modeSelect = document.getElementById("mode");
    const resultsBody = document.getElementById("resultsBody");
    const statusBox = document.getElementById("status");
    const explanationBox = document.getElementById("explanation");

    if (searchBtn) {
        searchBtn.addEventListener("click", async () => {
            const query = queryInput.value;
            const mode = modeSelect.value;

            statusBox.textContent = "Lekérdezés...";

            try {
                const response = await fetch("/api/sqli/search", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ query, mode })
                });

                const data = await response.json();

                renderSqliResults(data.results || []);

                explanationBox.innerHTML = `
                    <p><strong>Mód:</strong> ${escapeHtml(data.mode)}</p>
                    <p><strong>Bemenet:</strong> ${escapeHtml(data.input)}</p>
                    <p>${escapeHtml(data.explanation)}</p>
                `;

                statusBox.textContent = "Kész";
            } catch (e) {
                statusBox.textContent = "Hiba";
            }
        });
    }

    function renderSqliResults(results) {
        if (!results.length) {
            resultsBody.innerHTML = `<tr><td colspan="4">Nincs találat</td></tr>`;
            return;
        }

        resultsBody.innerHTML = results.map(u => `
            <tr>
                <td>${u.id}</td>
                <td>${escapeHtml(u.username)}</td>
                <td>${escapeHtml(u.email)}</td>
                <td>${escapeHtml(u.role)}</td>
            </tr>
        `).join("");
    }

    // BLIND SQLI - CONTENT BASED

    const blindContentBtn = document.getElementById("blindContentBtn");
    const blindContentInput = document.getElementById("blindContentInput");
    const blindContentMode = document.getElementById("blindContentMode");
    const blindContentResult = document.getElementById("blindContentResult");
    const blindContentStatus = document.getElementById("blindContentStatus");
    const blindContentExplanation = document.getElementById("blindContentExplanation");

    if (blindContentBtn) {
        blindContentBtn.addEventListener("click", async () => {
            blindContentStatus.textContent = "Lekérdezés...";
            try {
                const res = await fetch("/api/sqli/blind/content", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        input: blindContentInput.value,
                        mode: blindContentMode.value
                    })
                });
                const data = await res.json();

                blindContentResult.textContent = data.message;
                blindContentResult.classList.toggle("hit", !!data.success);
                blindContentExplanation.innerHTML = `
                    <p><strong>Mód:</strong> ${escapeHtml(data.mode)}</p>
                    <p><strong>Bemenet:</strong> ${escapeHtml(data.input)}</p>
                    <p>A válasz <em>tartalma</em> eltér aszerint, hogy a feltétel
                    igaz-e vagy sem — ez jelenti a content-based blind vektort.</p>
                `;
                blindContentStatus.textContent = "Kész";
            } catch (e) {
                blindContentStatus.textContent = "Hiba: " + e.message;
            }
        });
    }

    // BLIND SQLI - TIME BASED

    const blindTimeBtn = document.getElementById("blindTimeBtn");
    const blindTimeInput = document.getElementById("blindTimeInput");
    const blindTimeMode = document.getElementById("blindTimeMode");
    const blindTimeResult = document.getElementById("blindTimeResult");
    const blindTimeStatus = document.getElementById("blindTimeStatus");
    const blindTimeExplanation = document.getElementById("blindTimeExplanation");

    if (blindTimeBtn) {
        blindTimeBtn.addEventListener("click", async () => {
            blindTimeStatus.textContent = "Futtatás...";
            const clientStart = performance.now();
            try {
                const res = await fetch("/api/sqli/blind/time", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        input: blindTimeInput.value,
                        mode: blindTimeMode.value
                    })
                });
                const data = await res.json();
                const clientElapsed = Math.round(performance.now() - clientStart);

                blindTimeResult.innerHTML = `
                    <p><strong>${escapeHtml(data.message)}</strong></p>
                    <p>Szerver által mért válaszidő: <code>${data.responseTimeMs} ms</code></p>
                    <p>Kliens oldali teljes idő: <code>${clientElapsed} ms</code></p>
                    <p>Konstruált SQL: <code>${escapeHtml(data.constructedSql || "")}</code></p>
                `;
                blindTimeExplanation.innerHTML = `
                    <p><strong>Mód:</strong> ${escapeHtml(data.mode)}</p>
                    <p>Vulnerable módban a bemenet közvetlenül a SQL-be kerül,
                    és a H2-ben regisztrált <code>SLEEP(seconds)</code> /
                    <code>SLEEP_MS(ms)</code> alias miatt a payload
                    (pl. <code>' OR SLEEP(3)--</code>) <em>valódi DB-szintű</em>
                    késleltetést okoz. Secure módban paraméteres lekérdezés
                    miatt a SLEEP szöveg literálként kezelődik — nem fut le.</p>
                    <p>A szerver mindkét módban zajt ad a válaszidőhöz, ezért egyetlen
                    kérésből nem feltétlenül egyértelmű, hogy van-e SLEEP.
                    Több kérést küldj, és <strong>az átlagos válaszidőt</strong>
                    hasonlítsd össze — ha a SLEEP-es payloadnál érdemben
                    magasabb, sérülékeny.</p>
                `;
                blindTimeStatus.textContent = "Kész";
            } catch (e) {
                blindTimeStatus.textContent = "Hiba: " + e.message;
            }
        });
    }

    // STORED XSS

    const addCommentBtn = document.getElementById("addCommentBtn");
    const xssModeSelect = document.getElementById("xssMode");
    const authorInput = document.getElementById("author");
    const commentInput = document.getElementById("comment");
    const commentsContainer = document.getElementById("commentsContainer");
    const xssStatus = document.getElementById("xssStatus");

    if (addCommentBtn) {
        addCommentBtn.addEventListener("click", async () => {
            const author = authorInput.value;
            const content = commentInput.value;

            await fetch("/api/xss/comments", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ author, content })
            });

            loadComments();
        });
    }

    async function loadComments() {
        const res = await fetch("/api/xss/comments");
        const comments = await res.json();

        if (xssModeSelect.value === "vulnerable") {
            commentsContainer.innerHTML = comments.map(c => `
                <div>
                    <b>${c.author}</b><br>
                    ${c.content}
                </div>
            `).join("");
        } else {
            commentsContainer.innerHTML = comments.map(c => `
                <div>
                    <b>${escapeHtml(c.author)}</b><br>
                    ${escapeHtml(c.content)}
                </div>
            `).join("");
        }
    }

    if (xssModeSelect) {
        xssModeSelect.addEventListener("change", loadComments);
    }

    loadComments();

    // REFLECTED XSS

    const reflectedBtn = document.getElementById("reflectedBtn");
    const reflectedInput = document.getElementById("reflectedInput");
    const reflectedMode = document.getElementById("reflectedMode");
    const reflectedResult = document.getElementById("reflectedResult");

    if (reflectedBtn) {
        reflectedBtn.addEventListener("click", async () => {
            const res = await fetch("/api/xss/reflected", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    input: reflectedInput.value,
                    mode: reflectedMode.value
                })
            });

            const data = await res.json();

            if (reflectedMode.value === "vulnerable") {
                reflectedResult.innerHTML = data.message;
            } else {
                reflectedResult.textContent = data.message;
            }
        });
    }

// DOM XSS

    const domBtn = document.getElementById("domBtn");
    const domInput = document.getElementById("domInput");
    const domMode = document.getElementById("domMode");
    const domResult = document.getElementById("domResult");
    const domStatus = document.getElementById("domStatus");

    function getHashValue() {
        try {
            return decodeURIComponent(window.location.hash.slice(1) || "");
        } catch (e) {
            return window.location.hash.slice(1) || "";
        }
    }

    function clearHash() {
        if (window.location.hash) {
            history.replaceState(null, "", window.location.pathname + window.location.search);
        }
    }

    function renderDom(value = "") {
        if (domMode.value === "vulnerable") {
            domResult.innerHTML = value;
            domStatus.textContent = "Vulnerable mód — innerHTML-ként renderelődik (a payload lefut).";
        } else {
            domResult.textContent = value;
            domStatus.textContent = "Secure mód — látszik az URL-ben és az inputban, de textContent-ként renderelődik (nem fut le).";
        }
    }

    function syncFromHash() {
        const hashValue = getHashValue();
        domInput.value = hashValue;
        renderDom(hashValue);
    }

    if (domBtn) {
        domBtn.addEventListener("click", () => {
            const value = domInput.value;
            const encoded = encodeURIComponent(value);
            if (window.location.hash === "#" + encoded) {
                renderDom(value);
            } else {
                window.location.hash = encoded;
            }
        });
    }

    if (domMode) {
        domMode.addEventListener("change", () => renderDom(domInput.value));
    }

    window.addEventListener("hashchange", syncFromHash);
    syncFromHash();

    // SEGÉD

    function escapeHtml(text) {
        return String(text)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

});