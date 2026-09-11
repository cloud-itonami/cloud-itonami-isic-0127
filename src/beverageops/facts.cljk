(ns beverageops.facts
  "Reference facts for beverage-crop plantation operations coordination:
  supply category cost policy and beverage-crop classification. This
  namespace contains pure lookup functions for domain reference data --
  the Governor and Advisor consult these instead of inventing thresholds.
  Mirrors `orchardops.facts` (cloud-itonami-isic-0122) in shape.")

(def supply-categories
  "Procurement categories this actor may propose orders for, and the
  default cost threshold above which an order proposal must escalate for
  human sign-off (grower/plantation-manager)."
  {"seedling"
   {:id "seedling" :name "苗木" :cost-threshold 500}

   "fertilizer"
   {:id "fertilizer" :name "肥料" :cost-threshold 500}

   "equipment"
   {:id "equipment" :name "設備" :cost-threshold 1000}})

(defn supply-category-by-id [id]
  (get supply-categories id))

(def default-cost-threshold
  "Fallback escalation threshold used when a supply-order proposal doesn't
  cite a known category (never invent a lower bar than this)."
  500)

(def beverage-crop-classes
  "End-use classes this actor's plantation/block records may cover (ISIC
  0127: growing of beverage crops)."
  {"coffee"     {:id "coffee" :name "コーヒー"}
   "tea"        {:id "tea" :name "茶"}
   "cacao"      {:id "cacao" :name "カカオ"}
   "yerba-mate" {:id "yerba-mate" :name "マテ茶"}})

(defn beverage-crop-class-by-id [id]
  (get beverage-crop-classes id))
