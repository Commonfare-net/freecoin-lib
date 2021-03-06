;; Freecoin-lib - library to facilitate blockchain functions

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2015- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Denis Roio <jaromil@dyne.org>
;; Aspasia Beneti <aspra@dyne.org>

;; With contributions by
;; Carlo Sciolla
;; Arjan Scherpenisse <arjan@scherpenisse.net>

;; Freecoin-lib is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; Freecoin-lib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify Freecoin-lib, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns freecoin-lib.test.db.wallet
  (:require [midje.sweet :refer :all]
            [freecoin-lib.core :as fb]
            [freecoin-lib.db 
             [wallet :as wallet]]
            [clj-storage.core :as storage]
            [schema.core :as s]))

(s/with-fn-validation

  (facts "Can create and fetch an empty wallet"
         (let [wallet-store (storage/create-memory-store)
               blockchain (fb/create-in-memory-blockchain :bk)]
           (fact "can create a wallet"
                 (let [{:keys [wallet apikey]} (wallet/new-empty-wallet! wallet-store blockchain
                                                                         "name" "test@email.com")]
                   wallet => (just {:name "name"
                                    :email "test@email.com"
                                    :public-key nil
                                    :private-key nil
                                    :account-id anything})))
           
           (fact "can fetch the wallet by its email"
                 (wallet/fetch wallet-store "test@email.com")
                 => (just {:name "name"
                           :email "test@email.com"
                           :public-key nil
                           :private-key nil
                           :account-id anything})))))

(defn create-wallet [wallet-store blockchain wallet-data]
  (let [{:keys [name email]} wallet-data]
    (:wallet (wallet/new-empty-wallet! wallet-store blockchain name email))))

(defn populate-wallet-store [wallet-store blockchain]
  (let [wallets-data [{:name "James Jones" :email "james@jones.com"}
                      {:name "James Jones" :email "jim@jones.com"}
                      {:name "Sarah Lastname" :email "sarah@email.com"}]]
    (doall (map (partial create-wallet wallet-store blockchain) wallets-data))))

(s/with-fn-validation

  (facts "Can query wallet collection"
         (let [wallet-store (storage/create-memory-store)
               blockchain (fb/create-in-memory-blockchain :bk)
               wallets (populate-wallet-store wallet-store blockchain)]
           
           (fact "without parameters, returns all wallets"
                 (wallet/query wallet-store) => (n-of anything 3))

           (tabular
            (fact "accepts an optional query map argument"
                  (wallet/query wallet-store ?query-m) => (n-of anything ?expected-count))
            ?query-m                                      ?expected-count
            {:name "James Jones"}                         2
            {:name "Sarah Lastname"}                      1
            {:name "Bob Nothere"}                         0
            {:email "james@jones.com"}                    1
            {:email "bob@nothere.com"}                    0
            {:name "James Jones" :email "jim@jones.com"}  1
            {:something "else"}                           0))))
