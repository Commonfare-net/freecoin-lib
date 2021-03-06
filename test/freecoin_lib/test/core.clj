(ns freecoin-lib.test.core
  (:require [midje.sweet :refer :all]
            [freecoin-lib
             [core  :as blockchain]
             [schemas :as fc]]
            [freecoin-lib.db.freecoin :as db]
            [clj-storage.db.mongo :as mongo]
            [clj-storage.core :as storage]
            [schema.core :as s]
            [taoensso.timbre :as log]
            [clj-time.core :as t])
  (:import [freecoin_lib.core BtcRpc]))

(facts "Validate against schemas"

         (fact Created Mongo stores fit the schema
               (let [uri "mongodb://localhost:27017/some-db"
                     db (mongo/get-mongo-db uri)
                     stores-m (db/create-freecoin-stores  db)]

                 (s/validate fc/StoresMap stores-m) => truthy

                 ;; TODO validate TTl
                 (blockchain/new-mongo "Testcoin" stores-m) => truthy

                 (blockchain/new-mongo nil) => (throws Exception)))

         (fact Created BTC RPC record validates against the schemas
               (let [conf-file (-> "sample-btc-rpc.conf"
                                   (clojure.java.io/resource)
                                   (.getPath))]
                 (blockchain/new-btc-rpc "FAIR" conf-file) => truthy
                 (s/validate BtcRpc (blockchain/new-btc-rpc "FAIR" conf-file)) => truthy
                 (blockchain/new-btc-rpc nil) => (throws Exception))))

(facts "Test internal functions"
       (fact "Test the parameter composition for lib requests"
             (blockchain/add-transaction-list-params
              {:from (t/date-time 2016 11 30)
               :to (t/date-time 2016 12 2)})
             => {:timestamp {"$lt" (t/date-time 2016 12 2)
                             "$gte" (t/date-time 2016 11 30)}}))
