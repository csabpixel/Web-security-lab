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

    // FELADAT 1 — Auth bypass

    const task1Btn = document.getElementById("task1Btn");
    const task1User = document.getElementById("task1User");
    const task1Pass = document.getElementById("task1Pass");
    const task1Result = document.getElementById("task1Result");
    const task1Status = document.getElementById("task1Status");

    if (task1Btn) {
        task1Btn.addEventListener("click", async () => {
            task1Status.textContent = "Lekérdezés...";
            try {
                const res = await fetch("/api/sqli/tasks/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        username: task1User.value,
                        password: task1Pass.value
                    })
                });
                const data = await res.json();

                let html = `<p><strong>${escapeHtml(data.message)}</strong></p>`;
                if (data.success && data.user) {
                    html += `<p>Beléptél mint: <code>${escapeHtml(data.user.username)}</code> (role: <code>${escapeHtml(data.user.role)}</code>)</p>`;
                }
                html += `<p>Konstruált SQL: <code>${escapeHtml(data.constructedSql || "")}</code></p>`;
                task1Result.innerHTML = html;
                task1Result.classList.toggle("hit", !!data.success);
                task1Status.textContent = "Kész";
            } catch (e) {
                task1Status.textContent = "Hiba: " + e.message;
            }
        });
    }

    // FELADAT 2 — UNION-based extraction

    const task2Btn = document.getElementById("task2Btn");
    const task2Input = document.getElementById("task2Input");
    const task2Result = document.getElementById("task2Result");
    const task2ResultsBody = document.getElementById("task2ResultsBody");
    const task2Status = document.getElementById("task2Status");

    if (task2Btn) {
        task2Btn.addEventListener("click", async () => {
            task2Status.textContent = "Lekérdezés...";
            try {
                const res = await fetch("/api/sqli/tasks/products", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        input: task2Input.value
                    })
                });
                const data = await res.json();

                if (!data.results || data.results.length === 0) {
                    task2ResultsBody.innerHTML = `<tr><td colspan="2" class="empty">Nincs találat.</td></tr>`;
                } else {
                    task2ResultsBody.innerHTML = data.results.map(r => `
                        <tr>
                            <td>${escapeHtml(String(r.col1 ?? ""))}</td>
                            <td>${escapeHtml(String(r.col2 ?? ""))}</td>
                        </tr>
                    `).join("");
                }

                task2Result.innerHTML = `
                    <p><strong>${escapeHtml(data.message)}</strong></p>
                    <p>Konstruált SQL: <code>${escapeHtml(data.constructedSql || "")}</code></p>
                `;
                task2Status.textContent = "Kész";
            } catch (e) {
                task2Status.textContent = "Hiba: " + e.message;
            }
        });
    }

    // FELADAT 3 — Numeric

    const task3Btn = document.getElementById("task3Btn");
    const task3Input = document.getElementById("task3Input");
    const task3ResultsBody = document.getElementById("task3ResultsBody");
    const task3Status = document.getElementById("task3Status");

    if (task3Btn) {
        task3Btn.addEventListener("click", async () => {
            task3Status.textContent = "Lekérdezés...";
            try {
                const res = await fetch("/api/sqli/tasks/price-search", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ input: task3Input.value })
                });
                const data = await res.json();

                if (!data.results || data.results.length === 0) {
                    task3ResultsBody.innerHTML = `<tr><td colspan="2" class="empty">Nincs találat.</td></tr>`;
                } else {
                    task3ResultsBody.innerHTML = data.results.map(r => `
                        <tr>
                            <td>${escapeHtml(String(r.col1 ?? ""))}</td>
                            <td>${escapeHtml(String(r.col2 ?? ""))}</td>
                        </tr>
                    `).join("");
                }

                task3Status.textContent = "Kész";
            } catch (e) {
                task3Status.textContent = "Hiba: " + e.message;
            }
        });
    }

    // CSRF DEMÓ

    const csrfMode = document.getElementById("csrfMode");
    const csrfResetBtn = document.getElementById("csrfResetBtn");
    const csrfUsername = document.getElementById("csrfUsername");
    const csrfEmail = document.getElementById("csrfEmail");
    const csrfToken = document.getElementById("csrfToken");
    const csrfHistory = document.getElementById("csrfHistory");
    const csrfStatus = document.getElementById("csrfStatus");
    const csrfLegitBtn = document.getElementById("csrfLegitBtn");
    const csrfLegitEmail = document.getElementById("csrfLegitEmail");
    const csrfEvilBtn = document.getElementById("csrfEvilBtn");

    function csrfRenderState(data) {
        if (!data) return;
        if (csrfUsername) csrfUsername.textContent = data.username ?? "Te";
        if (csrfEmail) csrfEmail.textContent = data.email ?? "—";
        if (csrfToken) csrfToken.textContent = data.csrfToken ?? "—";
        const hist = data.history || [];
        if (csrfHistory) {
            if (hist.length === 0) {
                csrfHistory.innerHTML = `<li style="color: var(--muted);">(még nincs változás)</li>`;
            } else {
                csrfHistory.innerHTML = hist.map(h => `<li>${escapeHtml(h)}</li>`).join("");
            }
        }
    }

    async function csrfLoadState() {
        try {
            const res = await fetch("/api/csrf/state");
            const data = await res.json();
            csrfRenderState(data);
        } catch (e) {

        }
    }

    if (csrfResetBtn) {
        csrfResetBtn.addEventListener("click", async () => {
            csrfStatus.textContent = "Reset...";
            const res = await fetch("/api/csrf/reset", { method: "POST" });
            const data = await res.json();
            csrfRenderState(data);
            csrfStatus.textContent = "Profil visszaállítva, új CSRF token generálva.";
        });
    }

    if (csrfLegitBtn) {
        csrfLegitBtn.addEventListener("click", async () => {
            const newEmail = csrfLegitEmail.value || "uj@email.hu";
            const mode = csrfMode.value;

            csrfStatus.textContent = "Email csere (" + mode + " módban)...";

            if (mode === "vulnerable") {
                const res = await fetch(`/api/csrf/change-email?email=${encodeURIComponent(newEmail)}`);
                const data = await res.json();
                csrfRenderState(data);
                csrfStatus.textContent = "Email csere (vulnerable mód, GET) — " + data.message;
            } else {
                const token = csrfToken.textContent;
                const res = await fetch("/api/csrf/change-email-secure", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ email: newEmail, csrfToken: token })
                });
                const data = await res.json();
                csrfRenderState(data);
                csrfStatus.textContent = data.success
                    ? "Email csere sikeres (secure mód, POST + CSRF token)."
                    : "Hiba: " + data.message;
            }
        });
    }

    const csrfEvilCode = document.getElementById("csrfEvilCode");
    const csrfEvilRender = document.getElementById("csrfEvilRender");

    if (csrfEvilBtn) {
        csrfEvilBtn.addEventListener("click", async () => {
            const code = csrfEvilCode ? csrfEvilCode.value : "";
            const emailBefore = csrfEmail ? csrfEmail.textContent : "";
            csrfStatus.textContent = "Evil oldal betöltése — a HTML-ed renderelődik...";

            csrfEvilRender.innerHTML = code;

            await new Promise(r => setTimeout(r, 600));
            await csrfLoadState();

            const emailAfter = csrfEmail ? csrfEmail.textContent : "";
            if (emailBefore !== emailAfter) {
                csrfStatus.textContent = "CSRF támadás sikeres! Az emailed " + emailBefore + " → " + emailAfter + " (vulnerable módban a GET endpoint elfogadja a kérést, mert a böngésző rácsatolta a session cookie-dat).";
            } else {
                csrfStatus.textContent = "️Az email nem változott. (Secure módban a GET endpoint le van tiltva, így a klasszikus <img>-trükk nem működik.)";
            }
        });
    }

    async function csrfSyncMode() {
        const mode = csrfMode.value;
        await fetch("/api/csrf/set-mode", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ mode })
        });
        await csrfLoadState();
        csrfStatus.textContent = "Mód váltva: " + mode + (mode === "secure" ? " — a backend most már elutasítja az állapot-módosító GET kéréseket." : " — a GET endpoint nyitva, sebezhető.");
    }

    if (csrfMode) {
        csrfMode.addEventListener("change", csrfSyncMode);
    }

    csrfLoadState();

    // PIZZA SHOP

    const pizzaLoginView = document.getElementById("pizzaLoginView");
    const pizzaAppView = document.getElementById("pizzaAppView");
    const pizzaLoginUser = document.getElementById("pizzaLoginUser");
    const pizzaLoginPass = document.getElementById("pizzaLoginPass");
    const pizzaLoginBtn = document.getElementById("pizzaLoginBtn");
    const pizzaLoginStatus = document.getElementById("pizzaLoginStatus");
    const pizzaLogoutBtn = document.getElementById("pizzaLogoutBtn");
    const pizzaUsernameEl = document.getElementById("pizzaUsername");

    function pizzaShowLoggedIn(username) {
        if (pizzaLoginView) pizzaLoginView.classList.add("pizza-hidden");
        if (pizzaAppView) pizzaAppView.classList.remove("pizza-hidden");
        if (pizzaUsernameEl) pizzaUsernameEl.textContent = username || "—";
        if (pizzaLoginStatus) {
            pizzaLoginStatus.textContent = "";
            pizzaLoginStatus.className = "pizza-status";
        }
        pizzaLoadMenu();
        pizzaRefreshBalance();
    }

    const pizzaBalanceEl = document.getElementById("pizzaBalance");
    const pizzaMenuPizzas = document.getElementById("pizzaMenuPizzas");
    const pizzaMenuDrinks = document.getElementById("pizzaMenuDrinks");
    const pizzaOrders = document.getElementById("pizzaOrders");
    const pizzaOrderStatus = document.getElementById("pizzaOrderStatus");

    let pizzaMenuCache = null;

    async function pizzaLoadMenu() {
        if (pizzaMenuCache) {
            pizzaRenderMenu(pizzaMenuCache);
            return;
        }
        try {
            const res = await fetch("/api/pizza/menu");
            const data = await res.json();
            pizzaMenuCache = data;
            pizzaRenderMenu(data);
        } catch (e) { }
    }

    function pizzaRenderMenu(items) {
        if (!pizzaMenuPizzas || !pizzaMenuDrinks) return;
        pizzaMenuPizzas.innerHTML = "";
        pizzaMenuDrinks.innerHTML = "";
        items.forEach(item => {
            const card = document.createElement("div");
            card.className = "pizza-item";
            card.innerHTML = `
                <img class="pizza-item-img" src="${item.image}" alt="${escapeHtml(item.name)}">
                <div class="pizza-item-body">
                    <div class="pizza-item-name">${escapeHtml(item.name)}</div>
                    <div class="pizza-item-price">${item.price} Ft</div>
                    <button class="pizza-item-btn" data-item-id="${item.id}">Rendelés</button>
                </div>
            `;
            const btn = card.querySelector("button");
            btn.addEventListener("click", () => pizzaOrder(item.id));
            (item.category === "pizza" ? pizzaMenuPizzas : pizzaMenuDrinks).appendChild(card);
        });
    }

    async function pizzaRefreshBalance() {
        try {
            const res = await fetch("/api/pizza/balance");
            const data = await res.json();
            if (!data.loggedIn) {
                pizzaShowLoggedOut();
                return;
            }
            if (pizzaBalanceEl) pizzaBalanceEl.textContent = data.balance;
            pizzaRenderOrders(data.orders || []);
        } catch (e) { }
    }

    function pizzaRenderOrders(list) {
        if (!pizzaOrders) return;
        if (list.length === 0) {
            pizzaOrders.className = "pizza-orders empty";
            pizzaOrders.innerHTML = "<li>(még nincs rendelésed)</li>";
            return;
        }
        pizzaOrders.className = "pizza-orders";
        pizzaOrders.innerHTML = list.map(o => `<li>${escapeHtml(o)}</li>`).join("");
    }

    async function pizzaOrder(itemId) {
        if (pizzaOrderStatus) {
            pizzaOrderStatus.textContent = "Rendelés...";
            pizzaOrderStatus.className = "pizza-status";
        }
        try {
            const res = await fetch(`/api/pizza/order?item=${encodeURIComponent(itemId)}`);
            const data = await res.json();
            if (pizzaOrderStatus) {
                pizzaOrderStatus.textContent = data.message || "";
                pizzaOrderStatus.className = "pizza-status " + (data.success ? "success" : "error");
            }
            if (typeof data.balance === "number" && pizzaBalanceEl) {
                pizzaBalanceEl.textContent = data.balance;
            }
            await pizzaRefreshBalance();
        } catch (e) {
            if (pizzaOrderStatus) {
                pizzaOrderStatus.textContent = "Hálózati hiba: " + e.message;
                pizzaOrderStatus.className = "pizza-status error";
            }
        }
    }

    function pizzaShowLoggedOut() {
        if (pizzaLoginView) pizzaLoginView.classList.remove("pizza-hidden");
        if (pizzaAppView) pizzaAppView.classList.add("pizza-hidden");
        if (pizzaLoginUser) pizzaLoginUser.value = "";
        if (pizzaLoginPass) pizzaLoginPass.value = "";
    }

    async function pizzaCheckMe() {
        try {
            const res = await fetch("/api/pizza/me");
            const data = await res.json();
            if (data.loggedIn) pizzaShowLoggedIn(data.username);
            else pizzaShowLoggedOut();
        } catch (e) {  }
    }

    if (pizzaLoginBtn) {
        pizzaLoginBtn.addEventListener("click", async () => {
            const username = pizzaLoginUser.value.trim();
            const password = pizzaLoginPass.value;
            if (!username || !password) {
                pizzaLoginStatus.textContent = "Add meg a felhasználónevet és jelszót.";
                pizzaLoginStatus.className = "pizza-status error";
                return;
            }
            pizzaLoginStatus.textContent = "Belépés...";
            pizzaLoginStatus.className = "pizza-status";
            try {
                const res = await fetch("/api/pizza/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ username, password })
                });
                const data = await res.json();
                if (data.success) {
                    pizzaShowLoggedIn(data.username);
                } else {
                    pizzaLoginStatus.textContent = data.message || "Sikertelen belépés.";
                    pizzaLoginStatus.className = "pizza-status error";
                }
            } catch (e) {
                pizzaLoginStatus.textContent = "Hálózati hiba: " + e.message;
                pizzaLoginStatus.className = "pizza-status error";
            }
        });
    }

    if (pizzaLogoutBtn) {
        pizzaLogoutBtn.addEventListener("click", async () => {
            try {
                await fetch("/api/pizza/logout", { method: "POST" });
            } catch (e) {}
            pizzaShowLoggedOut();
        });
    }

    if (pizzaLoginPass) {
        pizzaLoginPass.addEventListener("keypress", (e) => {
            if (e.key === "Enter") pizzaLoginBtn.click();
        });
    }

    pizzaCheckMe();

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