(ns beverageops.render-html
  "Build-time HTML renderer for `docs/samples/operator-console.html`.

  Closes flagship checklist item 2 (com-junkawasaki/root ADR-2607189300 /
  ADR-2608090800, Wave 0 Lane A): this repo previously had NO demo page
  and no generator at all. This namespace drives the REAL actor stack
  (`beverageops.operation` -> `beverageops.governor` ->
  `beverageops.store`) through a scenario adapted from this repo's own
  `beverageops.sim` demo driver and `beverageops.governor` HARD-hold
  rules, rendered deterministically -- no invented numbers, no timestamps
  in the page content, byte-identical across reruns against the same seed.

  Note on the operation surface: `beverageops.operation/build` is still
  the documented synchronous stub (see its docstring; langgraph StateGraph
  wiring is deferred, mirroring the pre-upgrade state of sibling
  `orchardops`/`berrynutops`). The stub is the real composition of
  advisor -> governor -> phase gate -> commit|escalate|hold for this
  repo; every disposition and every violation rule rendered below is the
  live return value of that composition, never a hand-typed mockup.

  Usage: `clojure -M:dev:render-html [out-file]`
  (default `docs/samples/operator-console.html`)."
  (:require [clojure.string :as str]
            [beverageops.store :as store]
            [beverageops.operation :as op]))

(def ^:private operator
  {:actor-id "beverage-ops-01"
   :role     :plantation-operator
   :phase    :phase-3})

(defn- exec-op!
  "Invoke the real OperationActor once. Appends every non-advisor audit
  fact (commit / hold / approval-requested) onto `ledger-atom` so the
  rendered page is built from real governor/phase output only."
  [actor ledger-atom request]
  (let [result (actor request operator)
        facts  (->> (:audit result)
                    (remove #(= :advisor-proposal (:t %)))
                    vec)]
    (swap! ledger-atom into facts)
    result))

(defn run-demo!
  "Seeds plantation-001 (a registered beverage-crop block) and runs a
  scenario mixing every disposition this actor can reach against real
  governor/phase rules:

    * plantation-001 :log-plantation-record  (clean, phase-3)  -> auto-commit
    * plantation-001 :schedule-field-operation (clean, phase-3) -> auto-commit
    * plantation-001 :flag-crop-health-concern -> always escalate (human)
    * plantation-001 :operate-field-equipment  -> HARD hold
      (`:field-equipment-or-spray-blocked`, permanent, never human)
    * plantation-ghost :log-plantation-record  -> HARD hold
      (`:plantation-not-registered`)
    * plantation-001 :log-plantation-record count 0 -> HARD hold
      (`:plantation-count-invalid`)

  Returns `{:store db :ledger facts}` -- every field the renderer reads
  is real actor output, not a hand-typed copy."
  []
  (let [db (store/mem-store
            {:initial-plantations
             {"plantation-001"
              {:id "plantation-001"
               :name "Test Plantation Block"
               :beverage-crop-class "coffee"}
              "plantation-002"
              {:id "plantation-002"
               :name "East Ridge Tea Block"
               :beverage-crop-class "tea"}}})
        actor  (op/build db)
        ledger (atom [])]
    (exec-op! actor ledger
              {:op :log-plantation-record
               :plantation-id "plantation-001"
               :record-type "harvest-yield"
               :count 500
               :notes "healthy yield"})
    (exec-op! actor ledger
              {:op :schedule-field-operation
               :plantation-id "plantation-001"
               :requested-date "2026-08-01"
               :operation-type "pruning"})
    (exec-op! actor ledger
              {:op :flag-crop-health-concern
               :plantation-id "plantation-001"
               :concern "coffee-borer-suspected"})
    (exec-op! actor ledger
              {:op :operate-field-equipment
               :plantation-id "plantation-001"})
    (exec-op! actor ledger
              {:op :log-plantation-record
               :plantation-id "plantation-ghost"
               :record-type "harvest-yield"
               :count 500})
    (exec-op! actor ledger
              {:op :log-plantation-record
               :plantation-id "plantation-001"
               :record-type "harvest-yield"
               :count 0})
    {:store db :ledger @ledger}))

;; ----------------------------- rendering -----------------------------

(defn- esc [v]
  (-> (str v)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")))

(defn- hold-rule [f]
  (or (some-> f :basis first)
      (some-> f :violations first :rule)))

(defn- last-fact-for [ledger plantation-id]
  (last (filter #(= (:subject %) plantation-id) ledger)))

(defn- status-cell [ledger plantation-id]
  (let [f (last-fact-for ledger plantation-id)]
    (cond
      (nil? f) "<span class=\"muted\">no activity</span>"
      (= :committed (:t f)) "<span class=\"ok\">committed</span>"
      (= :approval-granted (:t f)) "<span class=\"ok\">approved &amp; committed</span>"
      (= :governor-hold (:t f))
      (str "<span class=\"critical\">HARD hold &middot; "
           (esc (name (or (hold-rule f) :unknown)))
           "</span>")
      (= :approval-requested (:t f)) "<span class=\"warn\">awaiting approval</span>"
      :else "<span class=\"muted\">in progress</span>")))

(defn- plantation-row [db ledger plantation-id]
  (let [p (store/registered-plantation db plantation-id)]
    (format "        <tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>"
            (esc plantation-id)
            (esc (or (:name p) "(unregistered)"))
            (esc (or (:beverage-crop-class p) "—"))
            (status-cell ledger plantation-id))))

(defn- ledger-row [{:keys [t op subject disposition basis]}]
  (format "        <tr><td>%s</td><td><code>%s</code></td><td>%s</td><td>%s</td></tr>"
          (esc (name t))
          (esc (name (or op :n-a)))
          (esc subject)
          (esc (or (some->> basis (map name) (str/join ", "))
                   (some-> disposition name)
                   ""))))

(def ^:private action-gate-rows
  ;; Static description of this actor's own closed op contract
  ;; (README `Operational requests`, `beverageops.governor` /
  ;; `beverageops.phase`) -- documentation of fixed behavior, not runtime
  ;; telemetry, so it is legitimately hand-described rather than derived
  ;; from a live run.
  ["        <tr><td><code>:log-plantation-record</code></td><td><span class=\"ok\">phase-3 auto-commit when clean + registered + positive count</span></td></tr>"
   "        <tr><td><code>:schedule-field-operation</code></td><td><span class=\"ok\">phase-3 auto-commit when clean + registered</span></td></tr>"
   "        <tr><td><code>:flag-crop-health-concern</code></td><td><span class=\"warn\">ALWAYS human approval (crop safety) · never auto at any phase</span></td></tr>"
   "        <tr><td><code>:order-supplies</code></td><td><span class=\"warn\">escalates above category cost threshold</span></td></tr>"
   "        <tr><td><code>:operate-field-equipment</code></td><td><span class=\"critical\">HARD permanent block · grower exclusive</span></td></tr>"
   "        <tr><td><code>:finalize-spray-application</code></td><td><span class=\"critical\">HARD permanent block · agronomist exclusive</span></td></tr>"])

(defn render
  "Renders the full operator-console.html document from a demo result
  (`{:store db :ledger facts}`) produced by `run-demo!`."
  [{:keys [store ledger]}]
  (let [plantation-ids ["plantation-001" "plantation-002" "plantation-ghost"]
        plantation-rows (str/join "\n" (map (partial plantation-row store ledger) plantation-ids))
        ledger-rows (str/join "\n" (map ledger-row ledger))]
    (str
     "<html><head><meta charset=\"utf-8\"><title>cloud-itonami-isic-0127 &middot; beverage crop growing ops</title><style>"
     "body{font:14px/1.5 -apple-system,system-ui,sans-serif;margin:0;color:#1a1a1a;background:#f5f5f5}"
     ".bar{background:#1a2a0a;color:#fff;padding:1.2rem 2rem}.bar h1{margin:0;font-size:1.15rem;font-weight:600}"
     ".badge{display:inline-block;margin-top:.4rem;font-size:.75rem;opacity:.8}"
     "main{max-width:980px;margin:1.5rem auto;padding:0 1rem}"
     ".card{background:#fff;border-radius:8px;padding:1.2rem 1.4rem;margin-bottom:1.2rem;box-shadow:0 1px 3px rgba(0,0,0,.08)}"
     ".card h2{margin-top:0;font-size:1rem}.muted{color:#777;font-size:.82rem}"
     "table{border-collapse:collapse;width:100%;font-size:.85rem}th,td{text-align:left;padding:.42rem .5rem;border-bottom:1px solid #eee}th{font-weight:600;color:#555}"
     ".ok{color:#0a7d33}.warn{color:#9a6700}.critical{color:#b41010;font-weight:600}code{background:#f0f0f0;padding:.1rem .3rem;border-radius:3px;font-size:.8rem}"
     "</style></head><body>\n"
     "<header class=\"bar\">\n"
     "  <h1>Beverage crop growing ops (ISIC 0127) — Operator Console</h1>\n"
     "  <span class=\"badge\">read-only sample · governor-gated · field-equipment/spray permanently blocked · crop-health always human-approved · unregistered blocks HARD-blocked</span>\n"
     "</header>\n"
     "<main>\n"
     "  <section class=\"card\">\n"
     "    <h2>Scenario plantation blocks</h2>\n"
     "    <p class=\"muted\">Demo snapshot — build-time-generated from <code>beverageops.store</code> via <code>beverageops.render-html</code> (<code>clojure -M:dev:render-html</code>), regenerated from the real actor. No invented data.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Plantation</th><th>Name</th><th>Crop class</th><th>Last op status</th></tr></thead>\n"
     "      <tbody>\n"
     plantation-rows "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Action gate (Beverage Operations Governor)</h2>\n"
     "    <p class=\"muted\">HARD holds cannot be overridden. Direct field-equipment operation and spray-application finalization are permanently out of scope; unregistered plantation blocks are rejected before any human.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Op</th><th>Gate</th></tr></thead>\n"
     "      <tbody>\n"
     (str/join "\n" action-gate-rows) "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Audit ledger (this run)</h2>\n"
     "    <p class=\"muted\">Append-only decision-fact log — every proposal, hold and commit this scenario produced from the real actor.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Fact</th><th>Op</th><th>Subject</th><th>Basis</th></tr></thead>\n"
     "      <tbody>\n"
     ledger-rows "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "</main>\n"
     "</body></html>\n")))

(defn -main [& args]
  (let [out (or (first args) "docs/samples/operator-console.html")
        demo (run-demo!)
        out-file (java.io.File. out)
        html (render demo)
        hold-count (count (filter #(= :governor-hold (:t %)) (:ledger demo)))
        commit-count (count (filter #(= :committed (:t %)) (:ledger demo)))]
    (.. out-file getParentFile mkdirs)
    (spit out-file html)
    (println "wrote" out "(" (count (:ledger demo)) "ledger facts,"
             commit-count "commits,"
             hold-count "HARD holds )")))
