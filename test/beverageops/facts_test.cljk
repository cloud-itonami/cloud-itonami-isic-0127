(ns beverageops.facts-test
  (:require [clojure.test :refer [deftest is are testing]]
            [beverageops.facts :as facts]))

(deftest supply-category-lookup
  (testing "Lookup valid supply category"
    (let [c (facts/supply-category-by-id "seedling")]
      (is (= "seedling" (:id c)))
      (is (= "苗木" (:name c)))))

  (testing "Lookup invalid supply category"
    (is (nil? (facts/supply-category-by-id "unknown")))))

(deftest supply-category-cost-thresholds
  (testing "Category-specific cost thresholds"
    (are [id expected] (= expected (:cost-threshold (facts/supply-category-by-id id)))
      "seedling"    500
      "fertilizer"  500
      "equipment"   1000)))

(deftest default-cost-threshold-value
  (testing "Default fallback threshold matches the conservative baseline"
    (is (= 500 facts/default-cost-threshold))))

(deftest beverage-crop-class-lookup
  (testing "Lookup valid beverage-crop class"
    (are [id expected-name] (= expected-name (:name (facts/beverage-crop-class-by-id id)))
      "coffee"     "コーヒー"
      "tea"        "茶"
      "cacao"      "カカオ"
      "yerba-mate" "マテ茶"))

  (testing "Lookup invalid beverage-crop class"
    (is (nil? (facts/beverage-crop-class-by-id "unknown")))))
